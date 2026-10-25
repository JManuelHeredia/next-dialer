package com.nextdialer

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.nextdialer.data.DialerSettings
import com.nextdialer.ui.AppRoot
import com.nextdialer.ui.theme.DialerTheme
import com.nextdialer.ui.theme.DialerThemes
import com.nextdialer.ui.theme.withAccentOverride
import com.nextdialer.ui.theme.LocalDialpadTextScale
import com.nextdialer.ui.theme.LocalTextScale
import com.nextdialer.ui.theme.NextDialerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.refreshPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
        }

        val store = (application as NextDialerApp).settings
        val initialSettings = store.state.value
        applyWindowAppearance(
            theme = DialerThemes.byId(initialSettings.themeId),
            settings = initialSettings,
        )

        setContent {
            val settings by store.state.collectAsState()
            val theme = DialerThemes.byId(settings.themeId)
                .withAccentOverride(settings.accentColor)
            val context = LocalContext.current

            SideEffect {
                requestedOrientation = if (settings.screenRotation) {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }

                val activity = context as? Activity ?: return@SideEffect
                val window = activity.window
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                if (settings.fullScreen) {
                    controller.hide(WindowInsetsCompat.Type.statusBars())
                } else {
                    controller.show(WindowInsetsCompat.Type.statusBars())
                }
                controller.isAppearanceLightStatusBars = !theme.dark
                controller.isAppearanceLightNavigationBars = !theme.dark
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_DEFAULT

                // The wallpaper themes show the real system wallpaper through a genuinely
                // translucent window instead of reading it: WallpaperManager.getDrawable()
                // needs READ_EXTERNAL_STORAGE on Android 12 and is unavailable to normal apps
                // on Android 14+. Blur uses the platform's cross-window blur (Android 12+).
                applyWindowAppearance(theme, settings)
            }

            val textScale = when (settings.textSize) {
                1 -> 1.08f
                2 -> 1.18f
                else -> 1f
            }
            val dialpadScale = when (settings.dialpadTextSize) {
                1 -> 1.12f
                2 -> 1.24f
                else -> 1f
            }

            CompositionLocalProvider(
                LocalTextScale provides textScale,
                LocalDialpadTextScale provides dialpadScale,
            ) {
                NextDialerTheme(theme) {
                    AppRoot(
                        settings = settings,
                        store = store,
                        theme = theme,
                        viewModel = viewModel,
                        onRequestPermissions = {
                            store.setOnboardingDone(true)
                            val permissions = buildList {
                                add(Manifest.permission.READ_CONTACTS)
                                add(Manifest.permission.READ_CALL_LOG)
                                add(Manifest.permission.WRITE_CALL_LOG)
                                add(Manifest.permission.CALL_PHONE)
                                if (Build.VERSION.SDK_INT >= 33) {
                                    add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                            permissionLauncher.launch(permissions.toTypedArray())
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }

    /**
     * Keeps the window itself transparent for wallpaper themes so the system wallpaper is a
     * separate, stable layer behind the app. Flat themes get an opaque window background, which
     * also prevents the previous screen from showing through while Compose swaps routes.
     */
    private fun applyWindowAppearance(theme: DialerTheme, settings: DialerSettings) {
        if (theme.useWallpaper) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
            window.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
            window.setBackgroundDrawable(ColorDrawable(theme.background.toArgb()))
        }

        if (Build.VERSION.SDK_INT >= 31) {
            val attrs = window.attributes
            val radius = if (theme.useWallpaper) {
                (settings.wallpaperBlur * resources.displayMetrics.density).toInt()
            } else {
                0
            }
            attrs.flags = if (radius > 0) {
                attrs.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            } else {
                attrs.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
            }
            attrs.blurBehindRadius = radius
            runCatching { window.attributes = attrs }
        }
    }
}
