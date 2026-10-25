# Feature Inventory and Decisions

This document maps every meaningful exDialer v198 option to a NEXT Dialer decision. Keys and
defaults are taken from `PrefActivity.java` and `p030d/C0514am.java` in `RE/jadx`.

Legend: **Done** = working in the current checkpoint · **Partial** = present but simplified ·
**Later** = documented for a future checkpoint · **Dropped** = intentionally not carried over.

## Core UX defaults

| Behaviour | Original default | NEXT Dialer |
| --- | --- | --- |
| Launch screen | Dialer with call log + popup dialpad | Keypad tab (if Popup Dialpad is on), else Recents |
| Dialer swipe left | SMS | SMS |
| Dialer swipe right | Call | Call |
| Contacts swipe left | None | SMS (works by default; configurable) |
| Contacts swipe right | None | Call (works by default; configurable) |
| Tap on dialer row | View contact | View number/contact details |
| Tap on contacts row | View contact | View number/contact details |
| Long-press dialpad key | Speed dial (2–9) | Speed dial (2–9) |
| Long-press 0 | `+` | `+` |
| Long-press backspace | Clear | Clear |

## Appearance

Original keys are shown for reference. NEXT Dialer uses its own preference keys but mirrors the
same defaults where the feature is kept.

| Original option | Key / default | Decision | Notes |
| --- | --- | --- | --- |
| Theme | `theme_apk` | **Done** | Five built-in themes; see below. Theme APKs are not supported. |
| Display Photo | `display_photo` = true | **Done** | Contact thumbnails in lists; falls back to a letter avatar. |
| Full Screen | `full_screen` = false | **Done** | Hides the status bar while the app is foreground. |
| Text Size | `text_size` = 0 (Small/Normal/Large/Extra large, values −1…2) | **Partial** | Normal / Large / Extra large. Small maps to Normal for now. |
| Color Calls | `color_calls` = false | **Done** | Colours incoming/outgoing/missed labels and icons. |
| Relative Time | `relative_time` = true | **Done** | "2 min. ago", "Yesterday", "3 days ago". |
| Display Time | `display_time` = false | **Done** | Adds the clock time to day-based call-log labels. |
| Format Phone Number | `format_number` = true | **Done** | Uses the platform formatter and the current region. |
| Theme Switch Time | `theme_switch_time_start/end`, `second_theme` | **Later** | Day/night automatic theme switching in C3. |
| Tint System Bars | `tint_system_bar` = 1 (bitmask: status/navigation) | **Partial** | One toggle draws a status-bar scrim in the theme colour. Navigation-bar tinting is handled by the themed bottom bar. |
| Dialpad Size | `dialpad_width/height`, `dialpad_size` | **Partial** | Normal / Large / Extra large height scale. Free-form resizing is not planned. |
| Dialpad Text Size | `dialpad_text_size` = 0 | **Done** | Normal / Large / Extra large. |
| Use Theme Font | `use_theme_font` = true | **Dropped** | Built-in themes use the system font; theme APKs no longer exist. |
| Use Theme Sound | `use_theme_sound` = true | **Partial** | Replaced by the audible-dialpad-tone toggle; no theme sound packages. |
| Count of Merged Calls | `count_of_merged_calls` = false | **Done** | Shows "4 calls" on a merged row; toggle under Behaviour → Call history. |
| Display ABC | `display_basic_letters` = true | **Done** | Letters under dialpad digits. |
| ABC at Right | `letter_right` = true | **Done** | Letters to the right of the digit instead of underneath. |
| Badge Count | `enable_badge_count` = true | **Done** | In-app badge on Recents plus a summary notification with `setNumber` for launcher badging. Cleared by opening Recents. |
| Display Overflow Menu Button | `display_overflow_menu_button` | **Dropped** | Material bottom navigation and top-bar actions replace it. |
| — | — | **New** | Wallpaper dim slider (0–80 %). |
| — | — | **New** | Wallpaper blur slider (0–30 dp), using Android 12+ cross-window blur. |

