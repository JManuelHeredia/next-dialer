# Development Log

## 2026-10-24 — Environment, legacy unlock, project start

### Environment

- Android Studio updated to 2026.2.1 with bundled JBR 25.
- Re-checked emulator acceleration: still no hypervisor (`HypervisorPresent=False`,
  `VirtualizationFirmwareEnabled=False`, no WHPX/AEHD). Emulator 37.2.12 still exits silently
  under TCG.
- Kept the working workaround: emulator **31.1.4** with `-accel off`, API 28 x86 AVD
  `NEXT_API28`, software GPU.
- Downloaded Temurin **JDK 21.0.12.1** to `tools/jdk21/` for Gradle/AGP. Gradle 8.13 wrapper
  distribution is present in the user Gradle cache.
- Verified Google Maven and Maven Central access; selected AGP 8.13.2, Kotlin 2.2.21,
  Compose BOM 2025.10.00, compile/target SDK 36.

### Legacy fallback

- Confirmed the original's Pro check is only a package-presence check for
  `com.modoohut.dialer.donate`.
- Patched `TheApp.b()` to set `f802b = true` unconditionally.
- Built and signed `builds/legacy/exDialer-198-unlocked.apk`; About reports "v198 Pro" with no
  key package installed.

### Original UI / theme capture

- Captured Preferences root, Appearance, Behaviour, Advanced, theme picker, contacts, and
  Crystal/Classic dialer screenshots.
- Confirmed Crystal = `c_com.modoohut.dialer`, with `#80000000` content overlay and translucent
  panels. Theme picker writes `theme_apk` via a context menu (Apply Theme / Set as Second
  Theme).
- Confirmed "Plugin" is the bundled UI-language list, not an external plugin system.

### NEXT Dialer scaffold

- Created the plan, feature matrix, RE notes, and this log.
- Scaffolded `com.nextdialer` with Kotlin 2.2.21 + Compose, minSdk 26, compile/target 36.
- Implemented checkpoint 1:
  - single-activity edge-to-edge shell and bottom navigation;
  - permission onboarding and no-permission mode;
  - dialpad with DTMF, haptics, T9 search, clipboard suggestion, speed dial;
  - recents grouped by day with relative/absolute time and call colours;
  - contacts with alphabet index, search, avatars, and details;
  - swipe actions and configurable tap actions;
  - five themes plus wallpaper dim/blur;
  - Appearance / Behaviour / Advanced settings, theme picker, speed-dial editor;
  - portrait lock, full screen, status-bar tint, system-bar icon contrast.

### Build/test

- Release build succeeds with AGP 8.13.2 / Gradle 8.13 / JDK 21.
- APK: `builds/next-dialer-0.1.0-c1.apk` (11.49 MB, signed).
- Installed on the API 28 `NEXT_API28` emulator; contacts, call log and phone permissions granted
  with `pm grant`.
- Fixed a startup crash on fresh installs: `MainViewModel` registered its `ContentObserver` on the
  contacts provider before `READ_CONTACTS` was granted, which throws `SecurityException`. The
  observer is now registered/unregistered as permissions change, and provider reads are guarded.

### Wallpaper diagnosis and fix

- Symptom: Crystal and Crystal Simp showed the gradient fallback instead of the real wallpaper.
- Root cause: `WallpaperManager.getDrawable()` threw
  `SecurityException: read wallpaper ... requires android.permission.READ_EXTERNAL_STORAGE`.
- Researched the platform behaviour:
  - up to Android 12 the call needs `READ_EXTERNAL_STORAGE`;
  - on Android 13 it returns the default wallpaper or throws;
  - from Android 14 it always throws for normal apps.
- Replaced the bitmap loader with the platform show-wallpaper approach:
  - the activity window is translucent and uses `FLAG_SHOW_WALLPAPER`;
  - the real wallpaper (including live wallpapers) is composited behind the app;
  - Compose draws only the dim scrim and translucent panels;
  - no storage permission, no wallpaper snapshot, no gradient fallback.
- Blur now uses Android 12+ cross-window blur (`FLAG_BLUR_BEHIND` + `blurBehindRadius`); below
  Android 12 only dim is available and the Appearance screen says so.
- Removed `WallpaperLoader.kt` and the temporary `NextWallpaper` logging.

### Emulator smoke test (API 28)

- Keypad: digits, ABC letters, backspace, long-press clear, DTMF/haptics settings present.
- T9: typing `25423` matched Alice (`55512340001`).
- Recents: grouped Today/Yesterday/Earlier, search header, call-type colours, call button.
- Contacts: A–L groups, alphabet index, avatars, numbers.
- Contact detail: two numbers, Call/Message/Share, View call history.
- Themes: switched Crystal → Classic (opaque, wallpaper flag cleared) → Crystal (wallpaper
  returns), system-bar icons follow light/dark.
