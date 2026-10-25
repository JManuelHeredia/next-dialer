package com.nextdialer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextdialer.data.Contact
import com.nextdialer.data.DialerSettings
import com.nextdialer.data.SettingsStore
import com.nextdialer.data.SwipeAction
import com.nextdialer.data.TapAction
import com.nextdialer.ui.components.ChoiceDialog
import com.nextdialer.ui.components.SettingsDivider
import com.nextdialer.ui.components.SettingsHintCard
import com.nextdialer.ui.components.SettingsNote
import com.nextdialer.ui.components.SettingsPage
import com.nextdialer.ui.components.SettingsRow
import com.nextdialer.ui.components.SettingsSection
import com.nextdialer.ui.components.SettingsSliderRow
import com.nextdialer.ui.components.SettingsSwitchRow
import com.nextdialer.ui.components.SettingsValueRow
import com.nextdialer.ui.theme.DialerTheme
import com.nextdialer.ui.theme.DialerThemes
import com.nextdialer.util.PhoneUtils

@Composable
fun SettingsScreen(
    settings: DialerSettings,
    theme: DialerTheme,
    onBack: () -> Unit,
    onAppearance: () -> Unit,
    onBehaviour: () -> Unit,
    onAdvanced: () -> Unit,
    onAbout: () -> Unit,
) {
    SettingsPage(title = "Settings", theme = theme, onBack = onBack) {
        item { SettingsSection("NEXT Dialer", theme) }
        item {
            SettingsValueRow(
                title = "Appearance",
                value = DialerThemes.byId(settings.themeId).label,
                theme = theme,
                subtitle = "Theme, wallpaper dim and blur, text size",
                onClick = onAppearance,
            )
        }
        item { SettingsDivider(theme) }
        item {
            SettingsValueRow(
                title = "Behaviour",
                value = "",
                theme = theme,
                subtitle = "Swipe actions, tap actions, tones, haptics",
                onClick = onBehaviour,
            )
        }
        item { SettingsDivider(theme) }
        item {
            SettingsValueRow(
                title = "Advanced",
                value = "",
                theme = theme,
                subtitle = "Call log order, permissions, reset",
                onClick = onAdvanced,
            )
        }
        item { SettingsDivider(theme) }
        item {
            SettingsValueRow(
                title = "About NEXT Dialer",
                value = "",
                theme = theme,
                subtitle = "Version and credits",
                onClick = onAbout,
            )
        }
        item {
            SettingsNote(
                text = "NEXT Dialer is a keyless reimplementation of exDialer. " +
                    "No licence check, no key package, no Google Play billing.",
                theme = theme,
            )
        }
    }
}

