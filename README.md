# NEXT Dialer

A modern, keyless reimplementation of the classic **exDialer** experience: a fast dialer-first
app with contacts, call history, swipe actions, dimmed wallpaper, and optional blur.

NEXT Dialer is a clean-room Kotlin / Jetpack Compose app. It does **not** require the old
`com.modoohut.dialer.donate` key package and contains no Google Play licensing code.

> Status: **Checkpoint 5 — list details, accent control, and theme previews**. The signed release
> is built and verified on the API 28 emulator (company-field row confirmed on a physical phone).
> It adds exDialer-style two/three-line call rows with company and last-call details, a full
> accent-colour picker that recolours the Crystal status bar and highlights, minimal theme
> previews, and full-height rounded swipe actions. See [docs/PLAN.md](docs/PLAN.md) for the
> roadmap and [docs/DEVLOG.md](docs/DEVLOG.md) for the latest test/build notes.

## Screenshots

| Crystal (default) | Recents | Contacts |
| --- | --- | --- |
| ![Keypad](docs/screenshots/release-keypad-crystal.png) | ![Recents](docs/screenshots/release-recents-crystal.png) | ![Contacts](docs/screenshots/release-contacts-crystal.png) |

| Contact detail | Appearance | Themes |
| --- | --- | --- |
| ![Contact detail](docs/screenshots/release-contact-detail-crystal.png) | ![Appearance](docs/screenshots/release-appearance-crystal.png) | ![Theme picker](docs/screenshots/release-theme-picker.png) |

The Classic (opaque) theme for comparison:

![Classic keypad](docs/screenshots/release-keypad-classic.png)

### Checkpoint 2 — call-log fidelity

| Merged recents | Missed-call badge | Summary notification |
| --- | --- | --- |
| ![Merged recents](docs/screenshots/c2-recents-merged.png) | ![Badge](docs/screenshots/c2-badge-keypad.png) | ![Notification](docs/screenshots/c2-missed-notification.png) |

| Favourites | Delete confirmation |
| --- | --- |
| ![Favourites](docs/screenshots/c2-favourites.png) | ![Delete dialog](docs/screenshots/c2-delete-dialog.png) |


### Checkpoint 4 — details, swipes, and bottom bars

| Keypad (five-slot bar) | Fixed recent-calls legend | Recents |
| --- | --- | --- |
| ![Keypad](docs/screenshots/c4-keypad.png) | ![Scrolled keypad](docs/screenshots/c4-keypad-scroll.png) | ![Recents](docs/screenshots/c4-recents.png) |

| Contacts | Contact detail | Number detail |
| --- | --- | --- |
| ![Contacts](docs/screenshots/c4-contacts.png) | ![Contact detail](docs/screenshots/c4-contact-detail.png) | ![Number detail](docs/screenshots/c4-number-detail.png) |

| Filtered cross-number history | Clipboard paste enabled | Settings |
| --- | --- | --- |
| ![Filtered history](docs/screenshots/c4-history-filter.png) | ![Clipboard paste](docs/screenshots/c4-clipboard-paste.png) | ![Settings](docs/screenshots/c4-settings.png) |


### Checkpoint 5 — list details, accents, and theme previews

| Call rows | Accent override | Accent picker |
| --- | --- | --- |
| ![Call rows](docs/screenshots/c5-call-rows.png) | ![Teal accent](docs/screenshots/c5-accent-teal.png) | ![Accent picker](docs/screenshots/c5-accent-picker.png) |

| Theme previews | Collapsed dialpad | Call range |
| --- | --- | --- |
| ![Theme previews](docs/screenshots/c5-theme-previews.png) | ![Collapsed dialpad](docs/screenshots/c5-collapsed-dialpad.png) | ![Call range](docs/screenshots/c5-call-range.png) |

| History from a contact | Swipe reveal |
| --- | --- |
| ![History from detail](docs/screenshots/c5-history-from-detail.png) | ![Swipe reveal](docs/screenshots/c5-swipe-reveal.png) |


## Why two apps?

| App | Package | Purpose |
| --- | --- | --- |
| NEXT Dialer | `com.nextdialer` | New Compose app, actively developed. |
| Legacy unlocked | `com.modoohut.dialer` | Patched original v198, usable immediately while NEXT Dialer matures. |

The legacy build is at `builds/legacy/exDialer-198-unlocked.apk`. It is the original APK with
the Pro/key check neutralised; it still uses the original package name so it can coexist with
NEXT Dialer.

## Features in Checkpoint 5

- Dialpad with DTMF tones, haptics, long-press `0` for `+`, long-press backspace to clear.
- Five-slot dialpad bar (paste · contacts · call · backspace · overflow) with balanced padding.
- exDialer-style call rows: name (or number), number with type for contacts, call-type icon and
  date, and the last call's type and duration on the right. The contact company/organisation is
  shown in the right-hand details block when available.
