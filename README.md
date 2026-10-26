# NEXT Dialer

A modern, keyless reimplementation of the classic **exDialer** experience: a fast dialer-first
app with contacts, call history, swipe actions, dimmed wallpaper, and optional blur.

NEXT Dialer is a clean-room Kotlin / Jetpack Compose app. It does **not** require the old
`com.modoohut.dialer.donate` key package and contains no Google Play licensing code.

> Status: **Release candidate RC2**. The signed APK is built and verified on the API 28 emulator.
> RC2 adds the complete (unmerged) Recents list, long-press call actions, contact-source
> checkboxes, dialpad width/height/position and background controls, unified bottom-bar styling,
> custom/auto accent colours (including whitesmoke and near-black), and disables row actions in
> the contact history view. See [docs/PLAN.md](docs/PLAN.md) for the roadmap and
> [docs/DEVLOG.md](docs/DEVLOG.md) for the latest test/build notes.

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


### Release candidate RC1 (C6)

| Complete contact history | Swipeable search result | Row opacity |
| --- | --- | --- |
| ![Contact history](docs/screenshots/c6-contact-history.png) | ![Search swipe](docs/screenshots/c6-search-swipe.png) | ![Row opacity](docs/screenshots/c6-row-opacity.png) |

### Release candidate RC2

| Complete Recents | Long-press actions | Contact sources |
| --- | --- | --- |
| ![Recents](docs/screenshots/c7-recents-all.png) | ![Long press](docs/screenshots/c7-long-press.png) | ![Contact sources](docs/screenshots/c7-contact-sources.png) |

| Unified dialpad bar |
| --- |
| ![Dialpad bar](docs/screenshots/c7-dialpad-bar.png) |

## Why two apps?

| App | Package | Purpose |
| --- | --- | --- |
| NEXT Dialer | `com.nextdialer` | New Compose app, actively developed. |
| Legacy unlocked | `com.modoohut.dialer` | Patched original v198, usable immediately while NEXT Dialer matures. |

The legacy build is at `builds/legacy/exDialer-198-unlocked.apk`. It is the original APK with
the Pro/key check neutralised; it still uses the original package name so it can coexist with
NEXT Dialer.

## Features in RC2

- **Recents now lists every call by default** (Merge calls by = None), matching the complete
  contact history. The merge setting remains available.
- **Long-press call actions**: Call, Send SMS, View contact, Share, and Delete history in Recents;
  the dialpad recent list shares the same menu without Delete.
- **Contact sources checklist** under Behaviour → Contacts to show: All sources, Phone, SIM and
  each synced account, with contact counts. Filters Contacts, T9 search and name matching.
- **Dialpad sizing**: width (50–100 %), height (60–120 %), left/centre/right position, and a
  background-opacity slider. Key grid sizing is independent of the full-width bottom bar.
- **Unified bottom bars**: the dialpad bar now uses the same nav-bar background and divider as
  the Contacts/Recents bar; the call/dial slots are wider, and SHOW DIALPAD has an icon and
  larger text.
- **Accent picker**: standard, light and dark palettes plus Whitesmoke and Almost black, an
  Auto (background) mode, and a custom RRGGBB picker. The accent no longer recolours the status
  bar; the Crystal blue/transparent treatment stays intact.
- **Contact history rows**: tap and swipe are disabled to prevent recursive navigation; long
  press still offers explicit actions.
- **List row background opacity** applies to call rows, contact rows and search results.

### Previous RC1 features

- Dialpad with DTMF tones, haptics, long-press `0` for `+`, long-press backspace to clear.
- Five-slot dialpad bar (paste · contacts · call · backspace · overflow) with balanced padding.
- **Animated keypad collapse**: scrolling the recent list hides the keys smoothly and swaps the
  call button for SHOW DIALPAD; tapping it animates the keys back without losing scroll position.
- **Swipeable dialpad search results**, using the same configured swipe actions as call rows.
- exDialer-style call rows: name (or number), number with type for contacts, call-type icon and
  date, and the last call's type and duration on the right. The contact company/organisation is
  shown in the right-hand details block when available. Unanswered outgoing calls read
  “Not answered”.
- **Complete contact history**: “N CALLS TOTAL” opens every individual call across all of the
  contact's numbers, ignoring the merge setting; Back returns to the contact detail.
- **Swipe reveal spacing and full-swipe actions**: 16 dp start threshold, 4 dp gap between the
  row and the action panel, actions only when the swipe reaches full reveal.
- **Accent colour picker** with standard, light and dark swatches plus the theme default. The
  choice recolours the Crystal status-bar tint, dialpad digits, sliders, switches and highlights.
- **List row background** slider (0–100 % of the theme's current opacity) shared by call rows,
  contact rows and dialpad search results.
- Theme picker uses **minimal layout previews** built from Compose shapes (no images).
- **Call-log range** setting (All / 1 / 2 / 3 / 6 months / 1 year), defaulting to All, with the
  previous 1,000-row cap removed. (If an OEM provider itself prunes older rows, the app can only
  show what the provider returns.)
- T9 contact search while typing, opt-in clipboard paste, and speed-dial long-press.
- **Per-number detail screen**: Call / Message / Add-or-Edit / Share, every stored number,
  call total, complete history, and installed WhatsApp/Telegram chat links.
- Recents grouped by day, colour-coded call types, relative/absolute time, and call-back.
- **Call-log merging** by person or phone number, with an optional "N calls" count.
- **Missed-call badge** on Recents and a summary notification that also drives the launcher
  badge. Opening Recents clears both.
- **Delete** a merged call group by long-pressing a row, **swipe-delete** all history for a
  number, or **clear** the whole call log from Advanced (all require `WRITE_CALL_LOG` and
  confirm first).
- **Favourites** — All/Favourites pills inside Contacts.
- Contacts list with alphabet index, search, avatars, and contact details.
- **Swipe actions** for recents, contacts, dialpad recent calls and dialpad search results:
  Call / Message / View details / VoIP / Delete history / None, with labelled, full-height,
  rounded reveal panels.
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
adb install -r builds\next-dialer-0.2.0-rc2.apk
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