@Composable
fun AppearanceScreen(
    settings: DialerSettings,
    store: SettingsStore,
    theme: DialerTheme,
    onBack: () -> Unit,
    onTheme: () -> Unit,
) {
    var showTextSize by remember { mutableStateOf(false) }
    var showDialpadSize by remember { mutableStateOf(false) }
    var showDialpadTextSize by remember { mutableStateOf(false) }
    var showAccent by remember { mutableStateOf(false) }

    val currentTheme = DialerThemes.byId(settings.themeId)

    SettingsPage(title = "Appearance", theme = theme, onBack = onBack) {
        item { SettingsSection("Theme", theme) }
        item {
            SettingsValueRow(
                title = "Theme",
                value = currentTheme.label,
                theme = theme,
                subtitle = currentTheme.description,
                onClick = onTheme,
            )
        }
        item {
            SettingsValueRow(
                title = "Accent colour",
                value = accentLabel(settings.accentColor),
                theme = theme,
                subtitle = "Overrides the theme blue for the status bar, digits and highlights",
                onClick = { showAccent = true },
            )
        }
        item {
            SettingsSliderRow(
                title = "Wallpaper dim",
                valueLabel = "${settings.wallpaperDim}%",
                value = settings.wallpaperDim.toFloat(),
                valueRange = 0f..80f,
                steps = 15,
                theme = theme,
                onValueChange = { store.setWallpaperDim(it.toInt()) },
            )
        }
        item {
            SettingsSliderRow(
                title = "Wallpaper blur",
                valueLabel = "${settings.wallpaperBlur} dp",
                value = settings.wallpaperBlur.toFloat(),
                valueRange = 0f..30f,
                steps = 29,
                theme = theme,
                onValueChange = { store.setWallpaperBlur(it.toInt()) },
            )
        }
        item {
            SettingsNote(
                text = if (android.os.Build.VERSION.SDK_INT >= 31) {
                    "Dim and blur apply to the Crystal and Crystal Simp wallpaper themes. " +
                        "Blur is drawn by the system behind the app, so it also softens " +
                        "live wallpapers."
                } else {
                    "Dim applies to the Crystal and Crystal Simp wallpaper themes. " +
                        "Blur needs Android 12 or newer; on this version only the dim " +
                        "level is used."
                },
                theme = theme,
            )
        }

        item { SettingsSection("Layout", theme) }
        item {
            SettingsSwitchRow(
                title = "Display photo",
                subtitle = "Show contact photos in lists",
                checked = settings.displayPhoto,
                onCheckedChange = store::setDisplayPhoto,
                theme = theme,
            )
        }
        item {
            SettingsSwitchRow(
                title = "Full screen",
                subtitle = "Hide the status bar",
                checked = settings.fullScreen,
                onCheckedChange = store::setFullScreen,
                theme = theme,
            )
        }
        item {
            SettingsValueRow(
                title = "Text size",
                value = textSizeLabel(settings.textSize),
                theme = theme,
                onClick = { showTextSize = true },
            )
        }
        item {
            SettingsSwitchRow(
                title = "Tint system bars",
                subtitle = "Draw the status bar in the theme colour",
                checked = settings.tintSystemBars,
                onCheckedChange = store::setTintSystemBars,
                theme = theme,
            )
        }

        item { SettingsSection("Call history", theme) }
        item {
            SettingsSwitchRow(
                title = "Colour call types",
                subtitle = "Red for missed, green for outgoing, blue for incoming",
                checked = settings.colorCalls,
                onCheckedChange = store::setColorCalls,
                theme = theme,
            )
        }
        item {
            SettingsSwitchRow(
                title = "Relative time",
                subtitle = "Show \"2 min. ago\" and \"Yesterday\"",
                checked = settings.relativeTime,
                onCheckedChange = store::setRelativeTime,
                theme = theme,
            )
        }
        item {
            SettingsSwitchRow(
                title = "Display time",
                subtitle = "Add the clock time to day labels",
                checked = settings.displayTime,
                onCheckedChange = store::setDisplayTime,
                theme = theme,
            )
        }
        item {
            SettingsSwitchRow(
                title = "Format phone numbers",
                subtitle = "Use the platform formatter and current region",
                checked = settings.formatNumber,
                onCheckedChange = store::setFormatNumber,
                theme = theme,
            )
        }
        item {
            SettingsNote(
                text = "Merged-call counts and the missed-call badge are planned for " +
                    "checkpoint 2.",
                theme = theme,
            )
        }

        item { SettingsSection("Dialpad", theme) }
        item {
            SettingsValueRow(
                title = "Dialpad size",
                value = sizeLabel(settings.dialpadSize),
                theme = theme,
                onClick = { showDialpadSize = true },
            )
        }
        item {
            SettingsValueRow(
                title = "Dialpad text size",
                value = sizeLabel(settings.dialpadTextSize),
                theme = theme,
                onClick = { showDialpadTextSize = true },
            )
        }
        item {
            SettingsSwitchRow(
                title = "Display ABC",
                subtitle = "Show letters under or beside the digits",
                checked = settings.displayAbc,
                onCheckedChange = store::setDisplayAbc,
                theme = theme,
            )
        }
        item {
            SettingsSwitchRow(
                title = "ABC at right",
                subtitle = "Place letters to the right of the digit",
                checked = settings.abcAtRight,
                onCheckedChange = store::setAbcAtRight,
                theme = theme,
                enabled = settings.displayAbc,
            )
        }
        item {
            SettingsHintCard(
                text = "Crystal is the default wallpaper theme. Crystal Simp uses a lighter " +
                    "scrim; Lollipop, Simplism and Classic are opaque.",
                theme = theme,
            )
        }
    }

    if (showTextSize) {
        ChoiceDialog(
            title = "Text size",
            options = listOf(0, 1, 2),
            selected = settings.textSize,
            label = ::textSizeLabel,
            theme = theme,
            onSelect = {
                store.setTextSize(it)
                showTextSize = false
            },
            onDismiss = { showTextSize = false },
        )
    }
    if (showDialpadSize) {
        ChoiceDialog(
            title = "Dialpad size",
            options = listOf(0, 1, 2),
            selected = settings.dialpadSize,
            label = ::sizeLabel,
            theme = theme,
            onSelect = {
                store.setDialpadSize(it)
                showDialpadSize = false
            },
            onDismiss = { showDialpadSize = false },
        )
    }
    if (showDialpadTextSize) {
        ChoiceDialog(
            title = "Dialpad text size",
            options = listOf(0, 1, 2),
            selected = settings.dialpadTextSize,
            label = ::sizeLabel,
            theme = theme,
            onSelect = {
                store.setDialpadTextSize(it)
                showDialpadTextSize = false
            },
            onDismiss = { showDialpadTextSize = false },
        )
    }
    if (showAccent) {
        AccentColorDialog(
            selected = settings.accentColor,
            defaultColor = currentTheme.accent,
            theme = theme,
            onSelect = {
                store.setAccentColor(it)
                showAccent = false
            },
            onDismiss = { showAccent = false },
        )
    }
}