## Behaviour

| Original option | Key / default | Decision | Notes |
| --- | --- | --- | --- |
| Screen Rotation | `screen_rotation` = false | **Done** | Off = portrait lock, on = sensor/full user rotation. |
| Allow External Contact Viewer | `allow_ext_contact_viewer` = false | **Done** | Adds an "Open with" action on the contact detail screen. |
| Call Confirm | `call_confirm` = false | **Done** | Confirmation dialog before `ACTION_CALL`. |
| Speed Dial | `speed_dial_2…9` | **Done** | Editor screen; long-press on the dialpad triggers it. |
| Shortcut in Status Bar | `shortcut_in_status_bar` = 0 | **Dropped** | Removed by modern Android notification policy. |
| Swipe Item (Dialer) | swipe left `Sms`, right `Call` | **Done** | Enable switch plus independent left/right actions. Actions: Call, Message, View details, VoIP, Delete call history, None. |
| Swipe Item (Contacts) | both `None` | **Done** | Defaults changed to Message (left) / Call (right) so swipes work out of the box; all actions configurable. |
| Tap on Item (Dialer) | `click_item_action_dialer2` = View contact | **Done** | View details, Call, VoIP, SMS. |
| Tap on Item (Contacts) | `click_item_action_contacts2` = View contact | **Done** | Same. |
| Audible Touch Tones | `touch_tones` = true | **Done** | DTMF via `ToneGenerator`. |
| Haptic Feedback | `haptic_feedback` = false | **Done** | Keypress haptics; strength Weak/Normal/Strong/Extra strong. |
| Haptic Feedback Strength | `haptic_feedback_strength` = 0 | **Done** | Mapped to platform haptic constants. |
| Check Clipboard | `check_clipboard` = true | **Done** | Off by default (privacy). When on, shows the clipboard chip and enables the paste slot in the dialpad bar. |
| Popup Dialpad | `auto_popup_dialpad` = true | **Done** | Controls whether the app opens on the Keypad tab. |
| Swipe to Close Dialpad | `swipe_close_dialpad` = true | **Later** | Swipe-down-to-Recents gesture in C4. |
| Close Dialpad on Back | `close_dialpad_on_back` = false | **Done** | Back from Keypad goes to Recents instead of exiting. |
| View Calls | `view_calls_on_call_end` = true | **Done** | Returns to Recents when placing a call. |
| Open Stock Dialer | `open_stock_dialer` = true | **Dropped** | NEXT Dialer handles `tel:` intents itself. |
| Switch to Call Log | `switch_to_call_log` = false | **Later** | Contact detail → Recents transition in C4. |
| Remember Tab (Contacts) | `remember_tab` = true | **Partial** | Remembers the last bottom-navigation tab across launches. The original remembered the contacts sub-tab; this app currently has no contacts sub-tabs. |
| — | — | **New** | **Merge calls by** (None / Person / Phone number), default Phone number. Mirrors the original `merge_by` semantics. |
| — | — | **New** | **Delete call** — long-press a Recents row to delete the whole merged group (needs `WRITE_CALL_LOG`). |
| — | — | **New** | **Clear call history** in Advanced, with confirmation (needs `WRITE_CALL_LOG`). |
| — | — | **New** | **Delete call history for this number** as a swipe action; confirms, then removes every call-log row matching the number (uses `ContentResolver.delete` and `WRITE_CALL_LOG`). |
| — | — | **New** | **Dialpad search results** use the configured swipe actions, matching contacts behaviour. |
| — | — | **New** | **Animated keypad** collapse/expand when scrolling the recent list; SHOW DIALPAD restores it. |
| — | — | **New** | **Swipe threshold/spacing**: 16 dp dead zone before the reveal starts, 4 dp gap to the row, actions fire only at full reveal. |
| — | — | **New** | **Accent light/dark palettes** added to the standard swatches and theme default. |
| — | — | **New** | **List row background** opacity (0–100 % of the theme default) for call rows, contact rows and search results. |
| — | — | **New** | **Complete contact history**: all individual calls across every number of the contact, unmerged, with a fixed total header. |
| — | — | **New** | **Not answered** label for outgoing calls with zero duration. |
| — | — | **New** | **Show calls from** range (All / 1 / 2 / 3 / 6 months / 1 year). Default All; the 1,000-row limit was removed. |
| — | — | **New** | **Collapsible dialpad** while scrolling the recent list; the call button becomes SHOW DIALPAD. |
| — | — | **New** | **Call row layout** mirrors exDialer: name or number, number line, type icon/date, and last-call type/duration. Contact company appears on the right. |
| — | — | **New** | **History navigation** from a contact pushes onto the back stack instead of clearing it. |
| — | — | **New** | **Theme previews** are minimal Compose layouts rather than colour dots. |
| — | — | **New** | **Per-number detail screen** with Call / Message / Add-or-Edit / Share, all stored numbers, installed WhatsApp/Telegram chat links, call total, and a fixed CALL / SMS / overflow bar. |
| — | — | **New** | Fixed "RECENT CALLS" legend above the dialpad's recent list (does not scroll away). |
| — | — | **New** | Symmetric five-slot dialpad bar: paste · contacts · call · backspace · overflow, with horizontal and bottom padding. |
| — | — | **New** | Swipe reveal panels now show an icon plus a short label (Call / SMS / View / VoIP / Delete). |

