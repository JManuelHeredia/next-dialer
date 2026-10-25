package com.nextdialer.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextdialer.data.CallEntry
import com.nextdialer.data.ContactDetail
import com.nextdialer.data.ContactNumber
import com.nextdialer.data.DialerSettings
import com.nextdialer.ui.components.ContactAvatar
import com.nextdialer.ui.theme.DialerTheme
import com.nextdialer.util.DialerIntents
import com.nextdialer.util.Formatters
import com.nextdialer.util.PhoneUtils

/**
 * Per-number screen modelled on exDialer's contact detail: profile, quick actions, every stored
 * number, messaging-app shortcuts and a fixed CALL / SMS / overflow bar with the call total.
 * It deliberately avoids large app bars and cards-in-cards so it reads like a dialer utility.
 */
@Composable
fun ContactDetailScreen(
    number: String,
    contactId: Long?,
    settings: DialerSettings,
    theme: DialerTheme,
    calls: List<CallEntry>,
    loadDetail: suspend (String, Long?) -> ContactDetail,
    onBack: () -> Unit,
    onPlaceCall: (String) -> Unit,
    onViewCalls: () -> Unit,
    onDeleteHistory: (String) -> Unit,
) {
    val context = LocalContext.current
    val detail by produceState<ContactDetail?>(initialValue = null, number, contactId) {
        value = loadDetail(number, contactId)
    }

    val callCount = remember(calls, number) {
        calls.filter { PhoneUtils.numbersMatch(it.number, number) }.sumOf { it.count }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 132.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 6.dp, end = 10.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.textPrimary,
                    )
                }
                Spacer(Modifier.weight(1f))
                val value = detail
                if (value != null && value.contact.id > 0L) {
                    IconButton(onClick = {
                        DialerIntents.editContact(context, value.contact.id)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit contact",
                            tint = theme.textPrimary,
                        )
                    }
                }
            }

            val value = detail
            if (value == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = theme.accent)
                }
                return@Column
            }

            val contact = value.contact
            val known = contact.id > 0L
            val displayNumber = Formatters.formatPhone(
                context,
                number.ifBlank { contact.primaryNumber.orEmpty() },
                settings.formatNumber,
            )
            val title = contact.name
                .takeIf { it.isNotBlank() && it != number }
                ?: "Unknown"
            val numberEntry = contact.numbers.firstOrNull {
                PhoneUtils.numbersMatch(it.number, number)
            } ?: contact.numbers.firstOrNull()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ContactAvatar(
                    name = title,
                    photoUri = contact.photoUri,
                    size = 96.dp,
                    theme = theme,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (contact.starred) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        text = title,
                        color = theme.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
                Text(
                    text = displayNumber,
                    color = theme.textSecondary,
                    fontSize = 15.sp,
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    DetailAction(
                        icon = Icons.Filled.Call,
                        label = "Call",
                        theme = theme,
                        enabled = displayNumber.isNotBlank(),
                    ) {
                        onPlaceCall(number)
                    }
                    DetailAction(
                        icon = Icons.Filled.Message,
                        label = "Message",
                        theme = theme,
                        enabled = displayNumber.isNotBlank(),
                    ) {
                        DialerIntents.sms(context, number)
                    }
                    DetailAction(
                        icon = if (known) Icons.Filled.Edit else Icons.Filled.PersonAdd,
                        label = if (known) "Edit" else "Add",
                        theme = theme,
                    ) {
                        if (known) {
                            DialerIntents.editContact(context, contact.id)
                        } else {
                            DialerIntents.insertContact(context, number)
                        }
                    }
                    DetailAction(
                        icon = Icons.Filled.Share,
                        label = "Share",
                        theme = theme,
                    ) {
                        DialerIntents.shareContact(context, title, displayNumber)
                    }
                    if (settings.allowExternalViewer && known) {
                        DetailAction(
                            icon = Icons.Filled.OpenInNew,
                            label = "Open with",
                            theme = theme,
                        ) {
                            DialerIntents.openContactExternally(context, contact.id)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = theme.surface,
            ) {
                Column {
                    contact.numbers.forEachIndexed { index, entry ->
                        NumberRow(
                            entry = entry,
                            highlighted = numberEntry != null &&
                                PhoneUtils.numbersMatch(entry.number, numberEntry.number),
                            theme = theme,
                            onCall = { onPlaceCall(entry.number) },
                            onMessage = { DialerIntents.sms(context, entry.number) },
                        )
                        if (index != contact.numbers.lastIndex) {
                            HorizontalDivider(color = theme.divider, thickness = 0.6.dp)
                        }
                    }
                }
            }

            val whatsApps = DialerIntents.whatsAppPackages(context)
            val telegram = DialerIntents.telegramInstalled(context)
            if (whatsApps.isNotEmpty() || telegram) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = theme.surface,
                ) {
                    Column {
                        val apps = buildList {
                            whatsApps.forEach { pkg ->
                                add(
                                    Triple(
                                        if (pkg == "com.whatsapp.w4b") {
                                            "WhatsApp Business"
                                        } else {
                                            "WhatsApp"
                                        },
                                        "W",
                                        Color(0xFF25D366),
                                    ) to pkg,
                                )
                            }
                        }
                        apps.forEach { (style, pkg) ->
                            AppActionRow(
                                letter = style.second,
                                brand = style.third,
                                title = style.first,
                                subtitle = displayNumber,
                                theme = theme,
                            ) {
                                DialerIntents.whatsAppChat(context, number, pkg)
                            }
                            HorizontalDivider(color = theme.divider, thickness = 0.6.dp)
                        }
                        if (telegram) {
                            AppActionRow(
                                letter = "T",
                                brand = Color(0xFF229ED9),
                                title = "Telegram",
                                subtitle = displayNumber,
                                theme = theme,
                            ) {
                                DialerIntents.telegramChat(context, number)
                            }
                        }
                    }
                }
            }

            if (value.emails.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = theme.surface,
                ) {
                    Column {
                        value.emails.forEachIndexed { index, email ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { DialerIntents.email(context, email) }
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = null,
                                    tint = theme.textSecondary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = email,
                                    color = theme.textPrimary,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (index != value.emails.lastIndex) {
                                HorizontalDivider(color = theme.divider, thickness = 0.6.dp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
        }

        DetailBottomBar(
            theme = theme,
            callCount = callCount,
            knownContact = detail?.contact?.id?.let { it > 0L } == true,
            onViewCalls = onViewCalls,
            onCall = { onPlaceCall(number) },
            onMessage = { DialerIntents.sms(context, number) },
            onEdit = {
                val id = detail?.contact?.id ?: 0L
                if (id > 0L) {
                    DialerIntents.editContact(context, id)
                } else {
                    DialerIntents.insertContact(context, number)
                }
            },
            onShare = {
                DialerIntents.shareContact(
                    context,
                    detail?.contact?.name ?: number,
                    number,
                )
            },
            onCopy = {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Phone number", number))
                Toast.makeText(context, "Number copied", Toast.LENGTH_SHORT).show()
            },
            onDeleteHistory = { onDeleteHistory(number) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun NumberRow(
    entry: ContactNumber,
    highlighted: Boolean,
    theme: DialerTheme,
    onCall: () -> Unit,
    onMessage: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.number,
                color = if (highlighted) theme.accent else theme.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = entry.typeLabel,
                color = theme.textSecondary,
                fontSize = 12.sp,
            )
        }
        IconButton(onClick = onMessage) {
            Icon(
                imageVector = Icons.Filled.Message,
                contentDescription = "Message ${entry.number}",
                tint = theme.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = onCall) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = "Call ${entry.number}",
                tint = theme.accent,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}

@Composable
private fun AppActionRow(
    letter: String,
    brand: Color,
    title: String,
    subtitle: String,
    theme: DialerTheme,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(brand, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = letter,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = theme.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.Message,
            contentDescription = null,
            tint = theme.textSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun DetailBottomBar(
    theme: DialerTheme,
    callCount: Int,
    knownContact: Boolean,
    onViewCalls: () -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onDeleteHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.navBar.copy(alpha = if (theme.useWallpaper) 0.88f else 1f))
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = theme.divider, thickness = 0.6.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onViewCalls)
                .height(34.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (callCount == 0) {
                    "NO CALLS YET"
                } else {
                    "$callCount ${if (callCount == 1) "CALL" else "CALLS"} TOTAL"
                },
                color = if (callCount == 0) theme.textSecondary else theme.accent,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.3.sp,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                .height(54.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DetailBarAction(
                icon = Icons.Filled.Call,
                label = "CALL",
                theme = theme,
                modifier = Modifier.weight(1f),
                onClick = onCall,
            )
            DetailBarAction(
                icon = Icons.Filled.Message,
                label = "SMS",
                theme = theme,
                modifier = Modifier.weight(1f),
                onClick = onMessage,
            )
            Box(modifier = Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = { menu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More options",
                        tint = theme.textSecondary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                DropdownMenu(
                    expanded = menu,
                    onDismissRequest = { menu = false },
                    containerColor = theme.surfaceStrong,
                ) {
                    DropdownMenuItem(
                        text = { Text("View call history", color = theme.textPrimary) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.History,
                                contentDescription = null,
                                tint = theme.textSecondary,
                            )
                        },
                        onClick = {
                            menu = false
                            onViewCalls()
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (knownContact) "Edit contact" else "Add contact",
                                color = theme.textPrimary,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (knownContact) Icons.Filled.Edit else Icons.Filled.PersonAdd,
                                contentDescription = null,
                                tint = theme.textSecondary,
                            )
                        },
                        onClick = {
                            menu = false
                            onEdit()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Share contact", color = theme.textPrimary) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = null,
                                tint = theme.textSecondary,
                            )
                        },
                        onClick = {
                            menu = false
                            onShare()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Copy number", color = theme.textPrimary) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.ContentCopy,
                                contentDescription = null,
                                tint = theme.textSecondary,
                            )
                        },
                        onClick = {
                            menu = false
                            onCopy()
                        },
                    )
                    HorizontalDivider(color = theme.divider)
                    DropdownMenuItem(
                        text = { Text("Delete call history", color = theme.missedRed) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = theme.missedRed,
                            )
                        },
                        onClick = {
                            menu = false
                            onDeleteHistory()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailBarAction(
    icon: ImageVector,
    label: String,
    theme: DialerTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = theme.accent,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            color = theme.accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun DetailAction(
    icon: ImageVector,
    label: String,
    theme: DialerTheme,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    if (enabled) theme.accent.copy(alpha = 0.16f)
                    else theme.divider.copy(alpha = 0.4f),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) theme.accent else theme.textSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = label,
            color = if (enabled) theme.textSecondary else theme.textSecondary.copy(alpha = 0.5f),
            fontSize = 11.5.sp,
        )
    }
}
