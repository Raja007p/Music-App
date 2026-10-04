package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

class PlaybackPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tuneflow_playback_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_SONG_ID = "last_song_id"
        private const val KEY_LAST_POSITION_MS = "last_position_ms"
        private const val KEY_LAST_QUEUE_IDS = "last_queue_ids"
        private const val KEY_LAST_VIDEO_ID = "last_video_id"
        private const val KEY_RESUME_PLAYBACK = "resume_playback_enabled"
        private const val KEY_VIDEO_BACKGROUND_PLAY = "video_background_play_enabled"
    }

    var lastSongId: Long
        get() = prefs.getLong(KEY_LAST_SONG_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_LAST_SONG_ID, value).apply()

    var lastPositionMs: Long
        get() = prefs.getLong(KEY_LAST_POSITION_MS, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_POSITION_MS, value).apply()

    var lastVideoId: Long
        get() = prefs.getLong(KEY_LAST_VIDEO_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_LAST_VIDEO_ID, value).apply()

    var isResumePlaybackEnabled: Boolean
        get() = prefs.getBoolean(KEY_RESUME_PLAYBACK, true)
        set(value) = prefs.edit().putBoolean(KEY_RESUME_PLAYBACK, value).apply()

    var isVideoBackgroundPlayEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIDEO_BACKGROUND_PLAY, true)
        set(value) = prefs.edit().putBoolean(KEY_VIDEO_BACKGROUND_PLAY, value).apply()

    fun saveQueue(songIds: List<Long>) {
        val serialized = songIds.joinToString(",")
        prefs.edit().putString(KEY_LAST_QUEUE_IDS, serialized).apply()
    }

    fun getQueue(): List<Long> {
        val serialized = prefs.getString(KEY_LAST_QUEUE_IDS, null) ?: return emptyList()
        return serialized.split(",").mapNotNull { it.toLongOrNull() }
    }
}
