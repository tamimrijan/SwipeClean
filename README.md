# SwipeClean — swipe your gallery clean

A native Android app (Kotlin + Jetpack Compose) that turns photo cleanup into a quick swipe game.
**Swipe left = delete, swipe right = keep.** Nothing is ever deleted until you confirm it, twice.

**[⬇ Download the APK](release/SwipeClean-1.0.0.apk)** (Android 8.0+, ≈1.8 MB)

## Ready-made APKs (`release/` folder)

The `release/` folder updates itself: every `assembleRelease` or `assembleDebug` build copies fresh APKs into it.

| File | Use |
|---|---|
| `release/SwipeClean-<version>.apk` | Optimized release build (≈1.8 MB). Install this one. |
| `release/SwipeClean-<version>-debug.apk` | Debug build (larger, for development). |

The release APK is signed with the local **debug key** so it installs for testing.
Sign it with your own keystore before publishing to Google Play.

## Features

- **Random 10–15 photo sessions** (you can change the range in Settings) on big full-screen cards.
- **Tinder-style swiping** with rotation, KEEP/DELETE stamps, spring animations, haptics,
  velocity flings, double-tap to keep, and buttons for delete, undo and keep.
- **Safe deletion queue.** A left swipe only queues a photo. Undo restores the last swipe.
  The queue is saved, so you can come back to it later.
- **Session summary** showing kept, to-delete and space you could free, plus confetti and friendly copy.
- **Final confirmation screen** with photo previews and three choices: **Delete**, **Review selection**
  (tap to un-mark, long-press to enlarge), or **Cancel deletion** (keeps everything).
- **System-approved deletion:**
  - Android 11+: `MediaStore.createDeleteRequest` shows Android's own confirmation dialog.
  - Android 10: `RecoverableSecurityException` triggers the system consent prompt.
  - Android 8–9: direct `ContentResolver.delete` with the storage permission.
  - After deletion, the app checks which photos are actually gone before reporting freed space.
- **No repeats.** Photos never shown come first, then the ones shown longest ago.
- **Filters:** skip screenshots, downloads or favorites (favorites are skipped by default), or exclude whole albums.
- **Full gallery browser** with album chips, a swipeable full-screen viewer, and add/remove from the queue.
- **Gentle game layer:** session counter, progress bar, optional daily goal with a ring, a day streak, and lifetime stats.
  None of it pushes you to delete.
- **Material 3 design-kit styling:**
  - Colour: Material You dynamic colour from your wallpaper on Android 12+, falling back to the M3 baseline scheme
    (seed #6750A4). "Keep" green is an M3 extended colour with container roles; "delete" uses the error roles.
  - Type and shapes: the default M3 type scale and shape scale.
  - Components: large and center-aligned top app bars, filled, elevated and outlined cards, list items,
    filter chips, segmented buttons, large FABs for keep and delete, a tonal undo button, badges and alert dialogs.
- **Light, dark or system theme** with edge-to-edge layout, and a blurred photo backdrop behind cards on Android 12+.

## Privacy & battery

- The manifest has **no `INTERNET` permission**, so the app cannot upload anything.
- All scanning, random selection, history and stats live on the device in SharedPreferences.
- There are no background services, workers or polling. The app only queries MediaStore when it opens,
  when you return to it after 2+ minutes, or when photo access changes.

## Permissions by Android version

| Android | Permission requested |
|---|---|
| 14+ | `READ_MEDIA_IMAGES` + `READ_MEDIA_VISUAL_USER_SELECTED`. "Select photos" partial access is supported, with a banner to share more. |
| 13 | `READ_MEDIA_IMAGES` |
| 11–12 | `READ_EXTERNAL_STORAGE` |
| 8–10 | `READ_EXTERNAL_STORAGE` + `WRITE_EXTERNAL_STORAGE` |

If you deny access, the app explains why it needs it and gives you an **Open app settings** button.
Read failures show a clear message with a **Retry** button.

Minimum Android version is 8.0 (API 26). The app targets API 35.

---

## Build it yourself

### Option A — Android Studio (easiest)

1. Install **Android Studio** (Ladybug 2024.2 or newer).
2. **File → Open…** and select this folder (`Fun gallery`).
3. Let Gradle sync. Android Studio downloads anything missing (SDK 35, Gradle 8.11.1).
4. Pick a device and press **Run ▶**, or use **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.

### Option B — Command line (Windows)

Requirements: **JDK 17** and the **Android SDK** with platform 35. Android Studio installs both.

1. Point the project at your SDK. `local.properties` is machine-specific; create or edit it:
   ```
   sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
   ```
2. Build:
   ```bash
   .\gradlew.bat assembleRelease
   ```
   Output: `app\build\outputs\apk\release\app-release.apk`
   (use `assembleDebug` for `app\build\outputs\apk\debug\app-debug.apk`).

On macOS or Linux, use `./gradlew` instead.

## Install on your phone

### With USB (adb)

1. On the phone, open **Settings → About phone** and tap **Build number** 7 times.
   Then turn on **Developer options → USB debugging**.
2. Connect the phone and accept the "Allow USB debugging?" prompt.
3. Run:
   ```bash
   adb install -r release\SwipeClean-1.0.0.apk
   ```
   (`adb` is in `%LOCALAPPDATA%\Android\Sdk\platform-tools`.)

### Without a computer

1. Copy `release/SwipeClean-1.0.0.apk` to the phone (USB, Drive, email to yourself, etc.).
2. Open it from the Files app. Allow **Install unknown apps** for that app when prompted.
3. Tap **Install**. Play Protect may warn about an unknown developer; choose **Install anyway**.

## Project structure

```
app/src/main/java/com/swipeclean/app/
├── MainActivity.kt              Edge-to-edge entry point
├── data/
│   ├── Photo.kt                 Photo & Album models
│   ├── PhotoRepository.kt       MediaStore query + version-specific deletion
│   ├── Permissions.kt           Per-version permission logic, Full / Partial / None
│   ├── Prefs.kt                 Settings, stats, streak, deletion queue, shown-history
│   └── SessionPicker.kt         Random, filtered, no-repeat selection
└── ui/
    ├── App.kt                   Permission gate, navigation, system dialogs, snackbars
    ├── AppViewModel.kt          All state: session, queue, deletion flow, stats
    ├── Messages.kt              Playful copy
    ├── theme/Theme.kt           M3 dynamic/baseline color schemes + "keep" extended color
    ├── components/Common.kt     Scaffold wrapper, stat card, confetti, image helper
    └── screens/                 Permission, Home, Swipe, Summary, Confirm/Review, Done, Gallery, Settings
```

## Tech

Kotlin 2.0.21 · AGP 8.7.3 · Gradle 8.11.1 · Compose BOM 2024.12.01 (Material 3) · Coil 2.7 for image loading ·
Inter typeface (SIL Open Font License, see `app/src/main/assets/licenses/Inter-OFL.txt`).

## Author

**Tamim Ahmed Rijan**

- GitHub: [github.com/tamimrijan](https://github.com/tamimrijan)
- Instagram: [@tamim_ahmed_rijan](https://www.instagram.com/tamim_ahmed_rijan/)