@Composable
fun BehaviourScreen(
    settings: DialerSettings,
    store: SettingsStore,
    theme: DialerTheme,
    onBack: () -> Unit,
    onSpeedDial: () -> Unit,
) {
    var swipeDialog by remember { mutableStateOf<SwipeTarget?>(null) }
    var tapDialog by remember { mutableStateOf<TapTarget?>(null) }
    var showStrength by remember { mutableStateOf(false) }
    var showMerge by remember { mutableStateOf(false) }
    var showRange by remember { mutableStateOf(false) }

    val speedDialCount = settings.speedDial.size

    SettingsPage(title = "Behaviour", theme = theme, onBack = onBack) {
        item { SettingsSection("General", theme) }
        item {
            SettingsSwitchRow(
                title = "Screen rotation",
                subtitle = "Off locks the app to portrait",
                checked = settings.screenRotation,
                onCheckedChange = store::setScreenRotation,
                theme = theme,
            )
        }
        item {
            SettingsSwitchRow(
                title = "Allow external contact viewer",
                subtitle = "Offer an \"Open with\" action on contact details",
                checked = settings.allowExternalViewer,
                onCheckedChange = store::setAllowExternalViewer,
                theme = theme,
            )
        }
        item {
            SettingsSwitchRow(
                title = "Call confirm",
                subtitle = "Ask before placing a call",
                checked = settings.callConfirm,
                onCheckedChange = store::setCallConfirm,
                theme = theme,
            )
        }
        item {
            SettingsValueRow(
                title = "Speed dial",
                value = if (speedDialCount == 0) "Not set" else "$speedDialCount set",
                theme = theme,
                subtitle = "Long-press 2–9 on the dialpad",
                onClick = onSpeedDial,
            )
        }
        item {
            SettingsSwitchRow(
                title = "Short number as speed dial",
                subtitle = "A single digit 2–9 calls its speed-dial contact",
                checked = settings.shortNumberSpeedDial,
                onCheckedChange = store::setShortNumberSpeedDial,
                theme = theme,
            )
        }

        item { SettingsSection("Call history", theme) }
        item {
            SettingsValueRow(
                title = "Merge calls by",
                value = mergeLabel(settings.mergeBy),
                theme = theme,
                subtitle = "Show repeated calls as one row",
                onClick = { showMerge = true },
            )
        }
        item {
            SettingsValueRow(
                title = "Show calls from",
                value = callLogRangeLabel(settings.callLogRangeDays),
                theme = theme,
                subtitle = "Limit how far back the call log is loaded",
                onClick = { showRange = true },
            )
        }
        item {
            SettingsSwitchRow(
                title = "Count of merged calls",
                subtitle = "Show how many calls a row represents",
                checked = settings.countMergedCalls,
                onCheckedChange = store::setCountMergedCalls,
                theme = theme,
            )
        }
        item {
            SettingsSwitchRow(
                title = "Badge count",
                subtitle = "Show unread missed calls on Recents and the app icon",
                checked = settings.badgeCount,
                onCheckedChange = store::setBadgeCount,
                theme = theme,
            )
        }

        item { SettingsSection("Swipe actions", theme) }
        item {
            SettingsSwitchRow(
                title = "Enable swipe actions",
                subtitle = "Swipe rows in call history and contacts",
                checked = settings.swipeEnabled,
                onCheckedChange = store::setSwipeEnabled,
                theme = theme,
            )
        }
        item {
            SettingsValueRow(
                title = "Swipe left — call history",
                value = settings.swipeLeftDialer.label(),
                theme = theme,
                onClick = { swipeDialog = SwipeTarget.LEFT_DIALER },
            )
        }
        item {
            SettingsValueRow(
                title = "Swipe right — call history",
                value = settings.swipeRightDialer.label(),
                theme = theme,
                onClick = { swipeDialog = SwipeTarget.RIGHT_DIALER },
            )
        }
        item {
            SettingsValueRow(
                title = "Swipe left — contacts",
                value = settings.swipeLeftContacts.label(),
                theme = theme,
                onClick = { swipeDialog = SwipeTarget.LEFT_CONTACTS },
            )
        }
        item {
            SettingsValueRow(
                title = "Swipe right — contacts",
                value = settings.swipeRightContacts.label(),
                theme = theme,
                onClick = { swipeDialog = SwipeTarget.RIGHT_CONTACTS },
            )
        }

        item { SettingsSection("Tap actions", theme) }
        item {
            SettingsValueRow(
                title = "Tap on call history row",
                value = settings.tapDialer.label(),
                theme = theme,
                onClick = { tapDialog = TapTarget.DIALER },
            )
        }
        item {
            SettingsValueRow(
                title = "Tap on contact row",
                value = settings.tapContacts.label(),
                theme = theme,
                onClick = { tapDialog = TapTarget.CONTACTS },
            )
        }

        item { SettingsSection("Dialpad", theme) }
        item {
            SettingsSwitchRow(
                title = "Audible touch tones",
                subtitle = "Play DTMF tones while dialling",
                checked = settings.touchTones,
                onCheckedChange = store::setTouchTones,
                theme = theme,
            )
        }
        item {
            SettingsSwitchRow(
                title = "Haptic feedback",
                subtitle = "Vibrate on each keypress",
                checked = settings.haptic,
                onCheckedChange = store::setHaptic,
                theme = theme,
            )
        }
        item {
            SettingsValueRow(
                title = "Haptic strength",
                value = hapticLabel(settings.hapticStrength),
                theme = theme,
                onClick = { showStrength = true },
            )
        }
        item {
            SettingsSwitchRow(
                title = "Check clipboard",
                subtitle = "Show a paste shortcut when the clipboard holds a phone number",
                checked = settings.checkClipboard,
                onCheckedChange = store::setCheckClipboard,
                theme = theme,
            )
        }
        item {
            SettingsSwitchRow(
                title = "Popup dialpad",
                subtitle = "Open the app on the Keypad tab",
                checked = settings.autoPopupDialpad,
                onCheckedChange = store::setAutoPopupDialpad,
                theme = theme,
            )
        }
        item {
            SettingsSwitchRow(
                title = "Close dialpad on back",
                subtitle = "Back from the Keypad returns to call history",
                checked = settings.closeDialpadOnBack,
                onCheckedChange = store::setCloseDialpadOnBack,
                theme = theme,
            )
        }
        item {
            SettingsSwitchRow(
                title = "View calls after a call",
                subtitle = "Return to call history when a call starts",
                checked = settings.viewCallsOnCallEnd,
                onCheckedChange = store::setViewCallsOnCallEnd,
                theme = theme,
            )
        }

        item { SettingsSection("Tabs", theme) }
        item {
            SettingsSwitchRow(
                title = "Remember last tab",
                subtitle = "Reopen the tab you used last",
                checked = settings.rememberTab,
                onCheckedChange = store::setRememberTab,
                theme = theme,
            )
        }
        item {
            SettingsNote(
                text = "Swipe-down-to-close on the dialpad and automatic day/night theme " +
                    "switching are planned for later checkpoints.",
                theme = theme,
            )
        }
    }

    swipeDialog?.let { target ->
        ChoiceDialog(
            title = target.title,
            options = SwipeAction.entries,
            selected = target.get(settings),
            label = { it.label() },
            theme = theme,
            onSelect = {
                target.set(store, it)
                swipeDialog = null
            },
            onDismiss = { swipeDialog = null },
        )
    }

    tapDialog?.let { target ->
        ChoiceDialog(
            title = target.title,
            options = TapAction.entries,
            selected = target.get(settings),
            label = { it.label() },
            theme = theme,
            onSelect = {
                target.set(store, it)
                tapDialog = null
            },
            onDismiss = { tapDialog = null },
        )
    }

    if (showStrength) {
        ChoiceDialog(
            title = "Haptic strength",
            options = listOf(-1, 0, 1, 2),
            selected = settings.hapticStrength,
            label = ::hapticLabel,
            theme = theme,
            onSelect = {
                store.setHapticStrength(it)
                showStrength = false
            },
            onDismiss = { showStrength = false },
        )
    }

    if (showMerge) {
        ChoiceDialog(
            title = "Merge calls by",
            options = listOf(0, 1, 2),
            selected = settings.mergeBy,
            label = ::mergeLabel,
            theme = theme,
            onSelect = {
                store.setMergeBy(it)
                showMerge = false
            },
            onDismiss = { showMerge = false },
        )
    }

    if (showRange) {
        ChoiceDialog(
            title = "Show calls from",
            options = listOf(0, 30, 60, 90, 180, 365),
            selected = settings.callLogRangeDays,
            label = ::callLogRangeLabel,
            theme = theme,
            onSelect = {
                store.setCallLogRangeDays(it)
                showRange = false
            },
            onDismiss = { showRange = false },
        )
    }
}

