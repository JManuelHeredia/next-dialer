package com.nextdialer.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DialerSettings(
    val onboardingDone: Boolean = false,
    val themeId: String = "crystal",
    /** ARGB override for the theme accent. 0 keeps the theme's built-in colour. */
    val accentColor: Int = 0,
    val wallpaperDim: Int = 55,
    val wallpaperBlur: Int = 14,
    val displayPhoto: Boolean = true,
    val fullScreen: Boolean = false,
    val textSize: Int = 0,
    /** Percent of the theme's default list-row opacity; 100 = current/default. */
    val rowOpacity: Int = 100,
    val colorCalls: Boolean = false,
    val relativeTime: Boolean = true,
    val displayTime: Boolean = false,
    val formatNumber: Boolean = true,
    val tintSystemBars: Boolean = true,
    val dialpadSize: Int = 0,
    val dialpadTextSize: Int = 0,
    val displayAbc: Boolean = true,
    val abcAtRight: Boolean = true,
    val countMergedCalls: Boolean = false,
    val badgeCount: Boolean = true,
    val screenRotation: Boolean = false,
    val allowExternalViewer: Boolean = false,
    val callConfirm: Boolean = false,
    val swipeEnabled: Boolean = true,
    val swipeLeftDialer: SwipeAction = SwipeAction.SMS,
    val swipeRightDialer: SwipeAction = SwipeAction.CALL,
    val swipeLeftContacts: SwipeAction = SwipeAction.SMS,
    val swipeRightContacts: SwipeAction = SwipeAction.CALL,
    val tapDialer: TapAction = TapAction.VIEW,
    val tapContacts: TapAction = TapAction.VIEW,
    val touchTones: Boolean = true,
    val haptic: Boolean = false,
    val hapticStrength: Int = 0,
    val checkClipboard: Boolean = false,
    val autoPopupDialpad: Boolean = true,
    val swipeCloseDialpad: Boolean = true,
    val closeDialpadOnBack: Boolean = false,
    val viewCallsOnCallEnd: Boolean = true,
    val switchToCallLog: Boolean = false,
    val rememberTab: Boolean = true,
    val reverseCallLog: Boolean = false,
    val mergeBy: Int = 2,
    /** 0 = show every call the provider returns; otherwise a rolling day window. */
    val callLogRangeDays: Int = 0,
    val lastSeenCallId: Long = 0,
    val shortNumberSpeedDial: Boolean = true,
    val lastTab: String = "KEYPAD",
    val speedDial: Map<Int, String> = emptyMap(),
)

/**
 * Small typed wrapper around the app's SharedPreferences. Every setter writes immediately and the
 * state flow updates through the preference-change listener, so Compose sees changes everywhere.
 */
class SettingsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(read())
    val state: StateFlow<DialerSettings> = _state.asStateFlow()

    private val listener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> _state.value = read() }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    private fun read(): DialerSettings {
        val speedDial = buildMap {
            for (digit in 2..9) {
                prefs.getString("speed_dial_$digit", null)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { put(digit, it) }
            }
        }
        return DialerSettings(
            onboardingDone = prefs.getBoolean("onboarding_done", false),
            themeId = prefs.getString("theme_id", "crystal") ?: "crystal",
            accentColor = prefs.getInt("accent_color", 0),
            wallpaperDim = prefs.getInt("wallpaper_dim", 55),
            wallpaperBlur = prefs.getInt("wallpaper_blur", 14),
            displayPhoto = prefs.getBoolean("display_photo", true),
            fullScreen = prefs.getBoolean("full_screen", false),
            textSize = prefs.getInt("text_size", 0),
            rowOpacity = prefs.getInt("row_opacity", 100),
            colorCalls = prefs.getBoolean("color_calls", false),
            relativeTime = prefs.getBoolean("relative_time", true),
            displayTime = prefs.getBoolean("display_time", false),
            formatNumber = prefs.getBoolean("format_number", true),
            tintSystemBars = prefs.getBoolean("tint_system_bars", true),
            dialpadSize = prefs.getInt("dialpad_size", 0),
            dialpadTextSize = prefs.getInt("dialpad_text_size", 0),
            displayAbc = prefs.getBoolean("display_abc", true),
            abcAtRight = prefs.getBoolean("abc_at_right", true),
            countMergedCalls = prefs.getBoolean("count_merged_calls", false),
            badgeCount = prefs.getBoolean("badge_count", true),
            screenRotation = prefs.getBoolean("screen_rotation", false),
            allowExternalViewer = prefs.getBoolean("allow_external_viewer", false),
            callConfirm = prefs.getBoolean("call_confirm", false),
            swipeEnabled = prefs.getBoolean("swipe_enabled", true),
            swipeLeftDialer = enumPref("swipe_left_dialer", SwipeAction.SMS),
            swipeRightDialer = enumPref("swipe_right_dialer", SwipeAction.CALL),
            swipeLeftContacts = enumPref("swipe_left_contacts", SwipeAction.SMS),
            swipeRightContacts = enumPref("swipe_right_contacts", SwipeAction.CALL),
            tapDialer = enumPref("tap_dialer", TapAction.VIEW),
            tapContacts = enumPref("tap_contacts", TapAction.VIEW),
            touchTones = prefs.getBoolean("touch_tones", true),
            haptic = prefs.getBoolean("haptic", false),
            hapticStrength = prefs.getInt("haptic_strength", 0),
            checkClipboard = prefs.getBoolean("check_clipboard", false),
            autoPopupDialpad = prefs.getBoolean("auto_popup_dialpad", true),
            swipeCloseDialpad = prefs.getBoolean("swipe_close_dialpad", true),
            closeDialpadOnBack = prefs.getBoolean("close_dialpad_on_back", false),
            viewCallsOnCallEnd = prefs.getBoolean("view_calls_on_call_end", true),
            switchToCallLog = prefs.getBoolean("switch_to_call_log", false),
            rememberTab = prefs.getBoolean("remember_tab", true),
            reverseCallLog = prefs.getBoolean("reverse_call_log", false),
            mergeBy = prefs.getInt("merge_by", 2),
            callLogRangeDays = prefs.getInt("call_log_range_days", 0),
            lastSeenCallId = prefs.getLong("last_seen_call_id", 0L),
            shortNumberSpeedDial = prefs.getBoolean("short_number_speed_dial", true),
            lastTab = prefs.getString("last_tab", "KEYPAD") ?: "KEYPAD",
            speedDial = speedDial,
        )
    }

    private inline fun <reified T : Enum<T>> enumPref(key: String, fallback: T): T {
        val stored = prefs.getString(key, fallback.name) ?: fallback.name
        return runCatching { enumValueOf<T>(stored) }.getOrDefault(fallback)
    }

    private fun edit(block: SharedPreferences.Editor.() -> Unit) {
        val editor = prefs.edit()
        editor.block()
        editor.apply()
    }

    fun setTheme(id: String) = edit { putString("theme_id", id) }
    fun setAccentColor(argb: Int) = edit { putInt("accent_color", argb) }
    fun setOnboardingDone(value: Boolean) = edit { putBoolean("onboarding_done", value) }
    fun setWallpaperDim(value: Int) = edit { putInt("wallpaper_dim", value.coerceIn(0, 85)) }
    fun setWallpaperBlur(value: Int) = edit { putInt("wallpaper_blur", value.coerceIn(0, 30)) }

    fun setDisplayPhoto(value: Boolean) = edit { putBoolean("display_photo", value) }
    fun setFullScreen(value: Boolean) = edit { putBoolean("full_screen", value) }
    fun setTextSize(value: Int) = edit { putInt("text_size", value) }
    fun setRowOpacity(value: Int) = edit { putInt("row_opacity", value.coerceIn(0, 100)) }
    fun setColorCalls(value: Boolean) = edit { putBoolean("color_calls", value) }
    fun setRelativeTime(value: Boolean) = edit { putBoolean("relative_time", value) }
    fun setDisplayTime(value: Boolean) = edit { putBoolean("display_time", value) }
    fun setFormatNumber(value: Boolean) = edit { putBoolean("format_number", value) }
    fun setTintSystemBars(value: Boolean) = edit { putBoolean("tint_system_bars", value) }
    fun setDialpadSize(value: Int) = edit { putInt("dialpad_size", value) }
    fun setDialpadTextSize(value: Int) = edit { putInt("dialpad_text_size", value) }
    fun setDisplayAbc(value: Boolean) = edit { putBoolean("display_abc", value) }
    fun setAbcAtRight(value: Boolean) = edit { putBoolean("abc_at_right", value) }
    fun setCountMergedCalls(value: Boolean) = edit { putBoolean("count_merged_calls", value) }
    fun setBadgeCount(value: Boolean) = edit { putBoolean("badge_count", value) }

    fun setScreenRotation(value: Boolean) = edit { putBoolean("screen_rotation", value) }
    fun setAllowExternalViewer(value: Boolean) = edit { putBoolean("allow_external_viewer", value) }
    fun setCallConfirm(value: Boolean) = edit { putBoolean("call_confirm", value) }
    fun setSwipeEnabled(value: Boolean) = edit { putBoolean("swipe_enabled", value) }
    fun setSwipeLeftDialer(value: SwipeAction) = edit { putString("swipe_left_dialer", value.name) }
    fun setSwipeRightDialer(value: SwipeAction) = edit { putString("swipe_right_dialer", value.name) }
    fun setSwipeLeftContacts(value: SwipeAction) = edit { putString("swipe_left_contacts", value.name) }
    fun setSwipeRightContacts(value: SwipeAction) = edit { putString("swipe_right_contacts", value.name) }
    fun setTapDialer(value: TapAction) = edit { putString("tap_dialer", value.name) }
    fun setTapContacts(value: TapAction) = edit { putString("tap_contacts", value.name) }
    fun setTouchTones(value: Boolean) = edit { putBoolean("touch_tones", value) }
    fun setHaptic(value: Boolean) = edit { putBoolean("haptic", value) }
    fun setHapticStrength(value: Int) = edit { putInt("haptic_strength", value) }
    fun setCheckClipboard(value: Boolean) = edit { putBoolean("check_clipboard", value) }
    fun setAutoPopupDialpad(value: Boolean) = edit { putBoolean("auto_popup_dialpad", value) }
    fun setSwipeCloseDialpad(value: Boolean) = edit { putBoolean("swipe_close_dialpad", value) }
    fun setCloseDialpadOnBack(value: Boolean) = edit { putBoolean("close_dialpad_on_back", value) }
    fun setViewCallsOnCallEnd(value: Boolean) = edit { putBoolean("view_calls_on_call_end", value) }
    fun setSwitchToCallLog(value: Boolean) = edit { putBoolean("switch_to_call_log", value) }
    fun setRememberTab(value: Boolean) = edit { putBoolean("remember_tab", value) }
    fun setReverseCallLog(value: Boolean) = edit { putBoolean("reverse_call_log", value) }
    fun setMergeBy(value: Int) = edit { putInt("merge_by", value.coerceIn(0, 2)) }
    fun setCallLogRangeDays(value: Int) = edit { putInt("call_log_range_days", value) }
    fun setLastSeenCallId(value: Long) = edit { putLong("last_seen_call_id", value) }
    fun setShortNumberSpeedDial(value: Boolean) = edit { putBoolean("short_number_speed_dial", value) }
    fun setLastTab(value: String) = edit { putString("last_tab", value) }

    fun setSpeedDial(digit: Int, number: String?) {
        if (digit !in 2..9) return
        edit {
            if (number.isNullOrBlank()) remove("speed_dial_$digit")
            else putString("speed_dial_$digit", number.trim())
        }
    }

    fun resetAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "next_dialer_prefs"
    }
}
