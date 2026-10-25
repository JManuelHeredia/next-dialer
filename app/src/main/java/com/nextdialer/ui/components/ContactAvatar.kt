package com.nextdialer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.nextdialer.ui.theme.DialerTheme
import com.nextdialer.util.PhotoLoader

private val avatarColors = listOf(
    Color(0xFFEF6C00),
    Color(0xFF6A1B9A),
    Color(0xFF00897B),
    Color(0xFF3949AB),
    Color(0xFFD81B60),
    Color(0xFF8E24AA),
    Color(0xFF00ACC1),
    Color(0xFF5E35B1),
    Color(0xFF7CB342),
    Color(0xFFF4511E),
)

fun avatarColor(name: String): Color {
    val index = (name.hashCode() and 0x7fffffff) % avatarColors.size
    return avatarColors[index]
}

@Composable
fun ContactAvatar(
    name: String,
    photoUri: String?,
    size: Dp,
    theme: DialerTheme,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, photoUri) {
        value = PhotoLoader.load(context, photoUri)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(avatarColor(name)),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "#",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value * 0.42f).sp,
            )
        }
    }
}