@Composable
fun AdvancedScreen(
    settings: DialerSettings,
    store: SettingsStore,
    theme: DialerTheme,
    onBack: () -> Unit,
    onAbout: () -> Unit,
    onRequestPermissions: () -> Unit,
    onClearCallLog: () -> Unit,
) {
    var confirmReset by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    SettingsPage(title = "Advanced", theme = theme, onBack = onBack) {
        item { SettingsSection("Call history", theme) }
        item {
            SettingsSwitchRow(
                title = "Reverse call log",
                subtitle = "Show the oldest calls first",
                checked = settings.reverseCallLog,
                onCheckedChange = store::setReverseCallLog,
                theme = theme,
            )
        }
        item {
            SettingsValueRow(
                title = "Clear call history",
                value = "",
                theme = theme,
                subtitle = "Delete every call-log entry on this device",
                onClick = { confirmClear = true },
            )
        }

        item { SettingsSection("Permissions", theme) }
        item {
            SettingsRow(
                title = "Contacts",
                subtitle = "Needed for names, photos and T9 search",
                theme = theme,
                onClick = onRequestPermissions,
                trailing = {
                    Text(
                        text = if (settings.displayPhoto) "Manage" else "Manage",
                        color = theme.accent,
                        fontSize = 13.sp,
                    )
                },
            )
        }
        item {
            SettingsRow(
                title = "Call history",
                subtitle = "Needed for recent calls and call-back",
                theme = theme,
                onClick = onRequestPermissions,
                trailing = {
                    Text("Manage", color = theme.accent, fontSize = 13.sp)
                },
            )
        }
        item {
            SettingsRow(
                title = "Phone",
                subtitle = "Calls go through the system dialer when denied",
                theme = theme,
                onClick = onRequestPermissions,
                trailing = {
                    Text("Manage", color = theme.accent, fontSize = 13.sp)
                },
            )
        }
        item {
            SettingsNote(
                text = "Android may not show a dialog again after a permanent denial. " +
                    "Use the system app settings in that case.",
                theme = theme,
            )
        }

        item { SettingsSection("Maintenance", theme) }
        item {
            SettingsValueRow(
                title = "Reset all settings",
                value = "",
                theme = theme,
                subtitle = "Restore every NEXT Dialer preference to its default",
                onClick = { confirmReset = true },
            )
        }
        item {
            SettingsValueRow(
                title = "About",
                value = "",
                theme = theme,
                onClick = onAbout,
            )
        }
        item {
            SettingsNote(
                text = "JSON backup/restore through the Storage Access Framework and the " +
                    "default-dialer role request are planned for checkpoint 5.",
                theme = theme,
            )
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = theme.surfaceStrong,
            titleContentColor = theme.textPrimary,
            textContentColor = theme.textSecondary,
            title = { Text("Reset all settings?") },
            text = {
                Text("Theme, appearance, swipe actions and speed dial will return to defaults. " +
                    "Your contacts and call history are not touched.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        store.resetAll()
                        confirmReset = false
                    },
                ) {
                    Text("Reset", color = theme.missedRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text("Cancel", color = theme.textSecondary)
                }
            },
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            containerColor = theme.surfaceStrong,
            titleContentColor = theme.textPrimary,
            textContentColor = theme.textSecondary,
            title = { Text("Clear call history?") },
            text = {
                Text(
                    "Every call-log entry will be deleted from this device. " +
                        "Contacts are not affected. This cannot be undone.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearCallLog()
                        confirmClear = false
                    },
                ) {
                    Text("Clear", color = theme.missedRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text("Cancel", color = theme.textSecondary)
                }
            },
        )
    }
}

