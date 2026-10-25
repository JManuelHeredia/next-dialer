package com.nextdialer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.nextdialer.MainViewModel
import com.nextdialer.data.Contact
import com.nextdialer.data.DialerSettings
import com.nextdialer.data.SettingsStore
import com.nextdialer.data.SwipeAction
import com.nextdialer.data.TapAction
import com.nextdialer.notifications.MissedCallNotifier
import com.nextdialer.ui.components.WallpaperBackground
import com.nextdialer.ui.screens.AboutScreen
import com.nextdialer.ui.screens.AdvancedScreen
import com.nextdialer.ui.screens.AppearanceScreen
import com.nextdialer.ui.screens.BehaviourScreen
import com.nextdialer.ui.screens.ContactDetailScreen
import com.nextdialer.ui.screens.ContactsScreen
import com.nextdialer.ui.screens.DialpadScreen
import com.nextdialer.ui.screens.PermissionScreen
import com.nextdialer.ui.screens.RecentsScreen
import com.nextdialer.ui.screens.SettingsScreen
import com.nextdialer.ui.screens.SpeedDialScreen
import com.nextdialer.ui.screens.ThemePickerScreen
import com.nextdialer.ui.theme.DialerTheme
import com.nextdialer.util.DialerIntents

@Composable
fun AppRoot(
    settings: DialerSettings,
    store: SettingsStore,
    theme: DialerTheme,
    viewModel: MainViewModel,
    onRequestPermissions: () -> Unit,
) {
    val contacts by viewModel.contacts.collectAsState()
    val calls by viewModel.calls.collectAsState()
    val contactsGranted by viewModel.contactsGranted.collectAsState()
    val callLogGranted by viewModel.callLogGranted.collectAsState()
    val phoneGranted by viewModel.phoneGranted.collectAsState()

    val context = LocalContext.current
    val missedCount = remember(calls, settings.lastSeenCallId) {
        calls.sumOf { entry -> entry.unreadIds.count { it > settings.lastSeenCallId } }
    }

    LaunchedEffect(missedCount, settings.badgeCount) {
        MissedCallNotifier.update(context, missedCount, settings.badgeCount)
    }

    LaunchedEffect(settings.mergeBy) {
        viewModel.reload()
    }

    var onboardingDismissed by rememberSaveable { mutableStateOf(false) }
    val showOnboarding = !onboardingDismissed &&
        !settings.onboardingDone &&
        !contactsGranted && !callLogGranted && !phoneGranted

    if (showOnboarding) {
        Box(modifier = Modifier.fillMaxSize()) {
            WallpaperBackground(
                theme = theme,
                dimPercent = settings.wallpaperDim,
                tintSystemBars = settings.tintSystemBars,
                fullScreen = settings.fullScreen,
            )
            PermissionScreen(
                theme = theme,
                onRequest = {
                    onboardingDismissed = true
                    store.setOnboardingDone(true)
                    onRequestPermissions()
                },
                onSkip = {
                    onboardingDismissed = true
                    store.setOnboardingDone(true)
                },
            )
        }
        return
    }

    val routes = remember { mutableStateListOf<Route>(Route.Main) }
    val current = routes.lastOrNull() ?: Route.Main

    fun push(route: Route) {
        routes.add(route)
    }

    fun pop() {
        if (routes.size > 1) routes.removeAt(routes.lastIndex)
    }

    BackHandler(enabled = routes.size > 1) { pop() }

    var pendingCall by remember { mutableStateOf<String?>(null) }
    var pendingDeleteNumber by remember { mutableStateOf<String?>(null) }
    var callStarted by remember { mutableIntStateOf(0) }
    var requestedTab by remember { mutableStateOf<MainTab?>(null) }
    var recentsQuery by remember { mutableStateOf<String?>(null) }

    fun dialNow(number: String) {
        DialerIntents.call(context, number)
        callStarted += 1
    }

    fun placeCall(number: String) {
        if (number.isBlank()) return
        if (settings.callConfirm) {
            pendingCall = number
        } else {
            dialNow(number)
        }
    }

    fun runAction(action: SwipeAction, number: String?, contactId: Long?) {
        when (action) {
            SwipeAction.CALL -> number?.let(::placeCall)
            SwipeAction.SMS -> number?.let { DialerIntents.sms(context, it) }
            SwipeAction.VOIP -> number?.let { DialerIntents.voip(context, it) }
            SwipeAction.VIEW -> {
                val target = number
                    ?: contacts.firstOrNull { it.id == contactId }?.primaryNumber
                if (target != null) {
                    push(Route.Detail(target, contactId))
                } else if (contactId != null) {
                    push(Route.Detail("", contactId))
                }
            }
            SwipeAction.DELETE -> {
                val target = number
                    ?: contacts.firstOrNull { it.id == contactId }?.primaryNumber
                pendingDeleteNumber = target
            }
            SwipeAction.NONE -> Unit
        }
    }

    fun runTap(action: TapAction, number: String?, contactId: Long?) {
        when (action) {
            TapAction.VIEW -> {
                val target = number
                    ?: contacts.firstOrNull { it.id == contactId }?.primaryNumber
                if (target != null) {
                    push(Route.Detail(target, contactId))
                }
            }
            TapAction.CALL -> number?.let(::placeCall)
            TapAction.SMS -> number?.let { DialerIntents.sms(context, it) }
            TapAction.VOIP -> number?.let { DialerIntents.voip(context, it) }
        }
    }

    val saveableStateHolder = rememberSaveableStateHolder()

    Box(modifier = Modifier.fillMaxSize()) {
        WallpaperBackground(
            theme = theme,
            dimPercent = settings.wallpaperDim,
            tintSystemBars = settings.tintSystemBars,
            fullScreen = settings.fullScreen,
        )

        // Keep each screen's saveable state (selected tab, dialpad number, list scroll
        // position) while a settings or contact route is pushed on top.
        saveableStateHolder.SaveableStateProvider(routeKey(current)) {
            when (current) {
                Route.Main -> MainScaffold(
                    settings = settings,
                    store = store,
                    theme = theme,
                    contacts = contacts,
                    calls = calls,
                    missedCount = missedCount,
                    contactsGranted = contactsGranted,
                    callLogGranted = callLogGranted,
                    phoneGranted = phoneGranted,
                    onRequestPermissions = onRequestPermissions,
                    onSettings = { push(Route.Settings) },
                    onPlaceCall = ::placeCall,
                    onRunAction = ::runAction,
                    onRunTap = ::runTap,
                    onDeleteCalls = viewModel::deleteCalls,
                    callStarted = callStarted,
                    requestedTab = requestedTab,
                    onTabRequestHandled = { requestedTab = null },
                    requestedQuery = recentsQuery,
                    onQueryHandled = { recentsQuery = null },
                )

                is Route.Detail -> ContactDetailScreen(
                    number = current.number,
                    contactId = current.contactId,
                    settings = settings,
                    theme = theme,
                    calls = calls,
                    loadDetail = viewModel::detailFor,
                    onBack = ::pop,
                    onPlaceCall = ::placeCall,
                    onViewCalls = {
                        routes.clear()
                        routes.add(Route.Main)
                        requestedTab = MainTab.RECENTS
                        recentsQuery = current.number
                    },
                    onDeleteHistory = { pendingDeleteNumber = it },
                )

                Route.Settings -> SettingsScreen(
                    settings = settings,
                    theme = theme,
                    onBack = ::pop,
                    onAppearance = { push(Route.Appearance) },
                    onBehaviour = { push(Route.Behaviour) },
                    onAdvanced = { push(Route.Advanced) },
                    onAbout = { push(Route.About) },
                )

                Route.Appearance -> AppearanceScreen(
                    settings = settings,
                    store = store,
                    theme = theme,
                    onBack = ::pop,
                    onTheme = { push(Route.ThemePicker) },
                )

                Route.Behaviour -> BehaviourScreen(
                    settings = settings,
                    store = store,
                    theme = theme,
                    onBack = ::pop,
                    onSpeedDial = { push(Route.SpeedDial) },
                )

                Route.Advanced -> AdvancedScreen(
                    settings = settings,
                    store = store,
                    theme = theme,
                    onBack = ::pop,
                    onAbout = { push(Route.About) },
                    onRequestPermissions = onRequestPermissions,
                    onClearCallLog = viewModel::clearCallLog,
                )

                Route.ThemePicker -> ThemePickerScreen(
                    current = settings.themeId,
                    theme = theme,
                    onBack = ::pop,
                    onSelect = {
                        store.setTheme(it.id)
                        pop()
                    },
                )

                Route.SpeedDial -> SpeedDialScreen(
                    settings = settings,
                    store = store,
                    theme = theme,
                    onBack = ::pop,
                    contacts = contacts,
                )

                Route.About -> AboutScreen(theme = theme, onBack = ::pop)
            }
        }

        val number = pendingCall
        if (number != null) {
            AlertDialog(
                onDismissRequest = { pendingCall = null },
                containerColor = theme.surfaceStrong,
                titleContentColor = theme.textPrimary,
                textContentColor = theme.textSecondary,
                title = { Text("Call this number?") },
                text = { Text(number) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pendingCall = null
                            dialNow(number)
                        },
                    ) {
                        Text("Call", color = theme.accent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingCall = null }) {
                        Text("Cancel", color = theme.textSecondary)
                    }
                },
            )
        }

        val deleteNumber = pendingDeleteNumber
        if (deleteNumber != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteNumber = null },
                containerColor = theme.surfaceStrong,
                titleContentColor = theme.textPrimary,
                textContentColor = theme.textSecondary,
                title = { Text("Delete call history?") },
                text = { Text("Every call with $deleteNumber will be removed from the system call log.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteCallsForNumber(deleteNumber)
                            pendingDeleteNumber = null
                        },
                    ) {
                        Text("Delete", color = theme.missedRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteNumber = null }) {
                        Text("Cancel", color = theme.textSecondary)
                    }
                },
            )
        }
    }
}

