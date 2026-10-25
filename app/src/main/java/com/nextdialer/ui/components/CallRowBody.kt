package com.nextdialer.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextdialer.data.CallEntry
import com.nextdialer.data.CallKind
import com.nextdialer.data.DialerSettings
import com.nextdialer.ui.theme.DialerTheme
import com.nextdialer.ui.theme.LocalTextScale
import com.nextdialer.util.Formatters

/**
 * Shared call-log row content, matching exDialer's list rhythm:
 *
 *  - line 1: contact name when known, otherwise the number;
 *  - line 2 (contacts only): number type + number, ellipsized;
 *  - line 3: call-type icon + date (plus merged count when enabled);
 *  - right edge: type and duration of the call shown by this row.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CallRowBody(
    call: CallEntry,
    settings: DialerSettings,
    theme: DialerTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    avatarSize: Dp = 46.dp,
    horizontalPadding: Dp = 12.dp,
    verticalPadding: Dp = 10.dp,
    rowHeight: Dp = 78.dp,
) {
    val context = LocalContext.current
    val scale = LocalTextScale.current
    val color = callKindColor(call.kind, theme, settings.colorCalls)
    val hasName = !call.displayName.isNullOrBlank()
    val displayNumber = Formatters.formatPhone(context, call.number, settings.formatNumber)
    val primaryText = if (hasName) call.displayName.orEmpty() else displayNumber
    val numberLine = if (hasName) {
        listOfNotNull(
            call.numberLabel?.takeIf { it.isNotBlank() },
            displayNumber.takeIf { it.isNotBlank() },
        ).joinToString(" ")
    } else {
        null
    }
    val missed = call.kind == CallKind.MISSED
    val rightText = buildString {
        append(
            if (call.kind == CallKind.OUTGOING && call.duration <= 0L) {
                "Not answered"
            } else {
                callKindLabel(call.kind)
            },
        )
        Formatters.formatClockDuration(call.duration)
            .takeIf { it.isNotEmpty() }
            ?.let { append(' ').append(it) }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = rowHeight)
            .background(rowSurfaceColor(theme, settings.rowOpacity))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (settings.displayPhoto) {
            ContactAvatar(
                name = primaryText,
                photoUri = call.photoUri,
                size = avatarSize,
                theme = theme,
            )
        } else {
            Box(
                modifier = Modifier.size(avatarSize),
                contentAlignment = Alignment.Center,
            ) {
                CallTypeIcon(kind = call.kind, color = color)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = primaryText,
                color = if (missed) theme.missedRed else theme.textPrimary,
                fontSize = (16f * scale).sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (numberLine != null) {
                Text(
                    text = numberLine,
                    color = theme.textSecondary,
                    fontSize = (12.5f * scale).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                CallTypeIcon(kind = call.kind, color = color)
                Spacer(Modifier.width(5.dp))
                Text(
                    text = Formatters.callTimeLabel(
                        context,
                        call.date,
                        settings.relativeTime,
                        settings.displayTime,
                    ),
                    color = theme.textSecondary,
                    fontSize = (12.5f * scale).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (settings.countMergedCalls && call.count > 1) {
                    Text(
                        text = " · ${call.count} calls",
                        color = theme.textSecondary,
                        fontSize = (12f * scale).sp,
                        maxLines = 1,
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier.widthIn(max = 120.dp),
            horizontalAlignment = Alignment.End,
        ) {
            val organization = call.organization
            if (!organization.isNullOrBlank()) {
                Text(
                    text = organization,
                    color = theme.textSecondary,
                    fontSize = (11.5f * scale).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                )
            }
            Text(
                text = rightText,
                color = theme.textSecondary,
                fontSize = (12f * scale).sp,
                maxLines = 1,
                textAlign = TextAlign.End,
            )
        }
    }
}