@Composable
fun ThemePickerScreen(
    current: String,
    theme: DialerTheme,
    onBack: () -> Unit,
    onSelect: (com.nextdialer.ui.theme.DialerTheme) -> Unit,
) {
    SettingsPage(title = "Theme", theme = theme, onBack = onBack) {
        item {
            SettingsNote(
                text = "Crystal themes use your wallpaper with adjustable dim and blur. " +
                    "The other themes are opaque and do not load the wallpaper.",
                theme = theme,
            )
        }
        DialerThemes.all.forEach { option ->
            item(key = option.id) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(option) }
                        .padding(horizontal = 22.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ThemePreview(option)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = option.label,
                            color = theme.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = option.description,
                            color = theme.textSecondary,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp,
                        )
                    }
                    if (option.id == current) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = theme.accent,
                            modifier = Modifier.size(22.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = theme.textSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            item { SettingsDivider(theme) }
        }
    }
}

@Composable
private fun ThemePreview(option: com.nextdialer.ui.theme.DialerTheme) {
    val backgroundModifier = if (option.useWallpaper) {
        Modifier.background(
            Brush.linearGradient(
                listOf(Color(0xFF203A43), Color(0xFF6D4C41), Color(0xFF2C5364)),
            ),
        )
    } else {
        Modifier.background(option.background)
    }

    Box(
        modifier = Modifier
            .size(width = 72.dp, height = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(backgroundModifier)
            .border(1.dp, option.divider, RoundedCornerShape(12.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(option.topBar),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, start = 5.dp, end = 5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(option.surface),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 3.dp)
                            .size(7.dp)
                            .background(option.accent, CircleShape),
                    )
                    Spacer(Modifier.width(4.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.72f)
                                .height(2.dp)
                                .background(option.textPrimary.copy(alpha = 0.7f)),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.46f)
                                .height(2.dp)
                                .background(option.textSecondary.copy(alpha = 0.6f)),
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(5.dp)
                .size(10.dp)
                .background(option.callGreen, CircleShape),
        )
    }
}