- T9 contact search while typing, opt-in clipboard paste, and speed-dial long-press.
- **Per-number detail screen**: Call / Message / Add-or-Edit / Share, every stored number,
  call total, filtered call history, and installed WhatsApp/Telegram chat links.
- **Accent colour picker** in Appearance with 12 colours plus the theme default. The choice
  recolours the Crystal status-bar tint, dialpad digits, sliders, switches and highlights.
- Theme picker uses **minimal layout previews** built from Compose shapes (no images).
- **Collapsible dialpad**: scrolling the recent list hides the keypad and swaps the green call
  button for a SHOW DIALPAD action; tapping it restores the keypad without losing scroll position.
- **Call-log range** setting (All / 1 / 2 / 3 / 6 months / 1 year), defaulting to All, with the
  previous 1,000-row cap removed. (If an OEM provider itself prunes older rows, the app can only
  show what the provider returns.)
- **History navigation fix**: opening call history from a contact keeps it on the back stack, so
  Back returns to the contact detail instead of exiting the app.
- **Swipe reveal panels** now fill the row height and are rounded on both edges, with an icon
  and label.
- Recents grouped by day, colour-coded call types, relative/absolute time, and call-back.
- **Call-log merging** by person or phone number, with an optional "N calls" count.
- **Missed-call badge** on Recents and a summary notification that also drives the launcher
  badge. Opening Recents clears both.
- **Delete** a merged call group by long-pressing a row, **swipe-delete** all history for a
  number, or **clear** the whole call log from Advanced (all require `WRITE_CALL_LOG` and
  confirm first).
- **Favourites** — All/Favourites pills inside Contacts.
- Contacts list with alphabet index, search, avatars, and contact details.
- **Swipe actions** for recents, contacts, and the dialpad's recent list: Call / Message /
  View details / VoIP / Delete history / None, with labelled reveal panels.
- Fixed "RECENT CALLS" legend above the dialpad list; the list scrolls under it.
- Configurable tap actions, call confirmation, and short-number speed dial.
- Crystal (real system wallpaper), Crystal Simp, Lollipop, Simplism, and Classic themes.
- Wallpaper **dim** control on every Android version and **blur** on Android 12+ (cross-window
  blur; no storage permission and no wallpaper snapshot).
- A subtle top shade keeps headers and search fields readable over bright wallpapers.
- Edge-to-edge UI, light/dark system-bar icons, portrait lock option.
- Appearance / Behaviour / Advanced settings, plus theme picker and speed-dial editor.

## Build

Requirements:

- Windows with Android SDK platform 36 and build-tools 36.1.0.
- JDK 21 (a portable copy is expected at `tools/jdk21/...`).
- Gradle 8.13 wrapper (generated in this repo).

```powershell
.\build.ps1
```

The script sets `JAVA_HOME` to the bundled JDK 21, builds a signed release APK, and copies it to
`builds/next-dialer-<version>.apk`.

Manual build:

```powershell
$env:JAVA_HOME = "C:\path\to\jdk-21"
.\gradlew.bat assembleRelease
```

Signing uses `work/nextdialer.keystore` (password/alias: `nextdialer`). This is a local
development key; replace it for any public distribution.

## Install and test

```powershell
adb install -r builds\next-dialer-0.1.0-c5.apk
adb shell pm grant com.nextdialer android.permission.READ_CONTACTS
adb shell pm grant com.nextdialer android.permission.READ_CALL_LOG
adb shell pm grant com.nextdialer android.permission.WRITE_CALL_LOG
adb shell pm grant com.nextdialer android.permission.CALL_PHONE
```

The first launch shows a permission onboarding screen. Contacts and call-log access are optional;
the dialpad still works without them.

## Repository layout

```
app/                     Kotlin + Compose application
builds/                  Installable APKs (legacy + NEXT Dialer)
docs/                    Plan, feature matrix, reverse-engineering notes, devlog
RE/                      Extracted original APK, jadx sources, screenshots, UI dumps
tools/                   Local JDK, Gradle/emulator helpers, apktool
work/                    Signing key and intermediate build artefacts
build.ps1                Reproducible release build script
```

## Documentation

- [docs/PLAN.md](docs/PLAN.md) — goals, architecture, checkpoint roadmap.
- [docs/FEATURES.md](docs/FEATURES.md) — original option inventory and keep/adapt/drop decisions.
- [docs/RE_NOTES.md](docs/RE_NOTES.md) — reverse-engineering findings and emulator commands.
- [docs/DEVLOG.md](docs/DEVLOG.md) — dated build/test log.

## Credits

Original exDialer by modoohut. NEXT Dialer is an independent reimplementation for personal use
and preserves the spirit of the original MiUI-style dialer.
