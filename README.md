<div align="center">

<img src="docs/logo.svg" width="120" alt="SwipeClean logo" />

# SwipeClean

### Swipe your gallery clean.

**SwipeClean** is a Tinder-style photo cleaner for Android.<br/>
It shows you a few random photos at a time, lets you swipe left to delete or right to keep, and never deletes anything until you *confirm it twice*.

<br/>

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3%20Expressive-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Offline](https://img.shields.io/badge/Works-100%25%20Offline-6A35D4?style=for-the-badge)

<br/>

[**⬇️ Download the latest APK**](https://github.com/tamimrijan/SwipeClean/raw/main/release/SwipeClean-1.0.0.apk) &nbsp;·&nbsp; [Features](#-features) &nbsp;·&nbsp; [Screenshots](#-screenshots) &nbsp;·&nbsp; [Build](#-build-it-yourself)

</div>

---

## ✨ Features

### 👆 Swipe to clean
- **Quick sessions**: 10 to 15 random photos per round on big full-screen cards (you can change the range).
- **Feels great**: rotation, KEEP and DELETE stamps, spring physics, haptics and velocity flings.
- **Fast controls**: swipe, or use the delete, undo and keep buttons. Double-tap a photo to keep it.
- **No repeats**: photos you have never seen come first, then the ones shown longest ago.

### 🛡️ Safe deletion
> Swipe left → photo goes to a *deletion queue* → session summary → **Review & confirm** → Android's own delete dialog

Nothing is deleted on a swipe. You can undo, review the selection, keep everything, or come back to the queue later.

- **Session summary** shows how many photos you kept, how many you marked and how much space you could free.
- **Confirmation screen** with previews and three choices: *Delete*, *Review selection* or *Cancel deletion*.
- **System approved**: uses `MediaStore.createDeleteRequest` on Android 11+, so Android asks one more time. No root needed.

### 🎯 Smart filters
- **Skip what you want**: screenshots, downloaded images or favorites (favorites are protected by default).
- **Albums**: choose which albums can appear in sessions.
- **Full gallery browser** with album chips and a swipeable full-screen viewer.

### 🎮 Fun, not pushy
- Optional **daily goal** with a wavy progress ring, **day streaks** and lifetime stats.
- Confetti and friendly messages. Keeping photos is celebrated too.

### 🎨 Design
- **Material 3 Expressive** design: bold Inter type, playful shapes (cookies, clovers, sunny bursts), shape morphing buttons, wavy progress and springy motion.
- Light and dark themes, plus optional Material You dynamic color.
- A one-screen home page, tabbed settings and an edge-to-edge layout.

---

## 📱 Screenshots

<div align="center">

| Home | Swipe | Summary |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/ec16ddd9-3558-480f-91a3-97a9c31b794d" width="220"/> | <img src="https://github.com/user-attachments/assets/1ad92c4f-25d3-4710-868c-e12e5e2c48e9" width="220"/> | <img src="https://github.com/user-attachments/assets/d078a233-64f9-4c0a-b7fa-adfb5bef3d7a" width="220"/> |

</div>

---

## 🚀 Install

1. Download **`SwipeClean-1.0.0.apk`** from the [`release`](release) folder.
2. Open it on your phone and allow *Install unknown apps* when asked.
3. Open SwipeClean and allow photo access.
4. Tap **Start cleaning** and swipe away.

<details>
<summary><b>Install with USB (adb)</b></summary>

<br/>

Turn on **USB debugging** in Developer options, connect your phone and run:

```bash
adb install -r release/SwipeClean-1.0.0.apk
```

</details>

---

## 🛠 Tech stack

| Area | Built with |
|---|---|
| Language and UI | Kotlin 2.2, Jetpack Compose 1.11, Material 3 Expressive (material3 1.5) |
| Photos | MediaStore API, Coil image loading |
| Deletion | `MediaStore.createDeleteRequest` (Android 11+), `RecoverableSecurityException` (Android 10) |
| Permissions | `READ_MEDIA_IMAGES`, partial "Select photos" access on Android 14+ |
| Storage | SharedPreferences (all on the device) |
| Typography | Inter variable font (SIL Open Font License) |
| Build | Gradle 8.11, AGP 8.7, R8 minified release (~2 MB) |

---

## 🧑‍💻 Build it yourself

**You need:** Android Studio (Ladybug or newer) or JDK 17 plus Android SDK 35.

```bash
git clone https://github.com/tamimrijan/SwipeClean.git
cd SwipeClean
./gradlew assembleDebug        # debug APK
./gradlew assembleRelease      # optimised release APK
```

Every build copies the APKs into `release/` as `SwipeClean-<version>.apk`.

**Release signing:** the release APK is signed with the debug key so it installs for testing.
Use your own keystore before publishing to Google Play.

---

## 📂 Project structure

```
app/src/main/java/com/swipeclean/app/
├── data/        MediaStore queries, deletion, permissions, settings, session picker
└── ui/          Compose screens, components, theme, view model
docs/            logo
release/         ready-to-install APK
```

---

## 🔒 Privacy

- No account, no ads, no analytics.
- **No internet permission at all.** Your photos never leave your phone.
- Settings, stats and history are stored only on the device.
- Photos are deleted only through Android's system-approved APIs, after you confirm.

---

## 👤 Author

<div align="center">

**Tamim Ahmed Rijan**

[![Instagram](https://img.shields.io/badge/Instagram-tamim__ahmed__rijan-E4405F?style=for-the-badge&logo=instagram&logoColor=white)](https://www.instagram.com/tamim_ahmed_rijan/)
[![GitHub](https://img.shields.io/badge/GitHub-tamimrijan-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/tamimrijan)

<br/>

If SwipeClean helped you free up space, give it a ⭐

</div>