@Composable
private fun AccentColorDialog(
    selected: Int,
    defaultColor: Color,
    theme: DialerTheme,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surfaceStrong,
        titleContentColor = theme.textPrimary,
        textContentColor = theme.textSecondary,
        title = { Text("Accent colour") },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect(0) }
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(defaultColor)
                            .border(1.dp, theme.divider, CircleShape),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Theme default",
                        color = theme.textPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                    )
                    if (selected == 0) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = theme.accent,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                AccentChoices.chunked(6).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        row.forEach { argb ->
                            val color = Color(argb)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selected == argb) 2.dp else 1.dp,
                                        color = if (selected == argb) {
                                            theme.textPrimary
                                        } else {
                                            theme.divider
                                        },
                                        shape = CircleShape,
                                    )
                                    .clickable { onSelect(argb) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected == argb) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = if (color.luminance() > 0.55f) {
                                            Color.Black
                                        } else {
                                            Color.White
                                        },
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = theme.accent)
            }
        },
    )
}

private val AccentChoices = listOf(
    0xFF33B8FF.toInt(),
    0xFF26C6DA.toInt(),
    0xFF26A69A.toInt(),
    0xFF43A047.toInt(),
    0xFF7CB342.toInt(),
    0xFFFFB300.toInt(),
    0xFFFB8C00.toInt(),
    0xFFF4511E.toInt(),
    0xFFE53935.toInt(),
    0xFFEC407A.toInt(),
    0xFF8E24AA.toInt(),
    0xFF5C6BC0.toInt(),
)

