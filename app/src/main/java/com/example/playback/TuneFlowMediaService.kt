package com.example.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class TuneFlowMediaService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var lastBitmap: Bitmap? = null
    private var lastArtUri: String? = null

    companion object {
        const val CHANNEL_ID = "tuneflow_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY_PAUSE = "com.example.playback.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.playback.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.playback.ACTION_PREVIOUS"
        const val ACTION_STOP = "com.example.playback.ACTION_STOP"

        fun startService(context: Context) {
            try {
                val intent = Intent(context, TuneFlowMediaService::class.java)
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                Log.e("TuneFlowMediaService", "Error starting media service", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val playerManager = TuneFlowPlayerManager.getInstance(applicationContext)

        mediaSession = MediaSession.Builder(this, playerManager.player)
            .setCallback(object : MediaSession.Callback {})
            .build()

        playerManager.player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updateNotification()
            }
        })

        // Observe song changes
        serviceScope.launch {
            playerManager.currentSong.collect {
                updateNotification()
            }
        }

        updateNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val playerManager = TuneFlowPlayerManager.getInstance(applicationContext)
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                playerManager.togglePlayPause()
                updateNotification()
            }
            ACTION_NEXT -> {
                playerManager.next()
                updateNotification()
            }
            ACTION_PREVIOUS -> {
                playerManager.previous()
                updateNotification()
            }
            ACTION_STOP -> {
                playerManager.pause()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        super.onStartCommand(intent, flags, startId)
        updateNotification()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TuneFlow Playback Controls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for background audio and video playback"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun updateNotification() {
        val playerManager = TuneFlowPlayerManager.getInstance(applicationContext)
        val song = playerManager.currentSong.value
        val isPlaying = playerManager.isPlaying.value

        if (song == null) {
            return
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Previous Action
        val prevIntent = Intent(this, TuneFlowMediaService::class.java).apply {
            action = ACTION_PREVIOUS
        }
        val prevPendingIntent = PendingIntent.getService(
            this,
            1,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Play/Pause Action
        val playPauseIntent = Intent(this, TuneFlowMediaService::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePendingIntent = PendingIntent.getService(
            this,
            2,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Next Action
        val nextIntent = Intent(this, TuneFlowMediaService::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getService(
            this,
            3,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop Action
        val stopIntent = Intent(this, TuneFlowMediaService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            4,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSubText(song.album)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setAutoCancel(false)
            .setSilent(true)
            .addAction(
                android.R.drawable.ic_media_previous,
                "Previous",
                prevPendingIntent
            )
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                playPausePendingIntent
            )
            .addAction(
                android.R.drawable.ic_media_next,
                "Next",
                nextPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Close",
                stopPendingIntent
            )

        if (lastBitmap != null) {
            builder.setLargeIcon(lastBitmap)
        }

        try {
            startForeground(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            Log.e("TuneFlowMediaService", "Error starting foreground notification", e)
        }

        // Asynchronously load artwork bitmap for large icon if changed
        val artUri = song.albumArtUri
        if (artUri != null && artUri != lastArtUri) {
            lastArtUri = artUri
            serviceScope.launch(Dispatchers.IO) {
                val bitmap = loadArtworkBitmap(artUri)
                if (bitmap != null) {
                    lastBitmap = bitmap
                    withContext(Dispatchers.Main) {
                        builder.setLargeIcon(bitmap)
                        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.notify(NOTIFICATION_ID, builder.build())
                    }
                }
            }
        }
    }

    private fun loadArtworkBitmap(uriString: String): Bitmap? {
        return try {
            if (uriString.startsWith("content://")) {
                val input = contentResolver.openInputStream(Uri.parse(uriString))
                BitmapFactory.decodeStream(input)
            } else if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                val url = URL(uriString)
                BitmapFactory.decodeStream(url.openStream())
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val playerManager = TuneFlowPlayerManager.getInstance(applicationContext)
        if (!playerManager.isPlaying.value) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.launch {
            mediaSession?.run {
                release()
                mediaSession = null
            }
        }
        super.onDestroy()
    }
}
