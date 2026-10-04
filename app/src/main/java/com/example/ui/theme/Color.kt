package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// TuneFlow Core Cosmic Dark Colors
val DarkBackground = Color(0xFF090C15)
val DarkSurface = Color(0xFF111524)
val DarkSurfaceElevated = Color(0xFF171C30)
val DarkSurfaceCard = Color(0xFF131728)
val DarkBorder = Color(0xFF222944)
val DarkBorderGlow = Color(0x408B5CF6)

// AMOLED Colors
val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0A0C14)
val AmoledSurfaceElevated = Color(0xFF121420)
val AmoledBorder = Color(0xFF1E2235)

// Light Theme Colors
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF1F5F9)
val LightSurfaceCard = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFE2E8F0)

// Accent Colors
val AccentCosmicPurple = Color(0xFF8B5CF6)
val AccentElectricBlue = Color(0xFF3B82F6)
val AccentNeonCyan = Color(0xFF06B6D4)
val AccentHotPink = Color(0xFFEC4899)
val AccentSunsetOrange = Color(0xFFF97316)
val AccentEmeraldGreen = Color(0xFF10B981)

// Gradient Helpers
val PurpleBlueGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))
)

val CyanBlueGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
)

val PinkPurpleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
)

val CardGlowGradient = Brush.verticalGradient(
    colors = listOf(Color(0x338B5CF6), Color(0x058B5CF6))
)

// Text Colors
val TextWhite = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)
val TextSubtle = Color(0xFF64748B)
val TextDark = Color(0xFF0F172A)