private fun accentLabel(argb: Int): String =
    if (argb == 0) "Theme default" else "#%06X".format(argb and 0xFFFFFF)

@Composable
fun SpeedDialScreen(
    settings: DialerSettings,
    store: SettingsStore,
    theme: DialerTheme,
    onBack: () -> Unit,
    contacts: List<Contact>,
) {
    var editing by remember { mutableStateOf<Int?>(null) }

    SettingsPage(title = "Speed dial", theme = theme, onBack = onBack) {
        item {
            SettingsNote(
                text = "Long-press a digit on the dialpad to call its speed-dial number. " +
                    "A single digit also calls directly when short-number dialling is on.",
                theme = theme,
            )
        }
        (2..9).forEach { digit ->
            item(key = digit) {
                val number = settings.speedDial[digit]
                SettingsValueRow(
                    title = "$digit",
                    value = number ?: "Not set",
                    theme = theme,
                    subtitle = number?.let { numberLabel(contacts, it) },
                    onClick = { editing = digit },
                )
            }
            item { SettingsDivider(theme) }
        }
    }

    val digit = editing
    if (digit != null) {
        var text by remember(digit) { mutableStateOf(settings.speedDial[digit].orEmpty()) }
        AlertDialog(
            onDismissRequest = { editing = null },
            containerColor = theme.surfaceStrong,
            titleContentColor = theme.textPrimary,
            textContentColor = theme.textSecondary,
            title = { Text("Speed dial $digit") },
            text = {
                Column {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        singleLine = true,
                        label = { Text("Phone number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary,
                            focusedBorderColor = theme.accent,
                            unfocusedBorderColor = theme.divider,
                            focusedLabelColor = theme.accent,
                            unfocusedLabelColor = theme.textSecondary,
                            cursorColor = theme.accent,
                        ),
                    )
                    if (contacts.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Tip: pick a contact's number from the Contacts tab and " +
                                "type it here.",
                            color = theme.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleaned = text.trim()
                        if (cleaned.isNotBlank() && PhoneUtils.digits(cleaned).isNotEmpty()) {
                            store.setSpeedDial(digit, cleaned)
                        }
                        editing = null
                    },
                ) {
                    Text("Save", color = theme.accent)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            store.setSpeedDial(digit, null)
                            editing = null
                        },
                    ) {
                        Text("Clear", color = theme.missedRed)
                    }
                    TextButton(onClick = { editing = null }) {
                        Text("Cancel", color = theme.textSecondary)
                    }
                }
            },
        )
    }
}

@Composable
fun AboutScreen(theme: DialerTheme, onBack: () -> Unit) {
    SettingsPage(title = "About", theme = theme, onBack = onBack) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(theme.accent, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        tint = theme.accentOn,
                        modifier = Modifier.size(34.dp),
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "NEXT Dialer",
                    color = theme.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Version 0.1.0 · Checkpoint 1",
                    color = theme.textSecondary,
                    fontSize = 13.sp,
                )
            }
        }
        item {
            SettingsHintCard(
                text = "A modern, keyless reimplementation of exDialer: dialer-first, " +
                    "contacts and call history in one place, swipe actions, dimmed wallpaper " +
                    "and blur.",
                theme = theme,
            )
        }
        item { SettingsSection("Details", theme) }
        item {
            SettingsRow(
                title = "Package",
                theme = theme,
                trailing = {
                    Text("com.nextdialer", color = theme.textSecondary, fontSize = 13.sp)
                },
            )
        }
        item { SettingsDivider(theme) }
        item {
            SettingsRow(
                title = "Licence",
                theme = theme,
                trailing = {
                    Text("No key required", color = theme.accent, fontSize = 13.sp)
                },
            )
        }
        item { SettingsDivider(theme) }
        item {
            SettingsRow(
                title = "Legacy fallback",
                theme = theme,
                subtitle = "builds/legacy/exDialer-198-unlocked.apk",
                trailing = {
                    Text("v198 Pro", color = theme.textSecondary, fontSize = 13.sp)
                },
            )
        }
        item { SettingsSection("Credits", theme) }
        item {
            SettingsNote(
                text = "Original exDialer by modoohut. NEXT Dialer is an independent " +
                    "clean-room reimplementation in Kotlin and Jetpack Compose.",
                theme = theme,
            )
        }
        item {
            SettingsNote(
                text = "Documentation: README.md, docs/PLAN.md, docs/FEATURES.md, " +
                    "docs/RE_NOTES.md and docs/DEVLOG.md.",
                theme = theme,
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = theme.textSecondary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Checkpoint 2 adds merged calls, missed-call badge, delete " +
                        "entries and favourites.",
                    color = theme.textSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                )
            }
        }
    }
}

