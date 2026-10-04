package com.example.data.scanner

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.data.db.PlaylistEntity
import com.example.data.db.SongDao
import com.example.data.db.SongEntity
import com.example.data.db.TuneFlowDatabase
import com.example.data.db.VideoDao
import com.example.data.db.VideoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaScanner(private val context: Context) {
    private val database = TuneFlowDatabase.getDatabase(context)
    private val songDao: SongDao = database.songDao()
    private val videoDao: VideoDao = database.videoDao()
    private val playlistDao = database.playlistDao()

    suspend fun scanDeviceMedia(forceRescan: Boolean = false) = withContext(Dispatchers.IO) {
        // Purge any residual demo songs, videos, and playlists
        songDao.deleteDemoSongs()
        videoDao.deleteDemoVideos()
        playlistDao.deleteDemoPlaylists()

        val scannedSongs = mutableListOf<SongEntity>()
        val scannedVideos = mutableListOf<VideoEntity>()

        try {
            // Scan Audio files from device MediaStore
            val audioProjection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.ALBUM_ID
            )
            val audioSelection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val audioCursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                audioProjection,
                audioSelection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )

            audioCursor?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val mediaId = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val duration = cursor.getLong(durationCol)
                    val dataPath = cursor.getString(dataCol) ?: ""
                    val dateAdded = cursor.getLong(dateCol) * 1000L
                    val albumId = cursor.getLong(albumIdCol)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mediaId
                    )
                    val albumArtUri = "content://media/external/audio/albumart/$albumId"
                    val folderName = try {
                        File(dataPath).parentFile?.name ?: "Music"
                    } catch (e: Exception) {
                        "Music"
                    }

                    scannedSongs.add(
                        SongEntity(
                            mediaId = mediaId.toString(),
                            title = title,
                            artist = if (artist.contains("<unknown>")) "Unknown Artist" else artist,
                            album = if (album.contains("<unknown>")) "Unknown Album" else album,
                            genre = "Music",
                            durationMs = if (duration > 0) duration else 0L,
                            uri = contentUri.toString(),
                            albumArtUri = albumArtUri,
                            folderName = folderName,
                            dateAdded = dateAdded,
                            isFavorite = false
                        )
                    )
                }
            }

            // Scan Video files from device MediaStore
            val videoProjection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED
            )
            val videoCursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                "${MediaStore.Video.Media.TITLE} ASC"
            )

            videoCursor?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

                while (cursor.moveToNext()) {
                    val mediaId = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Video $mediaId"
                    val duration = cursor.getLong(durationCol)
                    val dataPath = cursor.getString(dataCol) ?: ""
                    val size = cursor.getLong(sizeCol)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        mediaId
                    )
                    val folderName = try {
                        File(dataPath).parentFile?.name ?: "Videos"
                    } catch (e: Exception) {
                        "Videos"
                    }

                    scannedVideos.add(
                        VideoEntity(
                            mediaId = mediaId.toString(),
                            title = title,
                            durationMs = if (duration > 0) duration else 0L,
                            uri = contentUri.toString(),
                            thumbnailUri = contentUri.toString(),
                            resolution = "Video",
                            sizeBytes = size,
                            folderName = folderName,
                            isFavorite = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MediaScanner", "Error querying MediaStore", e)
        }

        if (scannedSongs.isNotEmpty()) {
            songDao.insertSongs(scannedSongs)
        }

        if (scannedVideos.isNotEmpty()) {
            videoDao.insertVideos(scannedVideos)
        }

        // Initialize single default Smart Playlist "Liked Songs" if not present
        ensureLikedSongsPlaylist()
    }

    private suspend fun ensureLikedSongsPlaylist() {
        val existing = playlistDao.getAllPlaylists()
        // If no smart playlist exists, create the Liked Songs smart playlist
        playlistDao.insertPlaylist(
            PlaylistEntity(
                name = "Liked Songs",
                isSmart = true,
                smartType = "FAVORITES",
                coverArtUri = null
            )
        )
    }
}
