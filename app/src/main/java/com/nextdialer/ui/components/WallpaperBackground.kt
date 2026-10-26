package com.nextdialer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.nextdialer.ui.theme.DialerTheme

/**
 * Draws the scrim over the system wallpaper (which is shown by the window itself with
 * FLAG_SHOW_WALLPAPER) or an opaque background for the flat themes. The wallpaper is never
 * read into a bitmap, so no storage permission is needed on any Android version.
 */
@Composable
fun WallpaperBackground(
    theme: DialerTheme,
    dimPercent: Int,
    tintSystemBars: Boolean,
    fullScreen: Boolean,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (theme.useWallpaper) {
            val dim = (dimPercent / 100f).coerceIn(0f, 0.85f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dim)),
            )
            // Extra shade behind the screen headers so bright wallpapers do not wash out
            // the title, the search field, or the first list rows.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.42f),
                            0.34f to Color.Transparent,
                        ),
                    ),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(theme.background),
            )
        }

        if (tintSystemBars && !fullScreen && !theme.useWallpaper) {
            val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topInset)
                    .background(theme.topBar),
            )
        }
    }
}
