# Reverse-Engineering Notes

Working material lives in `RE/`:

```
RE/
  exDialer 198.apk          original APK (repo root)
  exDialer Pro Key 81.apk   key stub (repo root)
  exdialer_src/             apktool decode (smali + resources)
  jadx/main/sources/        jadx Java sources
  key_src/                  decoded key APK
  ui_*.xml                  uiautomator dumps
  shot_*.png                emulator screenshots
```

## Original package facts

| Item | Value |
| --- | --- |
| Package | `com.modoohut.dialer` |
| Version | 198 |
| minSdk / targetSdk | 7 / 26 |
| Key package | `com.modoohut.dialer.donate` v81 |
| Main activity | `com.modoohut.dialer.DialActivity` |
| Contacts activity | `com.modoohut.dialer.ContactsTabActivity` |
| Preferences | `com.modoohut.dialer.PrefActivity` |
| Theme picker | `com.modoohut.dialer.ThemeSelectActivity` |

The key APK contains only a manifest and `R`/`BuildConfig` classes. The original checks for the
key package's presence, not Google Play licensing.

## How the Pro check works

`TheApp.m880b()` (smali `TheApp;->b()V`) calls
`PackageManager.getPackageInfo("com.modoohut.dialer.donate", 1)`. On success it sets
`TheApp.f802b = true`; any exception sets it false. Trial expiry is tracked separately using the
install time and `/sdcard/com.modoohut.dialer/.t`.

The patched legacy build replaces the body of `b()` with:

```smali
const/4 v0, 0x1
sput-boolean v0, Lcom/modoohut/dialer/TheApp;->b:Z
return-void
```

Result: "v198 Pro" in About, no "Buy Pro" entry, no key package required.

Build:

```powershell
java -jar tools/apktool.jar b -f -o work/exDialer-198-unlocked-unsigned.apk RE/exdialer_src
zipalign -f 4 work/exDialer-198-unlocked-unsigned.apk work/exDialer-198-unlocked-aligned.apk
apksigner sign --ks work/nextdialer.keystore ... --out builds/legacy/exDialer-198-unlocked.apk ...
```

The legacy APK keeps the original package name, so it can coexist with `com.nextdialer`.

## Preference keys and defaults

From `p030d/C0514am.java` (getter defaults):

| Key | Default |
| --- | --- |
| `theme_apk` | default theme id |
| `second_theme` | `""` |
| `theme_switch_time_start` / `_end` | 1080 / 480 (minutes) |
| `display_photo` | true |
| `full_screen` | false |
| `text_size` | `"0"` |
| `color_calls` | false |
| `relative_time` | true |
| `display_time` | false |
| `format_number` | true |
| `tint_system_bar` | 1 |
| `dialpad_text_size` | `"0"` |
| `dialpad_size` | `"0"` |
| `display_basic_letters` | true |
| `letter_right` | true |
| `count_of_merged_calls` | false |
| `enable_badge_count` | true |
| `screen_rotation` | false |
| `allow_ext_contact_viewer` | false |
| `call_confirm` | false |
| `call_confirm_voip` | false |
| `touch_tones` | true |
| `haptic_feedback` | false |
| `haptic_feedback_strength` | `"0"` (−1…2) |
| `check_clipboard` | true |
| `auto_popup_dialpad` | true |
| `swipe_close_dialpad` | true |
| `close_dialpad_on_back` | false |
| `view_calls_on_call_end` | true |
| `open_stock_dialer` | true |
| `switch_to_call_log` | false |
| `remember_tab` | true |
| `click_item_action_dialer2` | View contact (ordinal 2) |
| `click_item_action_contacts2` | View contact (ordinal 2) |
| `swipeLeftItemActionDialer` | SMS |
| `swipeRightItemActionDialer` | Call |
| `swipeLeftItemActionContacts` | −1 (None) |
| `swipeRightItemActionContacts` | −1 (None) |
| `keep_in_memory` | true |
| `hw_accel` | true |
| `reverse_call_log` | false |
| `short_num_as_speed_dial` | true |
| `strict_mode` | true |
| `t9_lang` | `"auto"` |
| `filter_sort_option` | `"0"` |
| `sort_by_last_name` | false |
| `additional_fields` | 3 |
| `ignore_display_options` | false |
| `merge_by` | 2 |
| `roaming_auto_dial` | false |
| `sim_country_iso` | `""` |
| `appointment_desc` | `"#"` |
| `speed_dial_2…9` | empty |

