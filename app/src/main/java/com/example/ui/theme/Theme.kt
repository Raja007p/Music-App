package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode {
    SYSTEM, DARK, LIGHT, AMOLED
}

enum class AccentColor(val title: String, val color: Color) {
    PURPLE("Cosmic Purple", AccentCosmicPurple),
    BLUE("Electric Blue", AccentElectricBlue),
    CYAN("Neon Cyan", AccentNeonCyan),
    PINK("Hot Pink", AccentHotPink),
    ORANGE("Sunset Orange", AccentSunsetOrange),
    GREEN("Emerald Green", AccentEmeraldGreen)
}

@Composable
fun TuneFlowTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    accent: AccentColor = AccentColor.PURPLE,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.AMOLED -> true
    }
    val isAmoled = themeMode == AppThemeMode.AMOLED

    val primaryColor = accent.color

    val colorScheme = when {
        isAmoled -> darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            secondary = primaryColor.copy(alpha = 0.8f),
            onSecondary = Color.White,
            tertiary = AccentNeonCyan,
            background = AmoledBackground,
            onBackground = TextWhite,
            surface = AmoledSurface,
            onSurface = TextWhite,
            surfaceVariant = AmoledSurfaceElevated,
            onSurfaceVariant = TextMuted,
            outline = AmoledBorder
        )
        isDark -> darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            secondary = primaryColor.copy(alpha = 0.8f),
            onSecondary = Color.White,
            tertiary = AccentNeonCyan,
            background = DarkBackground,
            onBackground = TextWhite,
            surface = DarkSurface,
            onSurface = TextWhite,
            surfaceVariant = DarkSurfaceElevated,
            onSurfaceVariant = TextMuted,
            outline = DarkBorder
        )
        else -> lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            secondary = primaryColor.copy(alpha = 0.8f),
            onSecondary = Color.White,
            tertiary = AccentNeonCyan,
            background = LightBackground,
            onBackground = TextDark,
            surface = LightSurface,
            onSurface = TextDark,
            surfaceVariant = LightSurfaceElevated,
            onSurfaceVariant = TextSubtle,
            outline = LightBorder
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    TuneFlowTheme(
        themeMode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
        accent = AccentColor.PURPLE,
        content = content
    )
}
