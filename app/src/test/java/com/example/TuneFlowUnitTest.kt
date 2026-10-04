package com.example

import com.example.data.model.Song
import com.example.data.model.Video
import com.example.ui.components.formatDuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TuneFlowUnitTest {

    @Test
    fun testFormatDuration() {
        assertEquals("0:00", formatDuration(0L))
        assertEquals("1:00", formatDuration(60000L))
        assertEquals("3:24", formatDuration(204000L))
        assertEquals("24:18", formatDuration(1458000L))
    }

    @Test
    fun testSongModelCreation() {
        val song = Song(
            id = 1L,
            title = "A New Journey",
            artist = "HOYO-MIX",
            durationMs = 204000L,
            isFavorite = true
        )
        assertEquals("A New Journey", song.title)
        assertEquals("HOYO-MIX", song.artist)
        assertTrue(song.isFavorite)
    }

    @Test
    fun testVideoModelCreation() {
        val video = Video(
            id = 10L,
            title = "Anime Movie.mp4",
            durationMs = 1458000L,
            resolution = "1080p"
        )
        assertEquals("Anime Movie.mp4", video.title)
        assertEquals("1080p", video.resolution)
    }
}
