package com.nextdialer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextdialer.data.Contact
import com.nextdialer.data.DialerSettings
import com.nextdialer.data.SwipeAction
import com.nextdialer.ui.components.ContactAvatar
import com.nextdialer.ui.components.DayHeader
import com.nextdialer.ui.components.EmptyState
import com.nextdialer.ui.components.ListBottomBar
import com.nextdialer.ui.components.PermissionBanner
import com.nextdialer.ui.components.SearchHeader
import com.nextdialer.ui.components.SwipeActionRow
import com.nextdialer.ui.components.rowSurfaceColor
import com.nextdialer.ui.theme.DialerTheme
import com.nextdialer.ui.theme.LocalTextScale
import com.nextdialer.util.DialerIntents
import com.nextdialer.util.PhoneUtils
import kotlinx.coroutines.launch

@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    settings: DialerSettings,
    theme: DialerTheme,
    contactsGranted: Boolean,
    onRequestPermissions: () -> Unit,
    onTap: (Contact) -> Unit,
    onSwipeAction: (SwipeAction, Contact) -> Unit,
    onOpenKeypad: () -> Unit,
    onOpenRecents: () -> Unit,
    missedCount: Int,
    onSettings: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var favouritesOnly by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val filtered = remember(contacts, query, favouritesOnly) {
        contacts.filter { contact ->
            (!favouritesOnly || contact.starred) && PhoneUtils.matchesContact(contact, query)
        }
    }
    val groups = remember(filtered) {
        filtered.groupBy { PhoneUtils.initialLetter(it.name) }.toSortedMap()
    }
    val indexMap = remember(groups) {
        val map = LinkedHashMap<String, Int>()
        var index = 0
        groups.forEach { (letter, list) ->
            map[letter] = index
            index += 1 + list.size
        }
        map
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchHeader(
            query = query,
            onQueryChange = { query = it },
            placeholder = "Search contacts",
            theme = theme,
        )

        if (contactsGranted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterPill(
                    label = "All",
                    selected = !favouritesOnly,
                    theme = theme,
                    onClick = { favouritesOnly = false },
                )
                FilterPill(
                    label = "Favourites",
                    selected = favouritesOnly,
                    theme = theme,
                    onClick = { favouritesOnly = true },
                )
            }
        }

        if (!contactsGranted) {
            PermissionBanner(
                message = "Allow access to contacts to see names, photos, and numbers.",
                theme = theme,
                onGrant = onRequestPermissions,
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
                    !contactsGranted -> EmptyState(
                        icon = Icons.Filled.Contacts,
                        title = "Contacts access needed",
                        message = "Grant contacts access to browse and search your address book.",
                        theme = theme,
                        actionLabel = "Grant access",
                        onAction = onRequestPermissions,
                    )
                    query.isNotBlank() -> EmptyState(
                        icon = Icons.Filled.Search,
                        title = "No matches",
                        message = "No contacts match \"$query\".",
                        theme = theme,
                    )
                    favouritesOnly -> EmptyState(
                        icon = Icons.Filled.Star,
                        title = "No favourites yet",
                        message = "Star contacts in your contacts app to pin them here.",
                        theme = theme,
                        actionLabel = "Show all contacts",
                        onAction = { favouritesOnly = false },
                    )
                    else -> EmptyState(
                        icon = Icons.Filled.PersonAdd,
                        title = "No contacts yet",
                        message = "Contacts with phone numbers will appear here.",
                        theme = theme,
                        actionLabel = "Add contact",
                        onAction = {
                            DialerIntents.insertContact(context)
                        },
                    )
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 20.dp),
                ) {
                    groups.forEach { (letter, group) ->
                        item(key = "header_$letter") {
                            DayHeader(label = letter, theme = theme)
                        }
                        items(
                            count = group.size,
                            key = { index -> "contact_${group[index].id}_$index" },
                        ) { index ->
                            val contact = group[index]
                            ContactRow(
                                contact = contact,
                                settings = settings,
                                theme = theme,
                                onClick = { onTap(contact) },
                                onAction = { onSwipeAction(it, contact) },
                            )
                        }
                    }
                }

                AlphabetIndex(
                    letters = indexMap.keys.toList(),
                    theme = theme,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onLetter = { letter ->
                        indexMap[letter]?.let { target ->
                            scope.launch { listState.animateScrollToItem(target) }
                        }
                    },
                )
            }
        }

        ListBottomBar(
            theme = theme,
            contactsMode = true,
            missedCount = missedCount,
            onShortcut = { DialerIntents.insertContact(context) },
            onShowDialpad = onOpenKeypad,
            onCallHistory = onOpenRecents,
            onContacts = {},
            onSettings = onSettings,
        )
    }
}

@Composable
private fun ContactRow(
    contact: Contact,
    settings: DialerSettings,
    theme: DialerTheme,
    onClick: () -> Unit,
    onAction: (SwipeAction) -> Unit,
) {
    val scale = LocalTextScale.current
    val number = contact.numbers.firstOrNull()

    SwipeActionRow(
        leftAction = if (settings.swipeEnabled) settings.swipeLeftContacts else SwipeAction.NONE,
        rightAction = if (settings.swipeEnabled) settings.swipeRightContacts else SwipeAction.NONE,
        enabled = settings.swipeEnabled,
        theme = theme,
        onAction = onAction,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(18.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(rowSurfaceColor(theme, settings.rowOpacity))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (settings.displayPhoto) {
                ContactAvatar(
                    name = contact.name,
                    photoUri = contact.photoUri,
                    size = 46.dp,
                    theme = theme,
                )
            } else {
                Box(modifier = Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = contact.initial,
                        color = theme.textSecondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    color = theme.textPrimary,
                    fontSize = (16f * scale).sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (number != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = number.typeLabel,
                            color = theme.textSecondary,
                            fontSize = (12f * scale).sp,
                            maxLines = 1,
                        )
                        Text(
                            text = " · ",
                            color = theme.textSecondary,
                            fontSize = (12f * scale).sp,
                        )
                        Text(
                            text = number.number,
                            color = theme.textSecondary,
                            fontSize = (12.5f * scale).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            IconButton(
                onClick = { onAction(SwipeAction.CALL) },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Phone,
                    contentDescription = "Call ${contact.name}",
                    tint = theme.accent,
                    modifier = Modifier.size(21.dp),
                )
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    theme: DialerTheme,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) {
            theme.accent.copy(alpha = 0.22f)
        } else {
            theme.surface.copy(alpha = if (theme.useWallpaper) 0.5f else 1f)
        },
        border = BorderStroke(
            1.dp,
            if (selected) theme.accent.copy(alpha = 0.6f) else theme.divider,
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            color = if (selected) theme.accent else theme.textSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun AlphabetIndex(
    letters: List<String>,
    theme: DialerTheme,
    modifier: Modifier = Modifier,
    onLetter: (String) -> Unit,
) {
    if (letters.isEmpty()) return
    Column(
        modifier = modifier
            .padding(end = 10.dp)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        letters.forEach { letter ->
            Text(
                text = letter,
                color = theme.accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onLetter(letter) }
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            )
        }
    }
}
