package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.model.Video

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaId: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val durationMs: Long,
    val uri: String,
    val albumArtUri: String?,
    val folderName: String,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastPlayedTimestamp: Long = 0L,
    val lyrics: String? = null
) {
    fun toSong(): Song = Song(
        id = id,
        mediaId = mediaId,
        title = title,
        artist = artist,
        album = album,
        genre = genre,
        durationMs = durationMs,
        uri = uri,
        albumArtUri = albumArtUri,
        folderName = folderName,
        isFavorite = isFavorite,
        playCount = playCount,
        dateAdded = dateAdded,
        lastPlayedTimestamp = lastPlayedTimestamp,
        lyrics = lyrics
    )

    companion object {
        fun fromSong(song: Song): SongEntity = SongEntity(
            id = song.id,
            mediaId = song.mediaId,
            title = song.title,
            artist = song.artist,
            album = song.album,
            genre = song.genre,
            durationMs = song.durationMs,
            uri = song.uri,
            albumArtUri = song.albumArtUri,
            folderName = song.folderName,
            isFavorite = song.isFavorite,
            playCount = song.playCount,
            dateAdded = song.dateAdded,
            lastPlayedTimestamp = song.lastPlayedTimestamp,
            lyrics = song.lyrics
        )
    }
}

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaId: String,
    val title: String,
    val durationMs: Long,
    val uri: String,
    val thumbnailUri: String?,
    val resolution: String,
    val sizeBytes: Long,
    val folderName: String,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPositionMs: Long = 0L,
    val lastPlayedTimestamp: Long = 0L
) {
    fun toVideo(): Video = Video(
        id = id,
        mediaId = mediaId,
        title = title,
        durationMs = durationMs,
        uri = uri,
        thumbnailUri = thumbnailUri,
        resolution = resolution,
        sizeBytes = sizeBytes,
        folderName = folderName,
        isFavorite = isFavorite,
        playCount = playCount,
        lastPositionMs = lastPositionMs,
        lastPlayedTimestamp = lastPlayedTimestamp
    )

    companion object {
        fun fromVideo(v: Video): VideoEntity = VideoEntity(
            id = v.id,
            mediaId = v.mediaId,
            title = v.title,
            durationMs = v.durationMs,
            uri = v.uri,
            thumbnailUri = v.thumbnailUri,
            resolution = v.resolution,
            sizeBytes = v.sizeBytes,
            folderName = v.folderName,
            isFavorite = v.isFavorite,
            playCount = v.playCount,
            lastPositionMs = v.lastPositionMs,
            lastPlayedTimestamp = v.lastPlayedTimestamp
        )
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSmart: Boolean = false,
    val smartType: String? = null,
    val coverArtUri: String? = null
) {
    fun toPlaylist(count: Int = 0): Playlist = Playlist(
        id = id,
        name = name,
        createdAt = createdAt,
        isSmart = isSmart,
        smartType = smartType,
        songCount = count,
        coverArtUri = coverArtUri
    )
}

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val orderIndex: Int
)

@Entity(tableName = "queue_items")
data class QueueItemEntity(
    @PrimaryKey val orderIndex: Int,
    val songId: Long
)
