# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

ACD ("Assistive Communication Device") is an Android app for a **Kindle Fire tablet, landscape
orientation**, helping a stroke patient with expressive aphasia communicate by spelling words on
screen. See `README.md` for the product intent. The target user can spell but struggles to
produce speech, so the UI prioritizes **large, tap-accurate buttons** over typing speed.

Planned screens/features (mostly not yet built): an alphabet button grid, a word/phrase display
area above the keys, a persistent screen-switcher bar, and on-device text-to-speech (stretch goal,
must work offline).

## Current state

The repository is still the **default Android Studio Jetpack Compose template** — `MainActivity.kt`
renders a "Hello Android" `Greeting`. None of the ACD features described above exist yet. New work
generally means replacing that template, not extending it.

## Build & test

> **Environment note:** Claude Code runs inside a **Podman container** (`ccontainer:latest`, based on
> `node:lts-bookworm-slim` → Debian 12), launched by `../ccontainer/run.sh` on a **Windows 11 + WSL2**
> setup. `/work` is bind-mounted from the host's `/mnt/c/work` (i.e. `C:\work`), so this project is
> `C:\work\acddad` on the host. **Android Studio** runs on the Windows host and does the actual
> building, emulator, and device deploys. The container has **no Android SDK / `adb` / `ANDROID_HOME`**,
> and `local.properties`'s `sdk.dir` is a Windows path. So the Gradle commands below generally
> **will not run from the container** — make code changes here and build/run in Android Studio on the host.

Uses the Gradle wrapper (`./gradlew`). A local Android SDK is required (`local.properties` →
`sdk.dir`).

```bash
./gradlew assembleDebug            # build debug APK
./gradlew installDebug             # build + install on a connected device/emulator
./gradlew test                     # JVM unit tests (app/src/test)
./gradlew testDebugUnitTest        # unit tests, debug variant only
./gradlew connectedAndroidTest     # instrumented tests (needs device/emulator; app/src/androidTest)
./gradlew lint                     # Android lint
```

Run a single unit test class/method:

```bash
./gradlew testDebugUnitTest --tests "com.example.acd.ExampleUnitTest.addition_isCorrect"
```

## Architecture & conventions

- **100% Kotlin + Jetpack Compose** (no XML layouts; no Views/Fragments). Material 3.
- Single-activity app: `MainActivity` (a `ComponentActivity`) sets content via `setContent { AcdTheme { ... } }`.
- Theme lives in `app/src/main/java/com/example/acd/ui/theme/` (`Theme.kt`, `Color.kt`, `Type.kt`),
  applied as `AcdTheme`. Wrap all composables in it.
- Package/namespace: `com.example.acd`. `applicationId` is also `com.example.acd`.
- `minSdk = 24`, `targetSdk`/`compileSdk = 36`. Kotlin `2.2.10`, AGP `9.2.1`, Java 11 bytecode.
- Dependencies are managed via the **version catalog** at `gradle/libs.versions.toml` — add/bump
  libraries there and reference them as `libs.*` in `app/build.gradle.kts`, rather than hardcoding
  versions in the build script.
- Gradle **configuration cache is enabled** (`gradle.properties`); avoid build-logic patterns that
  break it.

## Design guidance

When writing code, consult the project's `coding` skill for project-specific conventions and
constraints. Keep the aphasia/Kindle-Fire context front of mind: large touch targets, landscape
layout, offline-first, minimal cognitive load.
