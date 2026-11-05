# Overlay Demo — Android Studio Project

Canvas bounding-box + health-bar overlay rendered via a Foreground Service and
`TYPE_APPLICATION_OVERLAY`. No game-memory reading, no process access.

## Requirements

- Android Studio Ladybug (2024.2) or newer
- Android SDK: compile SDK 36 (Android 16 preview) + SDK platform 36 installed
- Gradle 8.7 — downloaded automatically on first sync
- Device or emulator running Android 8.0+ (API 26+)

## First-time setup in Android Studio

1. **File → Open** → select this folder.
2. Android Studio will warn that `gradle-wrapper.jar` is missing.  
   Click **"Add Gradle wrapper"** in the yellow banner — it downloads the correct jar.  
   Or run from terminal: `./gradlew wrapper --gradle-version 8.7`
3. Let the Gradle sync complete (downloads ~300 MB first run).
4. **Run → Run 'app'** or `./gradlew assembleDebug`.

## Building from terminal (after wrapper is set up)

```bash
./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

## Installing on device

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Using the app

1. Launch **Overlay Demo**.
2. Tap **Start Overlay** — if prompted, grant "Draw over other apps" permission,
   then tap Start again.
3. Press **Home** — five animated bounding boxes with health bars appear over your
   launcher. Red = "enemy" (even index), green = "team" (odd index).
4. Return to the app and tap **Stop Overlay** to dismiss, or use the notification
   action.

## Customising demo entities

Edit `OverlayService.kt` → `startAnimation()`. Each `DemoEntity` takes:

| Field         | Type    | Description                        |
|---------------|---------|------------------------------------|
| `screenX`     | Float   | Centre X (pixels)                  |
| `screenY`     | Float   | Top of bounding box (pixels)       |
| `screenFeetY` | Float   | Bottom of bounding box (pixels)    |
| `health`      | Int     | 0–100                              |
| `label`       | String  | Name drawn below the box           |
| `isEnemy`     | Boolean | true = red box, false = green box  |

## Project structure

```
app/src/main/
├── kotlin/com/bench/overlaydemo/
│   ├── DemoEntity.kt          — data class
│   ├── OverlaySurfaceView.kt  — Canvas renderer
│   ├── OverlayService.kt      — Foreground service + demo animation
│   └── MainActivity.kt        — Launcher + permission flow
├── res/
│   ├── layout/activity_main.xml
│   ├── values/strings.xml
│   ├── values/colors.xml
│   ├── values/themes.xml
│   └── drawable/status_bg.xml
└── AndroidManifest.xml
```

## Permissions used

| Permission               | Why                                             |
|--------------------------|-------------------------------------------------|
| `SYSTEM_ALERT_WINDOW`    | Draw `TYPE_APPLICATION_OVERLAY` window          |
| `FOREGROUND_SERVICE`     | Keep service alive while screen is on           |
| `POST_NOTIFICATIONS`     | Show foreground service notification (API 33+)  |
| `WAKE_LOCK`              | Optional — prevents render thread from sleeping |


## Build note (Android 16)

This project is configured for compileSdk 36 and Java 17. The foreground
service declares the Android 14+ special-use permission and subtype.

The original archive did not include `gradle/wrapper/gradle-wrapper.jar`.
For that reason, use the included GitHub Actions workflow to build without
a local Gradle wrapper: upload this project to a GitHub repository, then
open Actions → Build Android APK → Run workflow. Download the
`OverlayDemo-debug-apk` artifact and install `app-debug.apk` on your phone.
