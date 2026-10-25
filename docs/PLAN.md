# NEXT Dialer — Plan

## Goal

Rebuild the useful parts of exDialer v198 as a modern, maintainable, **license-free** Android
dialer. The result should feel like the original where it matters:

- Dialer-first launch and a large, responsive dialpad.
- Contacts and call history in the same app, without a separate contacts app.
- Swipe a row for SMS or recall; configure the swipe and tap actions.
- The dimmed-wallpaper "Crystal" look, now with adjustable dim and blur.
- The Appearance and Behaviour options that still make sense on modern Android.

It is not a pixel-perfect port. The original is a 2013-era app targeting API 26; NEXT Dialer
uses Compose, runtime permissions, edge-to-edge layout, and modern intents.

## Non-goals (for now)

- Reimplementing the in-call UI. Calls are handed to the system Telecom/phone app.
- Google Play licensing, donation keys, or any paid-feature gate.
- Theme APK plugins, T9 language plugins, or third-party launcher integrations.
- Cloud sync, backup services, or accounts.

## Technology

| Item | Choice |
| --- | --- |
| Language | Kotlin 2.2 |
| UI | Jetpack Compose + Material 3 |
| Min SDK | 26 (Android 8.0) |
| Target / compile SDK | 36 (Android 16) |
| Build | Gradle 8.13, AGP 8.13.2, JDK 21 |
| Package | `com.nextdialer` |
| Signing | Local keystore `work/nextdialer.keystore` |

Why minSdk 26: the original supports ancient devices, but 26 gives adaptive icons, modern
contacts/call-log providers, and a reasonable Compose baseline. Android 8+ covers the realistic
set of devices for this project.

## Architecture

```
MainActivity (single activity, edge-to-edge)
└── NextDialerApp
    ├── SettingsStore (SharedPreferences + StateFlow)
    ├── MainViewModel (permissions, contacts, call log, ContentObserver)
    ├── DialerTheme + WallpaperBackground (dim / blur)
    ├── PermissionScreen (first run)
    └── MainScaffold
        ├── RecentsScreen / ContactsScreen / DialpadScreen
        ├── Bottom action bars (dialpad and list variants)
        ├── NumberDetailScreen (per-number contact detail)
        └── Settings screens
```

Data access is direct through `ContentResolver`:

- `ContactsContract.CommonDataKinds.Phone` for the contact list.
- `ContactsContract.Contacts` / `Email` for details.
- `CallLog.Calls` for recents.

`MainViewModel` registers a `ContentObserver` and reloads on changes. No database is copied or
owned by the app; the system providers remain the source of truth.

### Theme model

A `DialerTheme` describes colours and whether the system wallpaper is used. Crystal and Crystal
Simp are wallpaper themes; Lollipop, Simplism, and Classic are opaque. Wallpaper themes use
translucent panels and white text, matching the original Crystal palette.

### Wallpaper and blur

The wallpaper is **not** read into a bitmap. The activity window is made translucent and given
`FLAG_SHOW_WALLPAPER`, so the real system wallpaper (including live wallpapers) is composited
behind the app by SurfaceFlinger. A Compose scrim supplies the dim level, and the UI panels are
translucent. This avoids storage permissions and works on every Android version.

Why not read it: `WallpaperManager.getDrawable()` requires `READ_EXTERNAL_STORAGE` up to Android
12; on Android 13 it returns the default wallpaper, and from Android 14 it always throws. Reading
the wallpaper is therefore not viable for a normal app on modern Android.

Blur uses the platform cross-window blur (`FLAG_BLUR_BEHIND` + `blurBehindRadius`) on Android 12+,
which blurs the wallpaper behind the window. Devices that do not support cross-window blur simply
show the dimmed wallpaper. Below Android 12 the blur slider has no effect (dim still works).

A top-down shade (42 % black, fading out by 34 % of the screen height) is drawn over wallpaper
themes so the header, search field, and first list rows stay readable on bright wallpapers.

## Checkpoints

Each checkpoint ends with a signed APK in `builds/` and an updated devlog.

### C1 — Core shell (complete)

- Project scaffold, signing, reproducible build script.
- Permission onboarding.
- Dialpad, DTMF, haptics, long-press `+`/clear.
- Recents and contacts with search.
- Contact details with call/SMS/edit/share.
- Swipe actions and tap actions.
- Crystal + four other themes.
- Wallpaper dim/blur.
- Appearance/Behaviour/Advanced settings shell.
- Speed-dial editor.
- Portrait lock, full screen, system-bar icon contrast.

