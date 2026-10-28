<div align="center">

# SwipeClean

### Swipe your gallery clean.

A Tinder-style photo cleaner for Android that turns a boring chore into a quick, satisfying game.
**Swipe left to delete, swipe right to keep.** Nothing is deleted until you say so, twice.

<br>

[![Download APK](https://img.shields.io/badge/Download-APK%20v1.0.0-6750A4?style=for-the-badge&logo=android&logoColor=white)](https://github.com/tamimrijan/SwipeClean/raw/main/release/SwipeClean-1.0.0.apk)

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Offline](https://img.shields.io/badge/Internet-not%20required-success?style=flat-square)
![APK size](https://img.shields.io/badge/APK-1.8%20MB-informational?style=flat-square)
![License](https://img.shields.io/github/license/tamimrijan/SwipeClean?style=flat-square)

<br>

<img src="https://github.com/user-attachments/assets/ec16ddd9-3558-480f-91a3-97a9c31b794d" width="250" alt="SwipeClean screenshot 1" />
&nbsp;&nbsp;
<img src="https://github.com/user-attachments/assets/1ad92c4f-25d3-4710-868c-e12e5e2c48e9" width="250" alt="SwipeClean screenshot 2" />
&nbsp;&nbsp;
<img src="https://github.com/user-attachments/assets/d078a233-64f9-4c0a-b7fa-adfb5bef3d7a" width="250" alt="SwipeClean screenshot 3" />

</div>

---

## Why SwipeClean?

Your gallery has thousands of photos: blurry shots, duplicates, old screenshots and memes.
Scrolling through all of them is exhausting, so nobody does it.

SwipeClean hands you **10–15 random photos at a time** on big full-screen cards. Swipe through them
in under a minute, while you're bored, in bed at night, or riding as a passenger, and watch your storage free up.

## How it works

```mermaid
flowchart LR
    A([Start cleaning]) --> B[Swipe 10-15 photos]
    B -->|Swipe right| K[Kept]
    B -->|Swipe left| Q[Deletion queue]
    Q --> S[Session summary]
    S --> R{Review & confirm}
    R -->|Cancel| K
    R -->|Delete| D[Android system dialog]
    D --> F([Space freed])
```

> **You stay in control.** A left swipe only *queues* a photo. You can undo, review the selection,
> or cancel. Photos are deleted only after you confirm in the app **and** in Android's own dialog.

## Features

<table>
<tr>
<td width="50%" valign="top">

### Swipe to clean
- Large full-screen photo cards
- Rotation, KEEP / DELETE stamps and spring physics
- Haptic feedback and velocity flings
- Double-tap to keep, plus **Undo**
- Session counter and progress bar

</td>
<td width="50%" valign="top">

### Safe by design
- Temporary deletion queue that persists
- Summary showing kept, deleted and space to free
- Confirmation screen with previews
- **Delete / Review / Cancel** options
- Uses Android's official delete confirmation

</td>
</tr>
<tr>
<td width="50%" valign="top">

### Smart sessions
- Random picks that avoid recently shown photos
- Skip screenshots, downloads or favorites
- Exclude whole albums
- Adjustable session size (5–30)

</td>
<td width="50%" valign="top">

### Fun, not pushy
- Optional daily goal with a progress ring
- Day streaks and lifetime stats
- Confetti and friendly messages
- Keeping photos is celebrated too

</td>
</tr>
<tr>
<td width="50%" valign="top">

### Beautiful UI
- Material 3 design-kit styling
- Light, dark or system theme
- Optional Material You dynamic color
- Inter typeface, edge-to-edge layout

</td>
<td width="50%" valign="top">

### Private & light
- **No internet permission** at all
- Everything runs locally on your device
- No background services or tracking
- Full gallery browser with album filters

</td>
</tr>
</table>

## Download & install

1. Download **[SwipeClean-1.0.0.apk](https://github.com/tamimrijan/SwipeClean/raw/main/release/SwipeClean-1.0.0.apk)** on your phone.
2. Open it and allow **Install unknown apps** when Android asks.
3. Tap **Install**. If Play Protect warns about an unknown developer, choose **Install anyway**.

<details>
<summary><b>Install with USB (adb)</b></summary>

<br>

1. Enable **Developer options** (tap *Build number* 7 times in *About phone*), then turn on **USB debugging**.
2. Connect your phone and run:

```bash
adb install -r release/SwipeClean-1.0.0.apk
```

</details>

## Privacy

| | |
|---|---|
| Internet access | **None.** The app has no `INTERNET` permission. |
| Where data lives | Only on your device: settings, stats and history are stored locally. |
| Deletion | Only through Android's system-approved APIs. No root needed. |
| Background work | None. Photos are scanned only when you open the app. |

## Compatibility

| Android version | Photo access | How deletion is approved |
|---|---|---|
| 14+ | All photos, or **Select photos** (partial access) | System confirmation dialog |
| 13 | `READ_MEDIA_IMAGES` | System confirmation dialog |
| 11–12 | `READ_EXTERNAL_STORAGE` | System confirmation dialog |
| 10 | `READ_EXTERNAL_STORAGE` | System consent prompt |
| 8–9 | Storage permission | In-app confirmation |

Minimum **Android 8.0** (API 26) · Target **API 35**

## Build from source

<details>
<summary><b>Android Studio (easiest)</b></summary>

<br>

1. Install **Android Studio** (Ladybug 2024.2 or newer).
2. **File → Open** and select the project folder.
3. Wait for Gradle sync, then press **Run ▶**.

</details>

<details>
<summary><b>Command line</b></summary>

<br>

You'll need **JDK 17** and the **Android SDK** (platform 35).

```bash
git clone https://github.com/tamimrijan/SwipeClean.git
cd SwipeClean
./gradlew assembleRelease      # Windows: .\gradlew.bat assembleRelease
```

The APK is copied to `release/SwipeClean-<version>.apk` automatically.

> The release build is signed with the debug key so it installs for testing.
> Use your own keystore before publishing to Google Play.

</details>

<details>
<summary><b>Project structure</b></summary>

<br>

```
app/src/main/java/com/swipeclean/app/
├── MainActivity.kt              Edge-to-edge entry point
├── data/
│   ├── Photo.kt                 Photo & Album models
│   ├── PhotoRepository.kt       MediaStore queries + version-specific deletion
│   ├── Permissions.kt           Per-version permission logic
│   ├── Prefs.kt                 Settings, stats, streaks, queue, history
│   └── SessionPicker.kt         Random, filtered, no-repeat selection
└── ui/
    ├── App.kt                   Permission gate, navigation, system dialogs
    ├── AppViewModel.kt          Session, queue, deletion flow and stats
    ├── theme/Theme.kt           Material 3 colors + Inter typography
    ├── components/Common.kt     Shared UI pieces and confetti
    └── screens/                 Home, Swipe, Summary, Confirm, Gallery, Settings...
```

</details>

## Built with

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white)

Kotlin 2.0 · Jetpack Compose (Material 3) · MediaStore API · Coil · Gradle 8.11 ·
[Inter](https://rsms.me/inter/) typeface (SIL Open Font License)

---

<div align="center">

### Made by Tamim Ahmed Rijan

[![GitHub](https://img.shields.io/badge/GitHub-tamimrijan-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/tamimrijan)
[![Instagram](https://img.shields.io/badge/Instagram-tamim__ahmed__rijan-E4405F?style=for-the-badge&logo=instagram&logoColor=white)](https://www.instagram.com/tamim_ahmed_rijan/)

If SwipeClean helped you free up some space, consider giving it a ⭐

</div>