@Composable
private fun MainScaffold(
    settings: DialerSettings,
    store: SettingsStore,
    theme: DialerTheme,
    contacts: List<Contact>,
    calls: List<com.nextdialer.data.CallEntry>,
    missedCount: Int,
    contactsGranted: Boolean,
    callLogGranted: Boolean,
    phoneGranted: Boolean,
    onRequestPermissions: () -> Unit,
    onSettings: () -> Unit,
    onPlaceCall: (String) -> Unit,
    onRunAction: (SwipeAction, String?, Long?) -> Unit,
    onRunTap: (TapAction, String?, Long?) -> Unit,
    onDeleteCalls: (List<Long>) -> Unit,
    callStarted: Int,
    requestedTab: MainTab?,
    onTabRequestHandled: () -> Unit,
    requestedQuery: String?,
    onQueryHandled: () -> Unit,
) {
    val initialTab = remember {
        when {
            settings.autoPopupDialpad -> MainTab.KEYPAD
            settings.rememberTab -> runCatching { MainTab.valueOf(settings.lastTab) }
                .getOrDefault(MainTab.RECENTS)
            else -> MainTab.RECENTS
        }
    }
    var tab by rememberSaveable { mutableStateOf(initialTab) }

    LaunchedEffect(tab) {
        if (settings.rememberTab) store.setLastTab(tab.name)
    }

    LaunchedEffect(requestedTab) {
        val target = requestedTab ?: return@LaunchedEffect
        tab = target
        onTabRequestHandled()
    }

    LaunchedEffect(callStarted) {
        if (callStarted > 0 && settings.viewCallsOnCallEnd) {
            tab = MainTab.RECENTS
        }
    }

    // Opening the call history acknowledges the missed calls that are currently listed, which
    // clears both the in-app badge and the summary notification.
    LaunchedEffect(tab, calls, settings.lastSeenCallId) {
        if (tab == MainTab.RECENTS && calls.isNotEmpty()) {
            val newest = calls.maxOf { entry -> entry.ids.maxOrNull() ?: entry.id }
            if (newest > settings.lastSeenCallId) store.setLastSeenCallId(newest)
        }
    }

    BackHandler(enabled = tab == MainTab.KEYPAD && settings.closeDialpadOnBack) {
        tab = MainTab.RECENTS
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (tab) {
            MainTab.RECENTS -> RecentsScreen(
                calls = calls,
                settings = settings,
                theme = theme,
                contactsGranted = contactsGranted,
                callLogGranted = callLogGranted,
                onRequestPermissions = onRequestPermissions,
                initialQuery = requestedQuery,
                onInitialQueryHandled = onQueryHandled,
                onTap = { call ->
                    onRunTap(
                        settings.tapDialer,
                        call.number,
                        call.contactId,
                    )
                },
                onSwipeAction = { action, call ->
                    onRunAction(action, call.number, call.contactId)
                },
                onDelete = { call -> onDeleteCalls(call.ids) },
                onOpenKeypad = { tab = MainTab.KEYPAD },
                onOpenContacts = { tab = MainTab.CONTACTS },
                missedCount = missedCount,
                onSettings = onSettings,
            )

            MainTab.CONTACTS -> ContactsScreen(
                contacts = contacts,
                settings = settings,
                theme = theme,
                contactsGranted = contactsGranted,
                onRequestPermissions = onRequestPermissions,
                onTap = { contact ->
                    onRunTap(
                        settings.tapContacts,
                        contact.primaryNumber,
                        contact.id,
                    )
                },
                onSwipeAction = { action, contact ->
                    onRunAction(action, contact.primaryNumber, contact.id)
                },
                onOpenKeypad = { tab = MainTab.KEYPAD },
                onOpenRecents = { tab = MainTab.RECENTS },
                missedCount = missedCount,
                onSettings = onSettings,
            )

            MainTab.KEYPAD -> DialpadScreen(
                settings = settings,
                theme = theme,
                contacts = contacts,
                calls = calls,
                contactsGranted = contactsGranted,
                callLogGranted = callLogGranted,
                onPlaceCall = onPlaceCall,
                onTapContact = { contact ->
                    onRunTap(settings.tapDialer, contact.primaryNumber, contact.id)
                },
                onTapCall = { call ->
                    onRunTap(settings.tapDialer, call.number, call.contactId)
                },
                onSwipeAction = { action, call ->
                    onRunAction(action, call.number, call.contactId)
                },
                onOpenSettings = onSettings,
                onOpenContacts = { tab = MainTab.CONTACTS },
                onOpenRecents = { tab = MainTab.RECENTS },
                missedCount = missedCount,
                onRequestPermissions = onRequestPermissions,
            )
        }
    }
}

private fun routeKey(route: Route): String = when (route) {
    Route.Main -> "main"
    is Route.Detail -> "detail-${route.contactId ?: "x"}-${route.number}"
    Route.Settings -> "settings"
    Route.Appearance -> "appearance"
    Route.Behaviour -> "behaviour"
    Route.Advanced -> "advanced"
    Route.ThemePicker -> "theme-picker"
    Route.SpeedDial -> "speed-dial"
    Route.About -> "about"
}