- Dim slider: 55 % → 70 % persisted in `next_dialer_prefs.xml` and applied live.
- Swipe left on a recents row (default SMS) opened the Messaging app for Alice.
- The AOSP wallpaper cropper crashes on this image, so a debug-only `SetWallpaperActivity` is
  used to install the test wallpaper; it is not part of release builds.

### Release verification (C1)

- Rebuilt the signed release after the wallpaper and `MainViewModel` fixes:
  `builds/next-dialer-0.1.0-c1.apk` (11.51 MB).
- Fresh install of `com.nextdialer` (previous debug package removed) with the three runtime
  permissions granted via `pm grant`; no startup crash, no `SecurityException` in logcat.
- Verified the real wallpaper is composited behind the UI with `FLAG_SHOW_WALLPAPER` and that
  the dim scrim is applied at the default 55 %.
- Added a subtle top shade (black 42 % → transparent at 34 % height) for wallpaper themes so
  the screen title, search field, and first rows stay readable over bright wallpapers. Also
  raised the status-bar tint from 55 % to 72 % alpha on wallpaper themes.
- Captured release screenshots in `docs/screenshots/`: keypad, recents, contacts, contact
  detail, settings, appearance, theme picker (Crystal), plus the Classic theme for comparison.
- Re-ran the theme round-trip Crystal → Classic → Crystal and confirmed the wallpaper flag and
  system-bar icon contrast switch correctly.

### Next

- Verify cross-window blur on an Android 12+ device/emulator when hardware acceleration is
  available.

## 2026-10-25 — C2: call-log fidelity

### Implementation

- `CallEntry` now carries `count`, `ids` (every merged call-log id, newest first) and
  `unreadIds` (missed calls still marked new). `DeviceRepository.loadCalls(contacts, mergeBy)`
  merges by person (`contactId`) or phone number (`PhoneUtils.normalize`), keeping the newest
  call as the visible row. `mergeBy` is read from settings on every reload.
- Added `deleteCalls(ids)` and `clearCallLog()`; the manifest gained `WRITE_CALL_LOG` and the
  permission request includes it. Long-press a Recents row → confirmation → delete the merged
  group. Advanced → "Clear call history" with its own confirmation.
- Missed-call badge: `MissedCallNotifier` posts a single summary notification with
  `setNumber(count)` (which also drives launcher badging) and a channel that shows a badge.
  The in-app badge is a `BadgedBox` on the Recents tab. Read state uses a local
  `last_seen_call_id` watermark: opening Recents stores the newest id and clears the badge and
  notification without writing to the call log.
- Favourites are All/Favourites pills inside Contacts; the Favourites view filters on the
  provider's `STARRED` flag and has a dedicated empty state.
- Behaviour gained a Call history section: Merge calls by (None / Person / Phone number),
  Count of merged calls, and Badge count.

### Build/test (debug)

- Debug build installed on `NEXT_API28`; `READ_CONTACTS`, `READ_CALL_LOG`, `WRITE_CALL_LOG`
  and `CALL_PHONE` granted with `pm grant`.
- Inserted repeated calls for Alice: Recents showed one row with "4 calls" and a duration.
- Inserted a second missed call for Bob: the row showed "2 calls" (later "3 calls" after one
  more) and the in-app badge counted the unread missed calls.
- Long-press → "Delete 3 calls?" removed the merged group; call-log row count dropped
  accordingly. Advanced → Clear call history works the same way.
- Summary notification verified in the shade: "2 missed calls / Open NEXT Dialer to see who
  called." Launcher badge is driven by the same notification.
- Starred Alice in the contacts provider; the Favourites pill showed only Alice.

### Build/test (release)

- Bumped to versionCode 2 / versionName `0.1.0-c2`; `build.ps1` writes
  `builds/next-dialer-0.1.0-c2.apk` (11.53 MB, signed).
- Fresh install, all permissions granted, no crash and no `SecurityException` in logcat.
- Captured C2 screenshots in `docs/screenshots/`: merged Recents ("4 calls"/"3 calls"),
  missed-call badge on the Keypad, the summary notification, Favourites, and the delete
  confirmation.

### Known limitation

- A missed call that arrives while the process is dead cannot raise a notification; discovery
  relies on the call-log `ContentObserver` while the app is alive. Reliable background
  notification needs the default-dialer role or a call-screening service, deferred.

### Next

