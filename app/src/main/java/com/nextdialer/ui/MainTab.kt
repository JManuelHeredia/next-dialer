package com.nextdialer.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(val label: String, val icon: ImageVector) {
    RECENTS("Recents", Icons.Filled.History),
    CONTACTS("Contacts", Icons.Filled.Contacts),
    KEYPAD("Keypad", Icons.Filled.Dialpad),
}
