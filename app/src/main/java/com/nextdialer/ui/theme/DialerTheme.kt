package com.nextdialer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

data class DialerTheme(
    val id: String,
    val label: String,
    val description: String,
    val useWallpaper: Boolean,
    val dark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceStrong: Color,
    val navBar: Color,
    val searchField: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val accentOn: Color,
    val topBar: Color,
    val divider: Color,
    val callGreen: Color,
    val missedRed: Color,
    val incoming: Color,
    val outgoing: Color,
    val settingsBackground: Color,
)

object DialerThemes {

    val Crystal = DialerTheme(
        id = "crystal",
        label = "Crystal",
        description = "Dimmed wallpaper, translucent panels, blue digits",
        useWallpaper = true,
        dark = true,
        background = Color(0xFF2A2230),
        surface = Color(0x8A303030),
        surfaceStrong = Color(0xE61A1A1A),
        navBar = Color(0xCC141414),
        searchField = Color(0x33FFFFFF),
        textPrimary = Color(0xFFF2FFFFFF),
        textSecondary = Color(0xFFB3FFFFFF),
        accent = Color(0xFF33B8FF),
        accentOn = Color(0xFF06141F),
        topBar = Color(0xFF296788),
        divider = Color(0x26FFFFFF),
        callGreen = Color(0xFF2ECC71),
        missedRed = Color(0xFFFF5A6E),
        incoming = Color(0xFF4FC3F7),
        outgoing = Color(0xFF66BB6A),
        settingsBackground = Color(0xE6101010),
    )

    val CrystalSimp = Crystal.copy(
        id = "crystal_simp",
        label = "Crystal Simp",
        description = "Flat translucent wallpaper, lighter scrim",
        surface = Color(0x4D000000),
        navBar = Color(0xCC0A0A0A),
        searchField = Color(0x26FFFFFF),
        topBar = Color(0xFF1E4E63),
        settingsBackground = Color(0xE60A0A0A),
    )

    val Lollipop = DialerTheme(
        id = "lollipop",
        label = "Lollipop",
        description = "Light cards, blue top bar, pink dialpad",
        useWallpaper = false,
        dark = false,
        background = Color(0xFFF2F4F7),
        surface = Color(0xFFFFFFFF),
        surfaceStrong = Color(0xFFFFFFFF),
        navBar = Color(0xFFFFFFFF),
        searchField = Color(0xFFFFFFFF),
        textPrimary = Color(0xFF1B1B1F),
        textSecondary = Color(0xFF6B7280),
        accent = Color(0xFF1976D2),
        accentOn = Color(0xFFFFFFFF),
        topBar = Color(0xFF1976D2),
        divider = Color(0xFFE3E6EA),
        callGreen = Color(0xFF43A047),
        missedRed = Color(0xFFE53935),
        incoming = Color(0xFF1E88E5),
        outgoing = Color(0xFF43A047),
        settingsBackground = Color(0xFFF2F4F7),
    )

    val Simplism = DialerTheme(
        id = "simplism",
        label = "Simplism",
        description = "Minimal light grey",
        useWallpaper = false,
        dark = false,
        background = Color(0xFFFAFAFA),
        surface = Color(0xFFFFFFFF),
        surfaceStrong = Color(0xFFFFFFFF),
        navBar = Color(0xFFFFFFFF),
        searchField = Color(0xFFF1F3F4),
        textPrimary = Color(0xFF202124),
        textSecondary = Color(0xFF5F6368),
        accent = Color(0xFF546E7A),
        accentOn = Color(0xFFFFFFFF),
        topBar = Color(0xFFFFFFFF),
        divider = Color(0xFFE8EAED),
        callGreen = Color(0xFF43A047),
        missedRed = Color(0xFFD93025),
        incoming = Color(0xFF1A73E8),
        outgoing = Color(0xFF43A047),
        settingsBackground = Color(0xFFFAFAFA),
    )

    val Classic = DialerTheme(
        id = "classic",
        label = "Classic",
        description = "White list, green call button, black text",
        useWallpaper = false,
        dark = false,
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        surfaceStrong = Color(0xFFFFFFFF),
        navBar = Color(0xFFFFFFFF),
        searchField = Color(0xFFF4F4F4),
        textPrimary = Color(0xFF1A1A1A),
        textSecondary = Color(0xFF757575),
        accent = Color(0xFF4CAF50),
        accentOn = Color(0xFFFFFFFF),
        topBar = Color(0xFFFFFFFF),
        divider = Color(0xFFE0E0E0),
        callGreen = Color(0xFF4CAF50),
        missedRed = Color(0xFFD32F2F),
        incoming = Color(0xFF2196F3),
        outgoing = Color(0xFF4CAF50),
        settingsBackground = Color(0xFFFFFFFF),
    )

    val all: List<DialerTheme> = listOf(Crystal, CrystalSimp, Lollipop, Simplism, Classic)

    fun byId(id: String): DialerTheme = all.firstOrNull { it.id == id } ?: Crystal
}

/**
 * Applies the user's accent override. Wallpaper themes also use it for the status-bar tint, so
 * Crystal's default blue can be replaced with any picked colour; other themes keep their own
 * top-bar treatment to preserve light/dark icon contrast.
 */
fun DialerTheme.withAccentOverride(argb: Int): DialerTheme {
    if (argb == 0) return this
    val color = Color(argb)
    return copy(
        accent = color,
        accentOn = if (color.luminance() > 0.55f) Color(0xFF06141F) else Color.White,
        topBar = if (useWallpaper) color else topBar,
    )
}

val LocalTextScale = staticCompositionLocalOf { 1f }
val LocalDialpadTextScale = staticCompositionLocalOf { 1f }

@Composable
fun NextDialerTheme(theme: DialerTheme, content: @Composable () -> Unit) {
    val colorScheme = if (theme.dark) {
        darkColorScheme(
            primary = theme.accent,
            onPrimary = theme.accentOn,
            secondary = theme.accent,
            onSecondary = theme.accentOn,
            background = theme.background,
            onBackground = theme.textPrimary,
            surface = theme.surface,
            onSurface = theme.textPrimary,
            surfaceVariant = theme.surfaceStrong,
            onSurfaceVariant = theme.textSecondary,
            outline = theme.divider,
            error = theme.missedRed,
            onError = Color.White,
        )
    } else {
        lightColorScheme(
            primary = theme.accent,
            onPrimary = theme.accentOn,
            secondary = theme.accent,
            onSecondary = theme.accentOn,
            background = theme.background,
            onBackground = theme.textPrimary,
            surface = theme.surface,
            onSurface = theme.textPrimary,
            surfaceVariant = theme.surfaceStrong,
            onSurfaceVariant = theme.textSecondary,
            outline = theme.divider,
            error = theme.missedRed,
            onError = Color.White,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content,
    )
}