Swipe/tap action enum (`p030d/EnumC0516ao`): `Call`, `Sms`, `Detail`, `VoipCall`. The UI also
offers `None` for swipe actions (`−1`).

## Theme ids

The theme picker shows Lollipop, Crystal, Simplism, Crystal Simp, Classic. Applying a theme
writes `theme_apk`:

- Crystal → `c_com.modoohut.dialer`
- Crystal Simp → `cc_com.modoohut.dialer`
- Lollipop → `com.modoohut.dialer` (default)
- Simplism → `s_com.modoohut.dialer`
- Classic → `com.modoohut.dialer.classic`

The theme package id is prefixed with `c_`, `cc_`, `l_`, `s_` to select resource qualifiers in
the original APK. NEXT Dialer uses its own `DialerTheme` model instead.

## Layout observations

- `res/layout/dialer.xml`: full-screen `MyListView` above a bottom button bar, plus a hidden
  `TabHost` used for tab-like navigation.
- `res/layout/dialer_list_item.xml`: photo, display name, call count, number/type, date,
  call-type indicator, and a right-aligned type label.
- `res/layout/contacts_tab.xml`: `TabHost` (favourites / call log / contacts / groups), overflow
  button, and a bottom action bar (dialpad, add, search, more).
- Dialpad uses the bundled `dialpad.ttf` / `_dialpad.ttf` fonts. NEXT Dialer uses the system
  font and a clean geometric layout; the fonts are not copied to avoid asset-licensing
  ambiguity.
- Crystal screenshots confirm the dimmed-wallpaper look: a dark translucent scrim over the
  wallpaper, translucent keypad, blue digits, and a green circular call button.

## Plugin mechanism

`PrefActivity` "Plugin" opens screen 4 (UI language). The list comes from
`C0559m.m1408a(resources)`, which enumerates `resources.getAssets().getLocales()`. There is no
external code loading. "Get More Plugins" is just a link. The only package-level extension point
is the theme APK (`theme_apk`), loaded as a resource package; NEXT Dialer replaces this with
built-in themes.

## Emulator notes

The machine has no usable hardware virtualisation:

- `HypervisorPresent = False`
- `VirtualizationFirmwareEnabled = False`
- No WHPX / AEHD service
- Android emulator 37.2.12 exits silently under TCG
- Emulator **31.1.4** works with `-accel off`

Working command (from the repo root):

```powershell
$env:ANDROID_SDK_ROOT="$env:LOCALAPPDATA\Android\Sdk"
.\tools\emulator31\emulator\emulator.exe -avd NEXT_API28 `
  -no-window -no-audio -no-boot-anim -no-snapshot -accel off `
  -gpu swiftshader_indirect
```

Useful ADB notes:

- `adb root` is required before inserting call-log rows; the shell user lacks
  `READ_CALL_LOG` / `WRITE_CALL_LOG`.
- Capture screenshots with `adb shell screencap -p /sdcard/s.png` then `adb pull`; `exec-out`
  corrupts PNGs on this Windows setup.
- Grant permissions after install:
  `adb shell pm grant com.nextdialer android.permission.READ_CONTACTS` etc.
- The emulator is 1080×1920 at 420 dpi (API 28).

## Evidence files

| File | Content |
| --- | --- |
| `RE/shot_dialer_initial.png` | Default Lollipop dialer with call log behind the popup dialpad |
| `RE/shot_dialer_crystal.png` | Crystal theme, dimmed wallpaper dialpad |
| `RE/shot_dialer_classic.png` | Classic theme, call log + dialpad |
| `RE/shot_contacts.png` | Contacts tab, alphabet index, bottom action bar |
| `RE/shot_prefs_*.png` | Preferences root / Appearance / Behaviour / Advanced |
| `RE/shot_theme_select.png` | Theme grid |
| `RE/ui_*.xml` | uiautomator hierarchies for the above |