Contact-history completeness is **not** an Android API limitation. The call-log provider returns
the individual rows; the earlier NEXT Dialer history screen reused the merged Recents list and
filtered to the tapped number, so it hid the other calls. C6 loads unmerged rows for every number
of the contact. If an OEM prunes old call-log rows, that remains a platform-level limit.

## Language and Filter (deferred)

The user asked to drop this section for now, but it is documented so it can be restored.

| Original option | Key / default | Decision | Notes |
| --- | --- | --- | --- |
| T9 Language | `t9_lang` = auto | **Later** | Current T9 uses a Latin A–Z map plus Unicode normalisation. Additional language maps (Cyrillic, Greek, Hebrew, Arabic, Persian, Korean) are future work. |
| Strict Mode | `strict_mode` = true | **Later** | Current matching is substring-based. |
| Merge Items (calls + T9) | `merge_by` = 2 | **Done** | Implemented as Behaviour → Call history → Merge calls by (None / Person / Phone number). The T9 half is covered by the existing contact search. |
| Contacts to Display | `exclude_account`, `exclude_groups`, `hidden_groups` | **Later** | Current list shows all contacts with at least one phone number. |
| Filter Results Sorting | `filter_sort_option` = 0 | **Later** | Match level first vs. frequency first. |
| Sort by Last Name | `sort_by_last_name` = false | **Later** | Uses the provider's `SORT_KEY_PRIMARY` today. |
| Additional Fields | `additional_fields` = 3 (nickname, address, organisation, note, email bitmask) | **Later** | Search currently covers name and number. |
| Ignore Display Options | `ignore_display_options` = false | **Later** | Depends on display filters. |
| Display Options | per-account visibility | **Later** | Account/group filtering. |

## Advanced

