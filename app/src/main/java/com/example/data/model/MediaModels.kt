package com.example.data.model

data class Song(
    val id: Long = 0,
    val mediaId: String = "",
    val title: String,
    val artist: String,
    val album: String = "",
    val genre: String = "Pop",
    val durationMs: Long = 0,
    val uri: String = "",
    val albumArtUri: String? = null,
    val folderName: String = "Music",
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastPlayedTimestamp: Long = 0L,
    val lyrics: String? = null
)

data class Video(
    val id: Long = 0,
    val mediaId: String = "",
    val title: String,
    val durationMs: Long = 0,
    val uri: String = "",
    val thumbnailUri: String? = null,
    val resolution: String = "1080p",
    val sizeBytes: Long = 0L,
    val folderName: String = "Videos",
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPositionMs: Long = 0L,
    val lastPlayedTimestamp: Long = 0L
)

data class Playlist(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSmart: Boolean = false,
    val smartType: String? = null,
    val songCount: Int = 0,
    val coverArtUri: String? = null
)

data class EqualizerState(
    val isEnabled: Boolean = true,
    val currentPreset: String = "Rock",
    // 10 bands (-12 to +12 dB)
    val bandLevels: List<Int> = listOf(4, 2, -1, 3, 5, 2, 0, 3, 4, 6),
    val bassBoost: Int = 60, // 0 - 100%
    val virtualizer: Int = 30, // 0 - 100%
    val volumeGain: Float = 1.0f,
    val isMono: Boolean = false,
    val balance: Float = 0.0f // -1.0 (left) to +1.0 (right)
)

data class PlaybackSettings(
    val autoPlayNext: Boolean = true,
    val gaplessPlayback: Boolean = true,
    val crossfadeDurationSec: Int = 0, // 0 to 10s
    val playbackSpeed: Float = 1.0f,
    val rememberLongAudioPosition: Boolean = true,
    val stopAfterCurrentTrack: Boolean = false,
    val skipSilence: Boolean = false
)

data class LyricLine(
    val timestampMs: Long,
    val text: String
)
