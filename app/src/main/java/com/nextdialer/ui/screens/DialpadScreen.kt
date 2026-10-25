package com.nextdialer.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nextdialer.data.CallEntry
import com.nextdialer.data.Contact
import com.nextdialer.data.DialerSettings
import com.nextdialer.data.SwipeAction
import com.nextdialer.ui.components.CallTypeIcon
import com.nextdialer.ui.components.ContactAvatar
import com.nextdialer.ui.components.DialpadBottomBar
import com.nextdialer.ui.components.PermissionBanner
import com.nextdialer.ui.components.SwipeActionRow
import com.nextdialer.ui.components.callKindColor
import com.nextdialer.ui.theme.DialerTheme
import com.nextdialer.ui.theme.LocalDialpadTextScale
import com.nextdialer.ui.theme.LocalTextScale
import com.nextdialer.util.DialerIntents
import com.nextdialer.util.Formatters
import com.nextdialer.util.PhoneUtils

private data class DialKey(
    val digit: String,
    val letters: String,
)

private val dialKeys = listOf(
    DialKey("1", ""),
    DialKey("2", "ABC"),
    DialKey("3", "DEF"),
    DialKey("4", "GHI"),
    DialKey("5", "JKL"),
    DialKey("6", "MNO"),
    DialKey("7", "PQRS"),
    DialKey("8", "TUV"),
    DialKey("9", "WXYZ"),
    DialKey("*", ""),
    DialKey("0", "+"),
    DialKey("#", ""),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialpadScreen(
    settings: DialerSettings,
    theme: DialerTheme,
    contacts: List<Contact>,
    calls: List<CallEntry>,
    contactsGranted: Boolean,
    callLogGranted: Boolean,
    onPlaceCall: (String) -> Unit,
    onTapContact: (Contact) -> Unit,
    onTapCall: (CallEntry) -> Unit,
    onSwipeAction: (SwipeAction, CallEntry) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenRecents: () -> Unit,
    missedCount: Int,
    onRequestPermissions: () -> Unit,
) {
    val context = LocalContext.current
    val textScale = LocalTextScale.current
    val digitScale = LocalDialpadTextScale.current

    var number by rememberSaveable { mutableStateOf("") }
    var clipboardNumber by remember { mutableStateOf<String?>(null) }

    fun readClipboardNumber(): String? {
        if (!settings.checkClipboard) return null
        return runCatching {
            val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = manager.primaryClip
                ?.takeIf { it.itemCount > 0 }
                ?.getItemAt(0)
                ?.coerceToText(context)
                ?.toString()
                .orEmpty()
            if (PhoneUtils.looksLikePhone(text)) text.trim() else null
        }.getOrNull()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, settings.checkClipboard) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) clipboardNumber = readClipboardNumber()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        clipboardNumber = readClipboardNumber()
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val toneGenerator = remember {
        runCatching { ToneGenerator(AudioManager.STREAM_DTMF, 70) }.getOrNull()
    }
    DisposableEffect(Unit) {
        onDispose { runCatching { toneGenerator?.release() } }
    }

    val vibrator = remember {
        runCatching { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
            .getOrNull()
    }

    fun feedback(digit: String) {
        if (settings.touchTones) {
            val tone = when (digit) {
                "*" -> ToneGenerator.TONE_DTMF_S
                "#" -> ToneGenerator.TONE_DTMF_P
                else -> ToneGenerator.TONE_DTMF_0 + (digit.firstOrNull()?.digitToIntOrNull() ?: 0)
            }
            runCatching { toneGenerator?.startTone(tone, 120) }
        }
        if (settings.haptic) {
            val millis = when (settings.hapticStrength) {
                -1 -> 8L
                1 -> 24L
                2 -> 40L
                else -> 14L
            }
            runCatching {
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(
                            millis,
                            VibrationEffect.DEFAULT_AMPLITUDE,
                        ),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(millis)
                }
            }
        }
    }

    fun append(value: String) {
        if (number.length >= 40) return
        number += value
        feedback(value)
    }

    fun removeLast() {
        if (number.isNotEmpty()) {
            number = number.dropLast(1)
            feedback("0")
        }
    }

    fun callTyped() {
        val speedDialTarget = if (settings.shortNumberSpeedDial && number.length == 1) {
            number.toIntOrNull()
                ?.takeIf { it in 2..9 }
                ?.let { settings.speedDial[it] }
        } else {
            null
        }
        onPlaceCall(speedDialTarget ?: number)
    }

    val digits = PhoneUtils.digits(number)
    val matches = remember(contacts, digits) {
        if (digits.isEmpty()) {
            emptyList()
        } else {
            contacts.filter { contact ->
                PhoneUtils.t9ForName(contact.name).contains(digits) ||
                    contact.numbers.any { it.normalized.contains(digits) }
            }.take(40)
        }
    }

    val display = if (settings.formatNumber && number.none { it == '*' || it == '#' }) {
        Formatters.formatPhone(context, number, true)
    } else {
        number
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!contactsGranted || !callLogGranted) {
            PermissionBanner(
                message = buildString {
                    append("Allow access to ")
                    append(
                        buildList {
                            if (!contactsGranted) add("contacts")
                            if (!callLogGranted) add("call history")
                        }.joinToString(" and "),
                    )
                    append(" for name lookup and recent calls.")
                },
                theme = theme,
                onGrant = onRequestPermissions,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (number.isNotEmpty()) {
                Text(
                    text = display,
                    color = theme.textPrimary,
                    fontSize = (30f * textScale).sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (number.isNotEmpty() && matches.isEmpty() && contactsGranted) {
                Text(
                    text = "No matching contact",
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                )
            }
            val clip = clipboardNumber
            if (clip != null && number.isEmpty() && settings.checkClipboard) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = theme.accent.copy(alpha = 0.16f),
                    modifier = Modifier.clickable {
                        number = PhoneUtils.digits(clip)
                        clipboardNumber = null
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentPaste,
                            contentDescription = null,
                            tint = theme.accent,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Paste $clip",
                            color = theme.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        if (number.isEmpty()) {
            RecentCallList(
                calls = calls,
                settings = settings,
                theme = theme,
                callLogGranted = callLogGranted,
                onTap = onTapCall,
                onSwipeAction = onSwipeAction,
                modifier = Modifier.weight(1f),
            )
        } else {
            MatchList(
                matches = matches,
                settings = settings,
                theme = theme,
                onTap = onTapContact,
                modifier = Modifier.weight(1f),
            )
        }

        val keyHeight = (56 * when (settings.dialpadSize) {
            1 -> 1.1f
            2 -> 1.2f
            else -> 1f
        }).dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(
                    theme.surface.copy(alpha = if (theme.useWallpaper) 0.78f else 1f),
                )
                .padding(top = 10.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                dialKeys.chunked(3).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEach { key ->
                            DialKeyButton(
                                key = key,
                                theme = theme,
                                height = keyHeight,
                                settings = settings,
                                digitScale = digitScale,
                                modifier = Modifier.weight(1f),
                                onClick = { append(key.digit) },
                                onLongClick = {
                                    when (key.digit) {
                                        "0" -> {
                                            if (number.isEmpty()) {
                                                number = "+"
                                            } else {
                                                number += "+"
                                            }
                                            feedback("0")
                                        }
                                        in "2".."9" -> {
                                            val target = settings.speedDial[key.digit.toInt()]
                                            if (target.isNullOrBlank()) {
                                                Toast.makeText(
                                                    context,
                                                    "No speed dial for ${key.digit}",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            } else {
                                                onPlaceCall(target)
                                            }
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }

            DialpadBottomBar(
                theme = theme,
                canCall = number.isNotEmpty(),
                canDelete = number.isNotEmpty(),
                pasteAvailable = settings.checkClipboard && clipboardNumber != null,
                missedCount = missedCount,
                onPaste = {
                    val pasted = readClipboardNumber() ?: clipboardNumber
                    if (pasted != null) {
                        number = PhoneUtils.digits(pasted)
                        clipboardNumber = pasted
                    }
                },
                onContacts = onOpenContacts,
                onCallHistory = onOpenRecents,
                onCall = { if (number.isNotEmpty()) callTyped() },
                onBackspace = { removeLast() },
                onClear = { number = "" },
                onMessage = if (number.isNotEmpty()) {
                    { DialerIntents.sms(context, number) }
                } else {
                    null
                },
                onAddContact = { DialerIntents.insertContact(context) },
                onSettings = onOpenSettings,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DialKeyButton(
    key: DialKey,
    theme: DialerTheme,
    height: androidx.compose.ui.unit.Dp,
    settings: DialerSettings,
    digitScale: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(height)
            .padding(4.dp)
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center,
    ) {
        val digitColor = if (theme.useWallpaper) theme.accent else theme.textPrimary
        val digitWidth = 24.dp
        val labelWidth = 38.dp

        if (!settings.displayAbc) {
            Text(
                text = key.digit,
                color = digitColor,
                fontSize = (26f * digitScale).sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
            )
        } else if (settings.abcAtRight) {
            // Reserve the same digit and label slots on every key. The 4-character labels
            // (PQRS/WXYZ) and the empty labels (1/*/#) then keep all digits on one x axis.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.width(digitWidth),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = key.digit,
                        color = digitColor,
                        fontSize = (26f * digitScale).sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
                Spacer(Modifier.width(3.dp))
                Box(
                    modifier = Modifier.width(labelWidth),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = key.letters,
                        color = theme.textSecondary,
                        fontSize = (9f * digitScale).sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.6.sp,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = key.digit,
                    color = digitColor,
                    fontSize = (26f * digitScale).sp,
                    fontWeight = FontWeight.Medium,
                )
                // Keep the label line even on 1/*/# so digits share the same baseline.
                Text(
                    text = key.letters.ifEmpty { " " },
                    color = theme.textSecondary.copy(alpha = if (key.letters.isEmpty()) 0f else 1f),
                    fontSize = (9f * digitScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                )
            }
        }
    }
}

@Composable
private fun MatchList(
    matches: List<Contact>,
    settings: DialerSettings,
    theme: DialerTheme,
    onTap: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (matches.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "Keep typing to search contacts",
                color = theme.textSecondary,
                fontSize = 13.sp,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
    ) {
        items(matches, key = { it.id }) { contact ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onTap(contact) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ContactAvatar(
                    name = contact.name,
                    photoUri = contact.photoUri,
                    size = 38.dp,
                    theme = theme,
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        color = theme.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = contact.numbers.firstOrNull()?.number.orEmpty(),
                        color = theme.textSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = theme.textSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun RecentCallList(
    calls: List<CallEntry>,
    settings: DialerSettings,
    theme: DialerTheme,
    callLogGranted: Boolean,
    onTap: (CallEntry) -> Unit,
    onSwipeAction: (SwipeAction, CallEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!callLogGranted || calls.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = if (callLogGranted) {
                    "Dial a number or search contacts"
                } else {
                    "Allow call history to see recent calls here"
                },
                color = theme.textSecondary,
                fontSize = 13.sp,
            )
        }
        return
    }

    val context = LocalContext.current
    Column(modifier = modifier.fillMaxWidth()) {
        // Fixed legend: it must stay visible while the call list scrolls.
        Text(
            text = "RECENT CALLS",
            color = theme.textSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 22.dp, top = 6.dp, bottom = 4.dp),
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
        ) {
            items(calls.take(14), key = { it.id }) { call ->
            val name = call.displayName
                ?: Formatters.formatPhone(context, call.number, settings.formatNumber)
            val color = callKindColor(call.kind, theme, settings.colorCalls)
            SwipeActionRow(
                leftAction = if (settings.swipeEnabled) settings.swipeLeftDialer else SwipeAction.NONE,
                rightAction = if (settings.swipeEnabled) settings.swipeRightDialer else SwipeAction.NONE,
                enabled = settings.swipeEnabled,
                theme = theme,
                onAction = { action -> onSwipeAction(action, call) },
                modifier = Modifier
                    .padding(vertical = 1.dp)
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            theme.surface.copy(
                                alpha = if (theme.useWallpaper) 0.62f else 1f,
                            ),
                        )
                        .clickable { onTap(call) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ContactAvatar(
                        name = name,
                        photoUri = call.photoUri,
                        size = 36.dp,
                        theme = theme,
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = name,
                            color = if (call.kind == com.nextdialer.data.CallKind.MISSED) {
                                theme.missedRed
                            } else {
                                theme.textPrimary
                            },
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CallTypeIcon(kind = call.kind, color = color)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = Formatters.callTimeLabel(
                                    context,
                                    call.date,
                                    settings.relativeTime,
                                    settings.displayTime,
                                ),
                                color = theme.textSecondary,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}
}