| Original option | Key / default | Decision | Notes |
| --- | --- | --- | --- |
| Keep in Memory | `keep_in_memory` = true | **Dropped** | Modern Android manages process lifetime; the old foreground-service trick is harmful. |
| Hardware Acceleration | `hw_accel` = true | **Dropped** | Always on in modern Compose. |
| Notification Access | — | **Dropped** | Used to replace missed-call notifications; replaced by a future native notification in C2. |
| Replace Missed Call Notification | `replace_missed_call_notifications` = false | **Dropped** | Same as above. |
| VoIP | `exclude_voip_apps` | **Partial** | "VoIP call" uses an app chooser for `tel:`/`voip:` handlers. No per-app exclusion list yet. |
| SIM Region | `sim_country_iso` | **Dropped** | Number formatting uses the platform/current region automatically. |
| Roaming Auto Dial | `roaming_auto_dial` = false | **Dropped** | Modern dialers/telecom handle national dialling. |
| Reverse Call Log | `reverse_call_log` = false | **Done** | Oldest-first display, including group order. |
| Appointment Text | `appointment_title/desc` | **Dropped** | Calendar appointment creation is outside the dialer's core job. |
| Short Number as Speed Dial | `short_num_as_speed_dial` = true | **Done** | A single digit 2–9 resolves through the speed-dial table before calling. |
| Hangul Romanization | `hangul_romanized` = false | **Dropped** | Tied to the deferred Korean T9 work. |
| Backup | `backup.bin` on external storage | **Later** | JSON export via SAF in C5. |
| Restore | `backup.bin` | **Later** | JSON import in C5. |
| — | — | **New** | Reset all settings. |
| — | — | **New** | Default-dialer role request (C5). |

## Themes

The original theme picker lists five built-in themes. NEXT Dialer reproduces their character
rather than their exact assets.

| Theme | Original id | Character | NEXT Dialer |
| --- | --- | --- | --- |
| Lollipop | `com.modoohut.dialer` | Light cards, blue top bar, pink dialpad FAB | Light blue theme |
| Crystal | `c_com.modoohut.dialer` | Dimmed wallpaper, translucent dark panels, blue digits, green call button | Wallpaper theme, default |
| Crystal Simp | `cc_com.modoohut.dialer` | Flat translucent wallpaper variant | Wallpaper theme with lighter scrim |
| Simplism | `s_com.modoohut.dialer` | Minimal light grey | Light grey theme |
| Classic | `com.modoohut.dialer.classic` | White list, green call button, black text | White/green theme |

Original Crystal palette (from `values/colors.xml` and `drawables.xml`):

- content overlay `#80000000`, transparent list, panel `#80303030`
- primary text `#e0ffffff`, secondary `#80ffffff`, accent `#c033b8ff`
- status bar `#c0296788`, navigation bar `#c0040a0d`
- missed call `#e0ff2e58`, outgoing `#ff87c03d`, incoming `#ff32b1e1`

## Plugins

The "Plugin" entry in the original preferences is **not** a third-party APK plugin system. It
opens the UI-language screen, which enumerates locales bundled in the APK assets
(`C0559m.m1408a(resources)`), plus a "Get More Plugins" link. The only real plugin-like
mechanism is the separate theme APK package (`theme_apk`), which is loaded as a resource
package. NEXT Dialer does not load external resource packages; all themes are built in.

## New / modernised features

- Runtime permission onboarding with a usable no-permission mode.
- Wallpaper dim and blur controls (the headline visual upgrade).
- Edge-to-edge layout and light/dark system-bar icons.
- Material 3 dialogs, sliders, and navigation.
- `tel:` intent handling so NEXT Dialer can be chosen for links and other apps.
- T9 matching with Unicode normalisation (Latin).
- Search across both recents and contacts.
- Contact details with call/SMS/edit/share and optional external viewer.
- Per-number detail screen with call total, call-history filter, and installed WhatsApp/Telegram chat links.
- Fixed recent-calls legend, symmetric five-slot dialpad bar, and labelled swipe actions.

WhatsApp and Telegram are detected through the manifest `<queries>` entries. Only public chat deep
links are used (WhatsApp `wa.me`, Telegram `tg://resolve?phone=`); neither app exposes a stable
public voice/video-call intent, so exDialer's Voice/Video rows are intentionally not reproduced.
- Speed-dial editor with validation.
- Settings reset.

## Deliberately not implemented

- In-call screen, call recording, SIP stack.
- Google Play licensing, donation keys, trial expiry.
- Theme APK loading, UI-language plugin packages.
- Notification-listener-based missed-call replacement.
- Foreground "keep in memory" service.
- Calendar appointment creation.
- Cloud backup/restore.
