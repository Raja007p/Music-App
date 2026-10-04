package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VideoThumbnailCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val lruCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun get(key: String): Bitmap? = lruCache.get(key)
    fun put(key: String, bitmap: Bitmap) {
        if (lruCache.get(key) == null) {
            lruCache.put(key, bitmap)
        }
    }
}

suspend fun loadVideoThumbnailBitmap(context: Context, uriString: String): Bitmap? = withContext(Dispatchers.IO) {
    if (uriString.isBlank()) return@withContext null

    VideoThumbnailCache.get(uriString)?.let { return@withContext it }

    var bitmap: Bitmap? = null

    // 1. Try ContentResolver.loadThumbnail for API 29+ content URIs
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && uriString.startsWith("content://")) {
        try {
            val uri = Uri.parse(uriString)
            bitmap = context.contentResolver.loadThumbnail(uri, Size(512, 288), null)
        } catch (_: Exception) {
            // Fall through to retriever
        }
    }

    // 2. Try MediaMetadataRetriever
    if (bitmap == null) {
        val retriever = MediaMetadataRetriever()
        try {
            if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                retriever.setDataSource(uriString, HashMap())
            } else {
                retriever.setDataSource(context, Uri.parse(uriString))
            }
            bitmap = retriever.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(0L)
                ?: retriever.frameAtTime
        } catch (_: Exception) {
            // Ignore error
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    if (bitmap != null) {
        VideoThumbnailCache.put(uriString, bitmap)
    }

    bitmap
}

@Composable
fun VideoThumbnailView(
    uri: String?,
    title: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = uri) {
        if (!uri.isNullOrBlank()) {
            value = loadVideoThumbnailBitmap(context, uri)
        } else {
            value = null
        }
    }

    val bitmap = bitmapState.value

    Crossfade(targetState = bitmap, label = "video_thumbnail_fade", modifier = modifier) { currentBitmap ->
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = title ?: "Video thumbnail",
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Aesthetic fallback placeholder with gradient and play icon
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF1E1B4B),
                                Color(0xFF0F172A),
                                Color(0xFF1E293B)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
