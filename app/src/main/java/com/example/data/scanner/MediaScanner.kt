package com.example.data.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.data.db.PlaylistEntity
import com.example.data.db.SongDao
import com.example.data.db.SongEntity
import com.example.data.db.TuneFlowDatabase
import com.example.data.db.VideoDao
import com.example.data.db.VideoEntity
import com.example.data.model.Song
import com.example.data.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaScanner(private val context: Context) {
    private val database = TuneFlowDatabase.getDatabase(context)
    private val songDao: SongDao = database.songDao()
    private val videoDao: VideoDao = database.videoDao()
    private val playlistDao = database.playlistDao()

    suspend fun scanDeviceMedia(forceRescan: Boolean = false) = withContext(Dispatchers.IO) {
        val existingSongCount = songDao.getSongCount()
        val existingVideoCount = videoDao.getVideoCount()

        val scannedSongs = mutableListOf<SongEntity>()
        val scannedVideos = mutableListOf<VideoEntity>()

        try {
            // Scan Audio files
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
                            durationMs = if (duration > 0) duration else 180000L,
                            uri = contentUri.toString(),
                            albumArtUri = albumArtUri,
                            folderName = folderName,
                            dateAdded = dateAdded
                        )
                    )
                }
            }

            // Scan Video files
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
                            durationMs = if (duration > 0) duration else 300000L,
                            uri = contentUri.toString(),
                            thumbnailUri = contentUri.toString(),
                            resolution = "1080p",
                            sizeBytes = size,
                            folderName = folderName
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MediaScanner", "Error querying MediaStore", e)
        }

        if (scannedSongs.isNotEmpty()) {
            songDao.insertSongs(scannedSongs)
        } else if (existingSongCount == 0) {
            // Seed initial premium offline library so the app is immediately alive and functional
            seedDemoLibrary()
        }

        if (scannedVideos.isNotEmpty()) {
            videoDao.insertVideos(scannedVideos)
        } else if (existingVideoCount == 0) {
            seedDemoVideos()
        }

        seedDefaultPlaylists()
    }

    private suspend fun seedDemoLibrary() {
        val demoSongs = listOf(
            SongEntity(
                mediaId = "demo_1",
                title = "A New Journey",
                artist = "HOYO-MIX",
                album = "The Shimmering Voyage",
                genre = "Orchestral / Soundtrack",
                durationMs = 204000L, // 3:24
                uri = "asset:///audio/demo_track.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
                folderName = "Anime OST",
                isFavorite = true,
                playCount = 42,
                lyrics = "[00:00.00]A New Journey - HOYO-MIX\n[00:12.50]When the stars align in the quiet sky\n[00:24.00]Whispers of the night calling out your name\n[00:36.20]Through the endless dawn we will find our way\n[00:48.80]Echoes of our dreams lighting up the flame\n[01:05.00]Stand tall, through the wind and stormy seas\n[01:18.40]Our story has just begun in harmony\n[01:32.00]A new journey awaits beyond the silver line\n[01:46.50]Together as one under the velvet night\n[02:04.00]Instrumental Solo\n[02:30.00]Forever in your heart the melody shines"
            ),
            SongEntity(
                mediaId = "demo_2",
                title = "Into the Night",
                artist = "YOASOBI",
                album = "THE BOOK",
                genre = "J-Pop",
                durationMs = 258000L, // 4:18
                uri = "asset:///audio/demo_track.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&auto=format&fit=crop&q=80",
                folderName = "J-Pop",
                isFavorite = true,
                playCount = 38,
                lyrics = "[00:00.00]Into the Night - YOASOBI\n[00:15.00]Sinking down into the shadows of the dusk\n[00:28.00]Captivated by the look within your eyes\n[00:42.00]Running through the neon avenue\n[00:58.00]Let the rhythm sweep all the tears away\n[01:15.00]Racing to the night where tomorrow starts anew"
            ),
            SongEntity(
                mediaId = "demo_3",
                title = "Moonlight",
                artist = "Aimer",
                album = "Sun Dance & Penny Rain",
                genre = "Ballad",
                durationMs = 302000L, // 5:02
                uri = "asset:///audio/demo_track.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&auto=format&fit=crop&q=80",
                folderName = "Night Drive",
                isFavorite = false,
                playCount = 27
            ),
            SongEntity(
                mediaId = "demo_4",
                title = "Stellar Horizon",
                artist = "LiSA",
                album = "LEO-NiNE",
                genre = "Anime Rock",
                durationMs = 276000L, // 4:36
                uri = "asset:///audio/demo_track.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800&auto=format&fit=crop&q=80",
                folderName = "Anime OST",
                isFavorite = true,
                playCount = 31
            ),
            SongEntity(
                mediaId = "demo_5",
                title = "Wind and Stars",
                artist = "Milet",
                album = "eyes",
                genre = "Alternative",
                durationMs = 235000L, // 3:55
                uri = "asset:///audio/demo_track.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&auto=format&fit=crop&q=80",
                folderName = "Chill Vibes",
                isFavorite = false,
                playCount = 19
            ),
            SongEntity(
                mediaId = "demo_6",
                title = "Reflections",
                artist = "yama",
                album = "the meaning of life",
                genre = "Indie",
                durationMs = 261000L, // 4:21
                uri = "asset:///audio/demo_track.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800&auto=format&fit=crop&q=80",
                folderName = "Chill Vibes",
                isFavorite = false,
                playCount = 14
            ),
            SongEntity(
                mediaId = "demo_7",
                title = "Last Page",
                artist = "ZUTOMAYO",
                album = "Gusare",
                genre = "J-Rock",
                durationMs = 229000L, // 3:49
                uri = "asset:///audio/demo_track.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&auto=format&fit=crop&q=80",
                folderName = "Workout",
                isFavorite = true,
                playCount = 22
            ),
            SongEntity(
                mediaId = "demo_8",
                title = "Eternal Echo",
                artist = "SawanoHiroyuki[nZk]",
                album = "iv",
                genre = "Cinematic / Epic",
                durationMs = 333000L, // 5:33
                uri = "asset:///audio/demo_track.mp3",
                albumArtUri = "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=800&auto=format&fit=crop&q=80",
                folderName = "Anime OST",
                isFavorite = true,
                playCount = 35
            )
        )
        songDao.insertSongs(demoSongs)
    }

    private suspend fun seedDemoVideos() {
        val demoVideos = listOf(
            VideoEntity(
                mediaId = "vid_1",
                title = "Anime Movie.mp4",
                durationMs = 1458000L, // 24:18
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                thumbnailUri = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&auto=format&fit=crop&q=80",
                resolution = "1080p",
                sizeBytes = 671088640L,
                folderName = "Anime",
                isFavorite = true,
                playCount = 3,
                lastPositionMs = 324000L
            ),
            VideoEntity(
                mediaId = "vid_2",
                title = "Travel Vlog.mp4",
                durationMs = 762000L, // 12:42
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                thumbnailUri = "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=800&auto=format&fit=crop&q=80",
                resolution = "4K Ultra HD",
                sizeBytes = 891289600L,
                folderName = "Travel",
                isFavorite = false,
                playCount = 1
            ),
            VideoEntity(
                mediaId = "vid_3",
                title = "Nature.mp4",
                durationMs = 515000L, // 08:35
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                thumbnailUri = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop&q=80",
                resolution = "1080p",
                sizeBytes = 241172480L,
                folderName = "Documentary",
                isFavorite = true,
                playCount = 5
            ),
            VideoEntity(
                mediaId = "vid_4",
                title = "Game Trailer.mp4",
                durationMs = 1100000L, // 18:20
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                thumbnailUri = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&auto=format&fit=crop&q=80",
                resolution = "1080p",
                sizeBytes = 534773760L,
                folderName = "Trailers",
                isFavorite = false,
                playCount = 2
            )
        )
        videoDao.insertVideos(demoVideos)
    }

    private suspend fun seedDefaultPlaylists() {
        val playlists = listOf(
            PlaylistEntity(name = "Liked Songs", isSmart = true, smartType = "FAVORITES", coverArtUri = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80"),
            PlaylistEntity(name = "Chill Vibes", isSmart = false, coverArtUri = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&auto=format&fit=crop&q=80"),
            PlaylistEntity(name = "Workout", isSmart = false, coverArtUri = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800&auto=format&fit=crop&q=80"),
            PlaylistEntity(name = "Anime OST", isSmart = false, coverArtUri = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&auto=format&fit=crop&q=80"),
            PlaylistEntity(name = "Night Drive", isSmart = false, coverArtUri = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=800&auto=format&fit=crop&q=80")
        )
        playlists.forEach {
            playlistDao.insertPlaylist(it)
        }
    }
}
