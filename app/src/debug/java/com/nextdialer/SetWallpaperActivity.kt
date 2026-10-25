package com.nextdialer

import android.app.Activity
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Bundle
import android.widget.Toast

/**
 * Debug-only helper used by the emulator test scripts to install a wallpaper without the
 * system wallpaper cropper (which is broken on the API 28 AOSP image). Not present in release
 * builds.
 */
class SetWallpaperActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            val width = 1080
            val height = 1920
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.shader = LinearGradient(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                Color.rgb(28, 84, 140),
                Color.rgb(190, 70, 140),
                Shader.TileMode.CLAMP,
            )
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.shader = null

            val circles = listOf(
                Triple(140, 280, 420) to Color.argb(170, 255, 220, 90),
                Triple(760, 620, 520) to Color.argb(150, 80, 230, 190),
                Triple(280, 1280, 480) to Color.argb(160, 255, 120, 80),
                Triple(900, 1560, 380) to Color.argb(140, 120, 160, 255),
            )
            circles.forEach { (circle, color) ->
                val (x, y, radius) = circle
                paint.color = color
                canvas.drawCircle(x.toFloat(), y.toFloat(), radius.toFloat(), paint)
            }

            WallpaperManager.getInstance(this).setBitmap(bitmap)
            bitmap.recycle()
        }.onSuccess {
            Toast.makeText(this, "Wallpaper set", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
        }
        finish()
    }
}