private enum class SwipeTarget(val title: String) {
    LEFT_DIALER("Swipe left — call history"),
    RIGHT_DIALER("Swipe right — call history"),
    LEFT_CONTACTS("Swipe left — contacts"),
    RIGHT_CONTACTS("Swipe right — contacts");

    fun get(settings: DialerSettings): SwipeAction = when (this) {
        LEFT_DIALER -> settings.swipeLeftDialer
        RIGHT_DIALER -> settings.swipeRightDialer
        LEFT_CONTACTS -> settings.swipeLeftContacts
        RIGHT_CONTACTS -> settings.swipeRightContacts
    }

    fun set(store: SettingsStore, action: SwipeAction) = when (this) {
        LEFT_DIALER -> store.setSwipeLeftDialer(action)
        RIGHT_DIALER -> store.setSwipeRightDialer(action)
        LEFT_CONTACTS -> store.setSwipeLeftContacts(action)
        RIGHT_CONTACTS -> store.setSwipeRightContacts(action)
    }
}

private enum class TapTarget(val title: String) {
    DIALER("Tap on call history row"),
    CONTACTS("Tap on contact row");

    fun get(settings: DialerSettings): TapAction = when (this) {
        DIALER -> settings.tapDialer
        CONTACTS -> settings.tapContacts
    }

    fun set(store: SettingsStore, action: TapAction) = when (this) {
        DIALER -> store.setTapDialer(action)
        CONTACTS -> store.setTapContacts(action)
    }
}

private fun textSizeLabel(value: Int): String = when (value) {
    1 -> "Large"
    2 -> "Extra large"
    else -> "Normal"
}

private fun sizeLabel(value: Int): String = when (value) {
    1 -> "Large"
    2 -> "Extra large"
    else -> "Normal"
}

private fun hapticLabel(value: Int): String = when (value) {
    -1 -> "Weak"
    1 -> "Strong"
    2 -> "Extra strong"
    else -> "Normal"
}

private fun mergeLabel(value: Int): String = when (value) {
    0 -> "None"
    1 -> "Person"
    else -> "Phone number"
}

private fun callLogRangeLabel(days: Int): String = when (days) {
    30 -> "1 month"
    60 -> "2 months"
    90 -> "3 months"
    180 -> "6 months"
    365 -> "1 year"
    else -> "All calls"
}

private fun SwipeAction.label(): String = when (this) {
    SwipeAction.CALL -> "Call"
    SwipeAction.SMS -> "Message"
    SwipeAction.VIEW -> "View details"
    SwipeAction.VOIP -> "VoIP call"
    SwipeAction.DELETE -> "Delete call history"
    SwipeAction.NONE -> "None"
}

private fun TapAction.label(): String = when (this) {
    TapAction.VIEW -> "View details"
    TapAction.CALL -> "Call"
    TapAction.SMS -> "Message"
    TapAction.VOIP -> "VoIP call"
}

private fun numberLabel(contacts: List<Contact>, number: String): String? {
    val match = contacts.firstOrNull { contact ->
        contact.numbers.any { PhoneUtils.numbersMatch(it.number, number) }
    }
    return match?.name
}
