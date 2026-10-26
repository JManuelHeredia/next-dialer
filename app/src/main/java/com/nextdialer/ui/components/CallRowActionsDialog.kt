package com.nextdialer.ui.components

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nextdialer.data.CallEntry
import com.nextdialer.data.DialerSettings
import com.nextdialer.ui.theme.DialerTheme
import com.nextdialer.util.DialerIntents
import com.nextdialer.util.Formatters

/**
 * Dynamic long-press menu for a call row. Registered contacts get view/edit/favourite/delete
 * actions; unknown numbers get Add contact. The number can always be copied or shared.
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
    val contactId = call.contactId?.takeIf { it > 0L }
    val isContact = contactId != null
    val name = call.displayName
        ?: Formatters.formatPhone(context, call.number, settings.formatNumber)
    val starred by produceState(initialValue = false, contactId) {
        value = contactId?.let { isStarred(context, it) } ?: false
    }
    var confirmDeleteContact by remember { mutableStateOf(false) }

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
                if (isContact) {
                    RowAction(
                        icon = Icons.Filled.Person,
                        label = "View contact",
                        theme = theme,
                        onClick = { run(onView) },
                    )
                    RowAction(
                        icon = Icons.Filled.Edit,
                        label = "Edit contact",
                        theme = theme,
                        onClick = {
                            run { DialerIntents.editContact(context, contactId) }
                        },
                    )
                    RowAction(
                        icon = Icons.Filled.Star,
                        label = if (starred) "Remove from favorites" else "Add to favorites",
                        theme = theme,
                        tint = if (starred) Color(0xFFFFC107) else theme.textSecondary,
                        onClick = {
                            setStarred(context, contactId, !starred)
                            Toast.makeText(
                                context,
                                if (starred) "Removed from favorites" else "Added to favorites",
                                Toast.LENGTH_SHORT,
                            ).show()
                            onDismiss()
                        },
                    )
                } else {
                    RowAction(
                        icon = Icons.Filled.PersonAdd,
                        label = "Add contact",
                        theme = theme,
                        onClick = {
                            run { DialerIntents.insertContact(context, call.number) }
                        },
                    )
                }
                RowAction(
                    icon = Icons.Filled.ContentCopy,
                    label = "Copy number",
                    theme = theme,
                    onClick = {
                        val clipboard = context.getSystemService(
                            Context.CLIPBOARD_SERVICE,
                        ) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("Phone number", call.number),
                        )
                        Toast.makeText(context, "Number copied", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                )
                RowAction(
                    icon = Icons.Filled.Share,
                    label = "Share",
                    theme = theme,
                    onClick = {
                        run { DialerIntents.shareContact(context, name, call.number) }
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
                if (isContact) {
                    RowAction(
                        icon = Icons.Filled.Delete,
                        label = "Delete contact",
                        theme = theme,
                        tint = theme.missedRed,
                        onClick = { confirmDeleteContact = true },
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

    if (confirmDeleteContact && contactId != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteContact = false },
            containerColor = theme.surfaceStrong,
            titleContentColor = theme.textPrimary,
            textContentColor = theme.textSecondary,
            title = { Text("Delete contact?") },
            text = { Text("$name will be removed from the contact store on this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteContact(context, contactId)
                        Toast.makeText(context, "Contact deleted", Toast.LENGTH_SHORT).show()
                        confirmDeleteContact = false
                        onDismiss()
                    },
                ) {
                    Text("Delete", color = theme.missedRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteContact = false }) {
                    Text("Cancel", color = theme.textSecondary)
                }
            },
        )
    }
}

private fun hasWriteContacts(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_CONTACTS,
    ) == PackageManager.PERMISSION_GRANTED

private fun isStarred(context: Context, contactId: Long): Boolean {
    val projection = arrayOf(ContactsContract.Contacts.STARRED)
    return runCatching {
        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            "${ContactsContract.Contacts._ID} = ?",
            arrayOf(contactId.toString()),
            null,
        )?.use { cursor ->
            cursor.moveToFirst() && cursor.getInt(0) == 1
        } ?: false
    }.getOrDefault(false)
}

private fun setStarred(context: Context, contactId: Long, starred: Boolean) {
    if (!hasWriteContacts(context)) {
        Toast.makeText(
            context,
            "Allow contacts modification to change favorites.",
            Toast.LENGTH_LONG,
        ).show()
        return
    }
    runCatching {
        context.contentResolver.update(
            ContactsContract.Contacts.CONTENT_URI,
            ContentValues().apply {
                put(ContactsContract.Contacts.STARRED, if (starred) 1 else 0)
            },
            "${ContactsContract.Contacts._ID} = ?",
            arrayOf(contactId.toString()),
        )
    }
}

private fun deleteContact(context: Context, contactId: Long) {
    if (!hasWriteContacts(context)) {
        Toast.makeText(
            context,
            "Allow contacts modification to delete contacts.",
            Toast.LENGTH_LONG,
        ).show()
        return
    }
    runCatching {
        context.contentResolver.delete(
            ContactsContract.RawContacts.CONTENT_URI,
            "${ContactsContract.RawContacts.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
        )
    }
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
            .padding(horizontal = 6.dp, vertical = 12.dp),
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
