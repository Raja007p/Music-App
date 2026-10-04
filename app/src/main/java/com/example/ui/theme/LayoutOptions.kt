package com.example.ui.theme

enum class NowPlayingLayout(val title: String, val subtitle: String) {
    COSMIC_EXPANSIVE("Cosmic Glow", "Ambient lighting & large artwork"),
    VINYL_TURNTABLE("Vinyl Record", "Realistic spinning vinyl & tone arm"),
    MINIMAL_CARD("Minimal Card", "Modern clean card & typography")
}

enum class LibraryLayout(val title: String, val subtitle: String) {
    TABBED_LIST("Tabbed List", "Full tabs with sorting & search"),
    COMPACT_LIST("Compact Rows", "Dense list with fast scrolling"),
    GRID_CARDS("Visual Grid", "2-column modern album cards")
}

enum class VideoLayout(val title: String, val subtitle: String) {
    CINEMA_CARDS("Cinema 16:9", "Large cinematic cards with duration"),
    COMPACT_LIST("Compact Rows", "Horizontal preview rows with metadata"),
    BENTO_GRID("Bento Cards", "Modern staggered visual grid")
}
