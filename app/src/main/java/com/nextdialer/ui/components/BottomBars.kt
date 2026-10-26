package com.nextdialer.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextdialer.ui.theme.DialerTheme

/**
 * Keypad bar with five evenly spaced slots: paste, contacts, the green call button, backspace
 * and overflow. Equal slots keep the call button centred and give the bar a symmetric rhythm.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialpadBottomBar(
    theme: DialerTheme,
    canCall: Boolean,
    canDelete: Boolean,
    pasteAvailable: Boolean,
    missedCount: Int,
    /** False when the keypad is collapsed so the centre slot becomes a SHOW DIALPAD action. */
    dialpadVisible: Boolean,
    onPaste: () -> Unit,
    onContacts: () -> Unit,
    onCallHistory: () -> Unit,
    onShowDialpad: () -> Unit,
    onCall: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onMessage: (() -> Unit)?,
    onAddContact: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(92.dp)
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                IconButton(onClick = onPaste, enabled = pasteAvailable) {
                    Icon(
                        imageVector = Icons.Filled.ContentPaste,
                        contentDescription = "Paste from clipboard",
                        tint = if (pasteAvailable) {
                            theme.textSecondary
                        } else {
                            theme.textSecondary.copy(alpha = 0.32f)
                        },
                        modifier = Modifier.size(23.dp),
                    )
                }
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                IconButton(onClick = onContacts) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Contacts",
                        tint = theme.textSecondary,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }

            Box(modifier = Modifier.weight(2f), contentAlignment = Alignment.Center) {
                if (dialpadVisible) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                if (canCall) {
                                    theme.callGreen
                                } else {
                                    theme.callGreen.copy(alpha = 0.45f)
                                },
                            )
                            .clickable(enabled = canCall, onClick = onCall),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Call",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onShowDialpad)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Dialpad,
                            contentDescription = null,
                            tint = theme.accent,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "SHOW DIALPAD",
                            color = theme.accent,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.7.sp,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        enabled = canDelete,
                        onClick = onBackspace,
                        onLongClick = onClear,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    tint = if (canDelete) {
                        theme.textSecondary
                    } else {
                        theme.textSecondary.copy(alpha = 0.35f)
                    },
                    modifier = Modifier.size(23.dp),
                )
            }
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            MainOverflowButton(
                theme = theme,
                missedCount = missedCount,
                showCallHistory = true,
                showContacts = true,
                onCallHistory = onCallHistory,
                onContacts = onContacts,
                onAddContact = onAddContact,
                onMessage = onMessage,
                onSettings = onSettings,
            )
        }
    }
}
}

/**
 * Original-style list bar: contacts shortcut, SHOW DIALPAD and overflow. It is shared by the
 * call log and contacts lists; the shortcut adds a contact while the contacts list is open.
 */
@Composable
fun ListBottomBar(
    theme: DialerTheme,
    contactsMode: Boolean,
    missedCount: Int,
    onShortcut: () -> Unit,
    onShowDialpad: () -> Unit,
    onCallHistory: () -> Unit,
    onContacts: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.navBar.copy(alpha = if (theme.useWallpaper) 0.82f else 1f))
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = theme.divider, thickness = 0.6.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp),
        ) {
            IconButton(
                onClick = onShortcut,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                Icon(
                    imageVector = if (contactsMode) {
                        Icons.Filled.PersonAdd
                    } else {
                        Icons.Filled.Person
                    },
                    contentDescription = if (contactsMode) "Add contact" else "Contacts",
                    tint = theme.textSecondary,
                    modifier = Modifier.size(24.dp),
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onShowDialpad)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Dialpad,
                    contentDescription = null,
                    tint = theme.accent,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "SHOW DIALPAD",
                    color = theme.accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.7.sp,
                    maxLines = 1,
                    softWrap = false,
                )
            }

            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                MainOverflowButton(
                    theme = theme,
                    missedCount = missedCount,
                    showCallHistory = contactsMode,
                    showContacts = !contactsMode,
                    onCallHistory = onCallHistory,
                    onContacts = onContacts,
                    onAddContact = onShortcut,
                    onMessage = null,
                    onSettings = onSettings,
                )
            }
        }
    }
}

@Composable
fun MainOverflowButton(
    theme: DialerTheme,
    missedCount: Int,
    showCallHistory: Boolean,
    showContacts: Boolean,
    onCallHistory: () -> Unit,
    onContacts: () -> Unit,
    onAddContact: () -> Unit,
    onMessage: (() -> Unit)?,
    onSettings: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More options",
                tint = theme.textSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = theme.surfaceStrong,
        ) {
            if (showCallHistory) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (missedCount > 0) {
                                "Call history · $missedCount missed"
                            } else {
                                "Call history"
                            },
                            color = theme.textPrimary,
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.History, contentDescription = null, tint = theme.textSecondary)
                    },
                    onClick = {
                        expanded = false
                        onCallHistory()
                    },
                )
            }
            if (showContacts) {
                DropdownMenuItem(
                    text = { Text("Contacts", color = theme.textPrimary) },
                    leadingIcon = {
                        Icon(Icons.Filled.Contacts, contentDescription = null, tint = theme.textSecondary)
                    },
                    onClick = {
                        expanded = false
                        onContacts()
                    },
                )
            }
            if (onMessage != null) {
                DropdownMenuItem(
                    text = { Text("Send message", color = theme.textPrimary) },
                    leadingIcon = {
                        Icon(Icons.Filled.Message, contentDescription = null, tint = theme.textSecondary)
                    },
                    onClick = {
                        expanded = false
                        onMessage()
                    },
                )
            }
            DropdownMenuItem(
                text = { Text("Add contact", color = theme.textPrimary) },
                leadingIcon = {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = theme.textSecondary)
                },
                onClick = {
                    expanded = false
                    onAddContact()
                },
            )
            HorizontalDivider(color = theme.divider)
            DropdownMenuItem(
                text = { Text("Settings", color = theme.textPrimary) },
                leadingIcon = {
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = theme.textSecondary)
                },
                onClick = {
                    expanded = false
                    onSettings()
                },
            )
        }
    }
}