### C2 — Call-log fidelity (complete)

- Merge repeated calls using the original `merge_by` semantics (0 = None, 1 = Person,
  2 = Phone number; default 2). The newest call represents the row; older calls increment
  its count and unread-missed tally.
- Optional "Count of merged calls" subtitle.
- Reverse call log option wired end to end.
- Missed-call badge on Recents plus a summary notification that drives launcher badging.
  Read state is tracked locally with a `last_seen_call_id` watermark, so no call-log writes
  are needed to clear it.
- Long-press a row to delete that merged group; "Clear call history" in Advanced. Both need
  `WRITE_CALL_LOG` and show a confirmation dialog.
- Favourites as All/Favourites pills inside Contacts (a separate tab was not needed).

Known limitation: while the process is dead, a new missed call cannot raise a notification.
Reliable background notification needs the default-dialer role or a call-screening service,
which is deferred.

### C3 — Shell fidelity and compositing fixes (complete)

- Original-style bottom bars: dialpad = contacts, call, backspace, overflow; lists = contacts,
  SHOW DIALPAD, overflow.
- Fixed dialpad digit alignment with reserved digit/label slots.
- Fixed wallpaper compositing: genuinely translucent window, fixed wallpaper while scrolling,
  no black flash, blur preserved.
- Removed top branding; preserved tab state across Settings via `SaveableStateHolder`.
- Enabled default swipe actions and smoothed horizontal drag handling.
- Settings → Back returns to the pre-Settings screen with the wallpaper, no snapshot stacking.

### C4 — Details, swipe completion and bar polish (complete)

- Call-history and contacts swipes default to SMS (left) / Call (right); both configurable.
- Added **Delete call history** as a swipe action (confirmation + call-log delete).
- Per-number detail screen: Call, Message, Add/Edit, Share, all numbers, call total, fixed
  CALL / SMS / overflow bar, and a filtered call-history jump.
- WhatsApp and Telegram chat rows when those apps are installed.
- Dialpad bar is now five symmetric slots: paste · contacts · call · backspace · overflow.
  Clipboard support is opt-in (off by default), and the paste slot is disabled when unusable.
- Fixed "RECENT CALLS" legend, horizontal bar padding, and subtle bottom padding under the call
  button.
- Swipe reveal panels now pair icons with short labels.

### C5 — Appearance completion

- Theme switch time (day/night second theme).
- Text-size and dialpad-size refinements across all screens.
- Theme font/sound equivalents where practical (dialpad tone choice).
- Optional "transparent panels" / scrim strength presets.
- Badge styling polish (colour, size, max count display).

### C6 — Behaviour completion

- Speed-dial polish and validation.
- Clipboard number parsing improvements.
- Swipe-to-close dialpad gesture.
- Contact-detail → call-log switch option (filtered history already ships in C4).
- External contact viewer chooser.

### C7 — Advanced

- JSON backup/restore through the Storage Access Framework.
- Optional default-dialer role request and `ACTION_DIAL` integration.
- Dual-SIM awareness where the platform exposes it.
- Roaming/national-number handling.

### C8 — Polish and release

- Tablet/foldable layout.
- Accessibility pass (TalkBack, touch targets, contrast).
- Localisation scaffolding.
- Performance pass (startup, list scrolling, wallpaper memory).
- Final documentation and a `1.0` build.

## Testing

Manual test matrix on the API 28 emulator and, where possible, a physical device:

1. Fresh install → permission onboarding → grant/deny paths.
2. Dialpad: tones, haptics, T9, clipboard, long-press, speed dial.
3. Recents: grouping, relative/absolute time, colours, call/SMS, swipe both directions.
4. Contacts: search, index jump, swipe, tap, details, edit/share.
5. Settings: every toggle changes behaviour without a restart.
6. Themes: all five, wallpaper dim/blur, light/dark system bars.
7. Rotation/portrait lock, full screen, back handling.
8. No permissions: dialpad and settings remain usable; empty states explain why.

Emulator constraints and commands are in [RE_NOTES.md](RE_NOTES.md). The current machine has no
hardware virtualisation, so the API 28 emulator runs under software emulation and is slow but
usable for functional checks.

## Release process

1. Bump `versionCode` / `versionName` in `app/build.gradle.kts`.
2. Run `.\build.ps1`.
3. Install and smoke-test the produced APK.
4. Update `docs/DEVLOG.md`, `docs/FEATURES.md`, and the status in this file.
