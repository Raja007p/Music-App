package com.example.playback

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.db.SongDao
import com.example.data.db.TuneFlowDatabase
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RepeatMode {
    OFF, ALL, ONE
}

class TuneFlowPlayerManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val database = TuneFlowDatabase.getDatabase(context)
    private val songDao: SongDao = database.songDao()
    private val prefs = context.getSharedPreferences("tuneflow_playback_prefs", Context.MODE_PRIVATE)

    val effectsManager = AudioEffectsManager(context)

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    // Sleep Timer
    private val _sleepTimerRemainingMs = MutableStateFlow<Long?>(null)
    val sleepTimerRemainingMs: StateFlow<Long?> = _sleepTimerRemainingMs.asStateFlow()

    private val _endAfterCurrentSong = MutableStateFlow(false)
    val endAfterCurrentSong: StateFlow<Boolean> = _endAfterCurrentSong.asStateFlow()

    private var sleepTimer: CountDownTimer? = null
    private var progressJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) {
                    startProgressTracking()
                    startMediaService()
                } else {
                    stopProgressTracking()
                    savePlaybackState()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val d = player.duration
                    _duration.value = if (d > 0) d else (_currentSong.value?.durationMs ?: 0L)
                    effectsManager.attachAudioSession(player.audioSessionId)
                } else if (playbackState == Player.STATE_ENDED) {
                    if (_endAfterCurrentSong.value) {
                        player.pause()
                        _endAfterCurrentSong.value = false
                        _sleepTimerRemainingMs.value = null
                    } else {
                        handleTrackEnded()
                    }
                }
            }
        })

        // Restore last playback state
        restoreLastPlayback()
    }

    private fun startMediaService() {
        try {
            val intent = Intent(context, TuneFlowMediaService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e("TuneFlowPlayer", "Failed to start MediaService", e)
        }
    }

    private fun savePlaybackState() {
        val song = _currentSong.value ?: return
        prefs.edit()
            .putLong("last_song_id", song.id)
            .putLong("last_position_ms", player.currentPosition)
            .apply()
    }

    fun restoreLastPlayback() {
        scope.launch(Dispatchers.IO) {
            val lastSongId = prefs.getLong("last_song_id", -1L)
            val lastPos = prefs.getLong("last_position_ms", 0L)
            if (lastSongId != -1L) {
                val songEntity = songDao.getSongById(lastSongId)
                if (songEntity != null && !songEntity.mediaId.startsWith("demo_")) {
                    withContext(Dispatchers.Main) {
                        val song = songEntity.toSong()
                        _currentSong.value = song
                        _queue.value = listOf(song)
                        _currentIndex.value = 0
                        _playbackPosition.value = lastPos
                        _duration.value = song.durationMs

                        try {
                            val mediaItem = createMediaItem(song)
                            player.setMediaItem(mediaItem)
                            player.prepare()
                            player.seekTo(lastPos)
                        } catch (e: Exception) {
                            Log.e("TuneFlowPlayer", "Error preparing restored track", e)
                        }
                    }
                }
            }
        }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            var counter = 0
            while (isActive) {
                if (player.isPlaying) {
                    _playbackPosition.value = player.currentPosition
                    val d = player.duration
                    if (d > 0) _duration.value = d

                    // Save state every ~5 seconds
                    counter++
                    if (counter % 12 == 0) {
                        savePlaybackState()
                    }
                }
                delay(400)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        _playbackPosition.value = player.currentPosition
    }

    fun playSong(song: Song, newQueue: List<Song>? = null) {
        val activeQueue = newQueue ?: _queue.value
        val queueWithSong = if (activeQueue.any { it.id == song.id }) {
            activeQueue
        } else {
            activeQueue + song
        }
        _queue.value = queueWithSong
        val index = queueWithSong.indexOfFirst { it.id == song.id }
        _currentIndex.value = if (index >= 0) index else 0
        _currentSong.value = song

        try {
            val mediaItem = createMediaItem(song)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            _isPlaying.value = true
            startMediaService()

            // Record playback in DB history
            scope.launch(Dispatchers.IO) {
                songDao.recordPlay(song.id, System.currentTimeMillis())
                savePlaybackState()
            }
        } catch (e: Exception) {
            Log.e("TuneFlowPlayer", "Error playing song: ${song.title}", e)
        }
    }

    private fun createMediaItem(song: Song): MediaItem {
        val uri = Uri.parse(song.uri)
        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(song.id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.albumArtUri?.let { Uri.parse(it) })
                    .build()
            )
            .build()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
            savePlaybackState()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.play()
            startMediaService()
        }
    }

    fun pause() {
        player.pause()
        savePlaybackState()
    }

    fun next() {
        val q = _queue.value
        if (q.isEmpty()) return
        val currentIdx = _currentIndex.value
        val nextIdx = if (_isShuffleEnabled.value) {
            (q.indices).random()
        } else {
            (currentIdx + 1) % q.size
        }
        val nextSong = q[nextIdx]
        playSong(nextSong)
    }

    fun previous() {
        val q = _queue.value
        if (q.isEmpty()) return
        if (player.currentPosition > 3000) {
            player.seekTo(0)
            return
        }
        val currentIdx = _currentIndex.value
        val prevIdx = if (currentIdx > 0) currentIdx - 1 else q.size - 1
        val prevSong = q[prevIdx]
        playSong(prevSong)
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _playbackPosition.value = positionMs
        savePlaybackState()
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        player.playbackParameters = PlaybackParameters(speed)
    }

    private fun handleTrackEnded() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                player.seekTo(0)
                player.play()
            }
            RepeatMode.ALL -> {
                next()
            }
            RepeatMode.OFF -> {
                val currentIdx = _currentIndex.value
                val q = _queue.value
                if (currentIdx < q.size - 1) {
                    next()
                } else {
                    player.pause()
                    player.seekTo(0)
                    savePlaybackState()
                }
            }
        }
    }

    fun toggleFavorite(song: Song) {
        val updatedFav = !song.isFavorite
        if (_currentSong.value?.id == song.id) {
            _currentSong.value = _currentSong.value?.copy(isFavorite = updatedFav)
        }
        val updatedQueue = _queue.value.map {
            if (it.id == song.id) it.copy(isFavorite = updatedFav) else it
        }
        _queue.value = updatedQueue
        scope.launch(Dispatchers.IO) {
            songDao.setFavorite(song.id, updatedFav)
        }
    }

    fun addToQueue(song: Song) {
        _queue.value = _queue.value + song
    }

    fun playNext(song: Song) {
        val q = _queue.value.toMutableList()
        val currentIdx = _currentIndex.value
        val insertIndex = if (currentIdx >= 0 && currentIdx < q.size) currentIdx + 1 else q.size
        q.add(insertIndex, song)
        _queue.value = q
    }

    fun removeFromQueue(index: Int) {
        val q = _queue.value.toMutableList()
        if (index in q.indices) {
            q.removeAt(index)
            _queue.value = q
            if (index < _currentIndex.value) {
                _currentIndex.value -= 1
            } else if (index == _currentIndex.value) {
                if (q.isNotEmpty()) {
                    val newIndex = index.coerceAtMost(q.size - 1)
                    _currentIndex.value = newIndex
                    playSong(q[newIndex])
                } else {
                    player.stop()
                    _currentSong.value = null
                    _currentIndex.value = -1
                }
            }
        }
    }

    fun clearQueue() {
        val current = _currentSong.value
        if (current != null) {
            _queue.value = listOf(current)
            _currentIndex.value = 0
        } else {
            _queue.value = emptyList()
            _currentIndex.value = -1
        }
    }

    // Sleep timer implementations
    fun startSleepTimer(minutes: Int) {
        sleepTimer?.cancel()
        _endAfterCurrentSong.value = false
        val durationMs = minutes * 60 * 1000L
        _sleepTimerRemainingMs.value = durationMs

        sleepTimer = object : CountDownTimer(durationMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _sleepTimerRemainingMs.value = millisUntilFinished
            }

            override fun onFinish() {
                _sleepTimerRemainingMs.value = null
                player.pause()
            }
        }.start()
    }

    fun setEndAfterCurrentSong(enabled: Boolean) {
        sleepTimer?.cancel()
        _sleepTimerRemainingMs.value = null
        _endAfterCurrentSong.value = enabled
    }

    fun cancelSleepTimer() {
        sleepTimer?.cancel()
        sleepTimer = null
        _sleepTimerRemainingMs.value = null
        _endAfterCurrentSong.value = false
    }

    fun release() {
        progressJob?.cancel()
        sleepTimer?.cancel()
        effectsManager.release()
        player.release()
    }

    companion object {
        @Volatile
        private var INSTANCE: TuneFlowPlayerManager? = null

        fun getInstance(context: Context): TuneFlowPlayerManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TuneFlowPlayerManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
