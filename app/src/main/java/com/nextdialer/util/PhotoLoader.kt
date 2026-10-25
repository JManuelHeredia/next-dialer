package com.nextdialer.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PhotoLoader {

    private val cache = object : LruCache<String, Bitmap>(64) {
        override fun sizeOf(key: String, value: Bitmap): Int = 1
    }

    suspend fun load(context: Context, uri: String?): ImageBitmap? = withContext(Dispatchers.IO) {
        if (uri.isNullOrBlank()) return@withContext null
        cache.get(uri)?.let { return@withContext it.asImageBitmap() }

        runCatching {
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                val options = BitmapFactory.Options().apply { inSampleSize = 2 }
                val bitmap = BitmapFactory.decodeStream(stream, null, options) ?: return@use null
                cache.put(uri, bitmap)
                bitmap.asImageBitmap()
            }
        }.getOrNull()
    }
}