- Start C3: appearance completion (day/night theme switch, badge styling, text/dialpad size
  refinements, dialpad tone choice, scrim strength presets).
- Verify cross-window blur on an Android 12+ device/emulator when hardware acceleration is
  available.

## 2026-10-25 — C3: shell fidelity and compositing fixes

### Implementation

- Replaced the Material bottom navigation with original-style bars: dialpad = contact shortcut,
  green call, backspace, overflow; lists = contact shortcut, SHOW DIALPAD, overflow.
- Aligned dialpad digits with fixed digit and label slots, including empty labels for `1`, `*`
  and `#`, so every column shares the same x axis.
- Made the wallpaper genuinely stable: the window is translucent with `windowShowWallpaper`,
  runtime transparent/opaque backgrounds per theme, `FLAG_SHOW_WALLPAPER`, and Android 12+
  cross-window blur. Compose only draws the dim scrim and panels.
- Removed the top branding and gear; settings moved into the overflow menu.
- Kept Main tab and per-screen state across pushed routes with `rememberSaveableStateHolder`.
- Reworked `SwipeActionRow` to use a synchronous drag offset (no coroutine per pointer event),
  removed `BoxWithConstraints`, and used a fixed 64 dp trigger threshold.
- Settings → Back returns to the previous tab with the wallpaper visible; no stacked-transparent
  activity or Settings snapshot.
- Contacts alphabet index end padding increased so it is no longer clipped.

### Build/test

- `builds/next-dialer-0.1.0-c3.apk` (versionCode 3, `0.1.0-c3`, 11.53 MB, signed).
- Verified on Xiaomi 2206122SC (Android 13): fixed wallpaper during scrolling, settings-back
  from Recents and Contacts, both swipe directions, dialpad alignment, and the new bars.
- `dumpsys gfxinfo` over a controlled list scroll: 853 frames, 36 janky (4.22 %), 90th 10 ms,
  95th 13 ms, 99th 18 ms, 0 missed vsync.

## 2026-10-25 — C4: details, swipe completion and bar polish

### Implementation

- Contacts swipes now default to Message (left) / Call (right); the dialpad's recent list uses
  the same configured swipe actions.
- Added `SwipeAction.DELETE`: deletes every call-log row for the number after confirmation.
  `MainViewModel.deleteCallsForNumber` collects the merged ids locally and calls the existing
  `ContentResolver.delete` path, so formatted numbers match correctly.
- Swipe reveal panels now show an icon plus a short label (Call / SMS / View / VoIP / Delete).
- Reworked the per-number detail screen (used by tap-to-view and swipe-to-view):
  - cached name/photo support even when the number is not a stored contact;
  - Call, Message, Add-or-Edit, Share and optional external viewer actions;
  - all stored numbers with call/message buttons and the tapped number highlighted;
  - call total in a fixed bottom strip, tapping it opens Recents filtered to the number;
  - fixed CALL / SMS / overflow bar (history, edit, share, copy, delete history);
  - WhatsApp / WhatsApp Business / Telegram chat rows when installed.
- Manifest `<queries>` entries allow detecting WhatsApp and Telegram. Only public chat deep
  links are used; their voice/video-call intents are private.
- Dialpad bar rebuilt as five equal slots: paste · contacts · call · backspace · overflow, with
  16 dp horizontal padding and 8 dp bottom padding inside the navigation inset.
- Clipboard support is off by default (`check_clipboard` = false). When enabled, the paste slot
  and chip activate only for phone-like clipboard text; the slot stays disabled otherwise.
- Moved the "RECENT CALLS" legend outside the `LazyColumn`, so it stays fixed while the list
  scrolls. Added matching surface backgrounds to those rows.
- Recents search now matches normalized digits, and a requested query can be pushed from the
  detail screen's call total.

### Build/test (API 28 emulator, release)

- `builds/next-dialer-0.1.0-c4.apk` (versionCode 4, `0.1.0-c4`, 11.56 MB, signed).
- Verified: fixed legend while scrolling, wallpaper stable, five-slot bar with paste disabled by
  default, clipboard toggle + paste into the dialpad, detail screen with cached name, filtered
  call history, and "Delete call history" confirmation removing the call-log rows.
- Swipe-left on a Recents row opened the Messaging app; the same code path serves Call/View/
  VoIP/Delete from the Behaviour options.
- Emulator input on this image cannot be held mid-gesture, so the labelled reveal was validated
  by code review and by the action firing; the real device remains the visual reference.

### Next

- Appearance completion (C5) and then the remaining Behaviour items (C6).
- Re-verify the labelled swipe reveal and cross-window blur on the physical Android 13 device.
