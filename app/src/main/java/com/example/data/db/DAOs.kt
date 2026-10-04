package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayed(limit: Int = 20): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT :limit")
    fun getRecentlyAdded(limit: Int = 20): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    suspend fun getSongsByIds(ids: List<Long>): List<SongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("UPDATE songs SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: Long, isFav: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun recordPlay(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE songs SET lyrics = :lyrics WHERE id = :id")
    suspend fun updateLyrics(id: Long, lyrics: String)

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int

    @Query("DELETE FROM songs WHERE mediaId LIKE 'demo_%'")
    suspend fun deleteDemoSongs()
}

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY title ASC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentlyPlayedVideos(limit: Int = 20): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): VideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("UPDATE videos SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: Long, isFav: Boolean)

    @Query("UPDATE videos SET lastPositionMs = :positionMs, lastPlayedTimestamp = :timestamp, playCount = playCount + 1 WHERE id = :id")
    suspend fun updatePlaybackPosition(id: Long, positionMs: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM videos WHERE title LIKE '%' || :query || '%'")
    fun searchVideos(query: String): Flow<List<VideoEntity>>

    @Query("SELECT COUNT(*) FROM videos")
    suspend fun getVideoCount(): Int

    @Query("DELETE FROM videos WHERE mediaId LIKE 'vid_%'")
    suspend fun deleteDemoVideos()
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("DELETE FROM playlists WHERE isSmart = 0 AND name IN ('Chill Vibes', 'Workout', 'Anime OST', 'Night Drive')")
    suspend fun deleteDemoPlaylists()

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.orderIndex ASC
    """)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun getSongCount(playlistId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylistSongs(playlistId: Long)

    @Transaction
    suspend fun setPlaylistSongs(playlistId: Long, songIds: List<Long>) {
        clearPlaylistSongs(playlistId)
        songIds.forEachIndexed { index, songId ->
            addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId, index))
        }
    }
}

@Dao
interface QueueDao {
    @Query("SELECT * FROM queue_items ORDER BY orderIndex ASC")
    fun getQueue(): Flow<List<QueueItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItems(items: List<QueueItemEntity>)

    @Query("DELETE FROM queue_items")
    suspend fun clearQueue()

    @Transaction
    suspend fun replaceQueue(songIds: List<Long>) {
        clearQueue()
        val entities = songIds.mapIndexed { index, id ->
            QueueItemEntity(orderIndex = index, songId = id)
        }
        insertQueueItems(entities)
    }
}
