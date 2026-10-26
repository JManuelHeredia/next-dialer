package com.nextdialer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextdialer.data.CallEntry
import com.nextdialer.data.CallKind
import com.nextdialer.data.DialerSettings
import com.nextdialer.data.SwipeAction
import com.nextdialer.data.TapAction
import com.nextdialer.ui.components.CallRowBody
import com.nextdialer.ui.components.CallRowActionsDialog
import com.nextdialer.ui.components.CallTypeIcon
import com.nextdialer.ui.components.ContactAvatar
import com.nextdialer.ui.components.DayHeader
import com.nextdialer.ui.components.EmptyState
import com.nextdialer.ui.components.ListBottomBar
import com.nextdialer.ui.components.PermissionBanner
import com.nextdialer.ui.components.SearchHeader
import com.nextdialer.ui.components.SwipeActionRow
import com.nextdialer.ui.components.callKindColor
import com.nextdialer.ui.theme.DialerTheme
import com.nextdialer.ui.theme.LocalTextScale
import com.nextdialer.util.Formatters
import com.nextdialer.util.PhoneUtils

@Composable
fun RecentsScreen(
    calls: List<CallEntry>,
    settings: DialerSettings,
    theme: DialerTheme,
    contactsGranted: Boolean,
    callLogGranted: Boolean,
    onRequestPermissions: () -> Unit,
    initialQuery: String? = null,
    onInitialQueryHandled: () -> Unit = {},
    summary: String? = null,
    onTap: (CallEntry) -> Unit,
    onSwipeAction: (SwipeAction, CallEntry) -> Unit,
    onDelete: (CallEntry) -> Unit,
    onOpenKeypad: () -> Unit,
    onOpenContacts: () -> Unit,
    missedCount: Int,
    onSettings: () -> Unit,
    allowSwipe: Boolean = true,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<CallEntry?>(null) }
    var actionCall by remember { mutableStateOf<CallEntry?>(null) }

    LaunchedEffect(initialQuery) {
        if (initialQuery != null) {
            query = initialQuery
            onInitialQueryHandled()
        }
    }

    val filtered = remember(calls, query) {
        if (query.isBlank()) {
            calls
        } else {
            calls.filter { call ->
                val name = call.displayName.orEmpty()
                val digits = PhoneUtils.digits(query)
                name.contains(query, ignoreCase = true) ||
                    call.number.contains(query, ignoreCase = true) ||
                    (digits.isNotEmpty() &&
                        PhoneUtils.normalize(call.number).contains(digits)) ||
                    (digits.isNotEmpty() && PhoneUtils.t9ForName(name).contains(digits))
            }
        }
    }

    val ordered = remember(filtered, settings.reverseCallLog) {
        if (settings.reverseCallLog) filtered.asReversed() else filtered
    }

    val groups = remember(ordered) {
        ordered.groupBy { Formatters.dayLabel(it.date) }
    }

    val missing = buildList {
        if (!contactsGranted) add("contacts")
        if (!callLogGranted) add("call history")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchHeader(
            query = query,
            onQueryChange = { query = it },
            placeholder = "Search call history",
            theme = theme,
        )

        if (missing.isNotEmpty()) {
            PermissionBanner(
                message = "Allow access to ${missing.joinToString(" and ")} to show names and recent calls.",
                theme = theme,
                onGrant = onRequestPermissions,
            )
        }

        if (summary != null) {
            Text(
                text = summary,
                color = theme.accent,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp),
            )
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    !callLogGranted -> EmptyState(
                        icon = Icons.Filled.History,
                        title = "Call history access needed",
                        message = "Grant call-log access to see recent calls and call back with one tap.",
                        theme = theme,
                        actionLabel = "Grant access",
                        onAction = onRequestPermissions,
                    )
                    query.isNotBlank() -> EmptyState(
                        icon = Icons.Filled.Search,
                        title = "No matches",
                        message = "No calls match \"$query\".",
                        theme = theme,
                    )
                    else -> EmptyState(
                        icon = Icons.Filled.History,
                        title = "No recent calls",
                        message = "Calls you make and receive will appear here.",
                        theme = theme,
                        actionLabel = "Open keypad",
                        onAction = onOpenKeypad,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp),
            ) {
                groups.forEach { (label, dayCalls) ->
                    item(key = "header_$label") {
                        DayHeader(label = label, theme = theme)
                    }
                    items(dayCalls, key = { it.id }) { call ->
                        CallRow(
                            call = call,
                            settings = settings,
                            theme = theme,
                            onClick = { onTap(call) },
                            onLongClick = { actionCall = call },
                            onAction = { onSwipeAction(it, call) },
                            allowSwipe = allowSwipe,
                        )
                    }
                }
            }
        }

        ListBottomBar(
            theme = theme,
            contactsMode = false,
            missedCount = missedCount,
            onShortcut = onOpenContacts,
            onShowDialpad = onOpenKeypad,
            onCallHistory = {},
            onContacts = onOpenContacts,
            onSettings = onSettings,
        )
    }

    pendingDelete?.let { call ->
        val name = call.displayName ?: call.number
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = theme.surfaceStrong,
            titleContentColor = theme.textPrimary,
            textContentColor = theme.textSecondary,
            title = {
                Text(
                    if (call.count > 1) "Delete ${call.count} calls?" else "Delete this call?",
                )
            },
            text = {
                Text(
                    if (call.count > 1) {
                        "Every merged call with $name will be removed from the system call log."
                    } else {
                        "$name will be removed from the system call log."
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(call)
                        pendingDelete = null
                    },
                ) {
                    Text("Delete", color = theme.missedRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel", color = theme.textSecondary)
                }
            },
        )
    }

    actionCall?.let { call ->
        CallRowActionsDialog(
            call = call,
            settings = settings,
            theme = theme,
            onDismiss = { actionCall = null },
            onCall = { onSwipeAction(SwipeAction.CALL, call) },
            onSms = { onSwipeAction(SwipeAction.SMS, call) },
            onView = { onSwipeAction(SwipeAction.VIEW, call) },
            onDelete = {
                pendingDelete = call
            },
        )
    }
}

@Composable
private fun CallRow(
    call: CallEntry,
    settings: DialerSettings,
    theme: DialerTheme,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onAction: (SwipeAction) -> Unit,
    allowSwipe: Boolean,
) {
    val swipe = settings.swipeEnabled && allowSwipe
    SwipeActionRow(
        leftAction = if (swipe) settings.swipeLeftDialer else SwipeAction.NONE,
        rightAction = if (swipe) settings.swipeRightDialer else SwipeAction.NONE,
        enabled = swipe,
        theme = theme,
        onAction = onAction,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(18.dp)),
    ) {
        CallRowBody(
            call = call,
            settings = settings,
            theme = theme,
            onClick = onClick,
            onLongClick = onLongClick,
            avatarSize = 46.dp,
            horizontalPadding = 12.dp,
            verticalPadding = 10.dp,
        )
    }
}
