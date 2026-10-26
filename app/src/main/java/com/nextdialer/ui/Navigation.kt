package com.nextdialer.ui

sealed interface Route {
    data object Main : Route

    /** Per-number screen: contact details when known, otherwise a plain number screen. */
    data class Detail(val number: String, val contactId: Long?) : Route

    /** Recents pushed on top of a detail screen so Back returns to that detail. */
    data class History(val number: String?, val contactId: Long?) : Route

    data object Settings : Route
    data object Appearance : Route
    data object Behaviour : Route
    data object Advanced : Route
    data object ThemePicker : Route
    data object SpeedDial : Route
    data object ContactSources : Route
    data object About : Route
}
