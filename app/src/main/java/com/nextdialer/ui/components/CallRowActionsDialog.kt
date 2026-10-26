package com.nextdialer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextdialer.data.CallEntry
import com.nextdialer.data.DialerSettings
import com.nextdialer.ui.theme.DialerTheme
import com.nextdialer.util.DialerIntents
import com.nextdialer.util.Formatters

/**
 * Long-press menu for a call row. Mirrors the useful exDialer entries without the legacy
 * "edit number before call" and VoIP items.
 */
@Composable
fun CallRowActionsDialog(
    call: CallEntry,
    settings: DialerSettings,
    theme: DialerTheme,
    onDismiss: () -> Unit,
    onCall: () -> Unit,
    onSms: () -> Unit,
    onView: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val name = call.displayName
        ?: Formatters.formatPhone(context, call.number, settings.formatNumber)

    fun run(action: () -> Unit) {
        onDismiss()
        action()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surfaceStrong,
        titleContentColor = theme.textPrimary,
        textContentColor = theme.textSecondary,
        title = {
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Column {
                RowAction(
                    icon = Icons.Filled.Call,
                    label = "Call",
                    theme = theme,
                    onClick = { run(onCall) },
                )
                RowAction(
                    icon = Icons.Filled.Message,
                    label = "Send SMS",
                    theme = theme,
                    onClick = { run(onSms) },
                )
                RowAction(
                    icon = Icons.Filled.Person,
                    label = "View contact",
                    theme = theme,
                    onClick = { run(onView) },
                )
                RowAction(
                    icon = Icons.Filled.Share,
                    label = "Share",
                    theme = theme,
                    onClick = {
                        run {
                            DialerIntents.shareContact(context, name, call.number)
                        }
                    },
                )
                if (onDelete != null) {
                    HorizontalDivider(color = theme.divider)
                    RowAction(
                        icon = Icons.Filled.Delete,
                        label = "Delete call history",
                        theme = theme,
                        tint = theme.missedRed,
                        onClick = { run(onDelete) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = theme.textSecondary)
            }
        },
    )
}

@Composable
private fun RowAction(
    icon: ImageVector,
    label: String,
    theme: DialerTheme,
    tint: Color = theme.textSecondary,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            color = if (label.startsWith("Delete")) theme.missedRed else theme.textPrimary,
            fontSize = 15.sp,
        )
    }
}
