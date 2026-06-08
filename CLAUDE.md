# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

ACD ("Assistive Communication Device") is an Android app for a **Kindle Fire (Fire Max 11) tablet in
landscape**, helping a stroke patient with expressive aphasia communicate by spelling words on
screen. See `README.md` for product intent. The target user can spell but struggles to produce
speech, so the UI prioritizes **large, tap-accurate buttons** and minimal cognitive load over typing
speed.

Built so far: two input panels switched by a vertical bar on the right — an **alphabet panel**
(0–9 + A–Z, with YES/NO quick-response keys) and a **words panel** (fixed hospital/AAC words plus
two rows of auto-learned most-used words). Both share a top control row (space / backspace /
clear-word / clear). A square **Speak** button reads the line via on-device TTS, YES/NO also speak
on tap, and a hidden **diagnostic panel** (type `UUUUU`) shows the learned-word frequency table.

`USAGE.md` documents the controls from the user's point of view — keep it in sync when behavior
changes.

## Build & test

> **Environment note:** Claude Code runs inside a **Podman container** (`ccontainer:latest`, based on
> `node:lts-bookworm-slim` → Debian 12), launched by `../ccontainer/run.sh` on a **Windows 11 + WSL2**
> setup. `/work` is bind-mounted from the host's `/mnt/c/work`, so this project is `C:\work\acddad` on
> the host. **Android Studio** on the Windows host does the actual building, emulator, and device
> deploys. The container has **no Android SDK / `adb` / `ANDROID_HOME`**, and `local.properties`'s
> `sdk.dir` is a Windows path, so the Gradle commands below **will not run from the container** — make
> code changes here and build/run in Android Studio on the host (its terminal is the place to run
> Gradle). The commands are documented for use there.

```bash
./gradlew assembleDebug            # build debug APK
./gradlew installDebug             # build + install on a connected device/emulator
./gradlew testDebugUnitTest        # JVM unit tests (app/src/test)
./gradlew connectedAndroidTest     # instrumented tests (needs device/emulator; app/src/androidTest is currently empty)
./gradlew lint                     # Android lint
```

Run a single unit test class or method:

```bash
./gradlew testDebugUnitTest --tests "com.example.acd.text.PhraseTest"
./gradlew testDebugUnitTest --tests "com.example.acd.text.PhraseTest.clearWord_removesLastWordBackToPreviousSpace"
```

## Architecture

Single-activity, 100% Kotlin + Jetpack Compose (no XML layouts, no Views/Fragments), Material 3.
`MainActivity` sets content via `setContent { AcdTheme { Scaffold { AlphabetScreen(...) } } }`. The
UI is **designed for landscape** (the Fire Max 11 is used mounted in landscape), but the activity is
**not orientation-locked** — the weighted Compose layout reflows to fill whatever orientation the
device is in, so portrait shows the same board squished tall rather than letterboxed into a band. (A
hard landscape lock was dropped because Android 16 / targetSdk 36 ignores `screenOrientation` on
large screens anyway; see git history.)

The central design rule here is **separating pure, JVM-testable logic from Android framework code**,
so the meaningful behavior is covered by fast unit tests in `app/src/test` (no instrumentation):

- **`text/Phrase.kt`** — an immutable value type holding the spelled-out text. All editing
  (`append`, `appendWord`, `space`, `backspace`, `clearWord`, `cleared`) returns a new `Phrase` and
  has zero Android dependencies. This is the model the UI holds in state. Tested by `PhraseTest`.
- **`speech/Speaker.kt`** — wraps Android `TextToSpeech`. Exposes an observable `SpeakState`
  (`LOADING` / `READY` / `ERROR`) and a `speak(text)` method; `rememberSpeaker()` ties its lifecycle
  to the composition. The two decisions worth testing are extracted as **pure top-level functions** —
  `resolveSpeakState(...)` and `pickMaleVoiceName(...)` — and the thin Android glue calls them. Tested
  by `SpeakStateTest` and `VoiceSelectionTest`.
- **`tally/WordTally.kt`** — immutable frequency map of words the user speaks or completes with a
  space. Matching is **length-aware fuzzy** (`editDistance` + `maxEditsForLength`: budget grows with
  word length, so long-word typos merge but short distinct words don't). `top(n, excluding)` ranks
  for the learned rows; `ranked()` feeds the diagnostic panel; `without` removes one word. All pure,
  tested by `WordTallyTest`.
- **`tally/WordMemory.kt`** — the Android-bound glue: holds the `WordTally` as observable state and
  **persists it in SharedPreferences** across launches. `rememberWordMemory()` loads on entry.
- **`ui/AlphabetScreen.kt`** — the whole UI. `AlphabetScreen` owns the `Phrase` state (via
  `rememberSaveable` + a `Saver`, survives rotation/process death), the current `Panel`, the
  `Speaker`, and the `WordMemory`. Panels: `ALPHABET`, `WORDS`, and a hidden `DIAGNOSTIC` that is
  excluded from the switcher's normal cycle and reached only by typing `DIAGNOSTIC_TRIGGER` (`UUUUU`).

UI patterns to preserve when extending:

- **Keys carry their own action, not a type tag.** A `Key` is `(label, apply: KeyAction, speaks)`
  where `KeyAction = (Phrase) -> Phrase`. Letter/digit keys append a character; word keys use
  `Phrase.appendWord` (separating space when needed). All key taps funnel through one handler
  (`onKeyPress`), which runs `key.apply` and — only for keys with `speaks` set (YES/NO) — also speaks
  the label. Add new keys by constructing `Key`s, not by adding conditionals; the single `speaks`
  flag and the diagnostic-trigger check are the only per-tap branches, both at that one point.
- **Tally events live at the screen, not the keys.** A spoken line (Speak button) records every word;
  a real space records the just-completed word. YES/NO speaking deliberately does **not** tally.
  Learned rows exclude `SCREEN_WORDS` (the fixed words + YES/NO) so nothing shows twice, and tapping
  a learned word `LEARNED_REMOVE_TAPS` (5) times in a row `forget`s it.
- **TTS has no gender API**, so a male voice is chosen by name in `pickMaleVoiceName` (explicit "male"
  marker, else known male Google en-US ids `en-us-x-iom` / `en-us-x-tpd`), falling back to the default
  voice. Google voice ids won't exist on the Fire's Amazon engine — revisit voice names when testing
  on-device. See README "References".
- **No icon-font dependency:** the Speak button's three states and the word-display end-of-line `_`
  cursor are drawn/typed directly (custom Canvas glyphs `drawSpeaker`/`drawClock`/`drawDot`; the `_`
  is appended display-only so it's never spoken or tallied). Key font sizes are constants
  (`KEY_FONT`, `BIG_KEY_FONT` for single-character keys).

## Conventions

- A project **`coding` skill** defines the in-repo conventions — consult it before writing code. In
  short: two-pass (working, then clean); keep it simple and close to requirements; **no default or
  optional parameters** (the signature is the signature, fail fast); prefer dispatch / first-class
  functions over conditionals that branch on a recurring type/mode; shallow call chains returning
  concrete objects; and **all tests are reusable unit tests in the test dir**, never throwaway scripts.
- `modifier` is passed as a **required** parameter to composables (no `= Modifier` default), per the
  no-defaults rule.
- Package/namespace and `applicationId`: `com.example.acd`.
- `minSdk = 24`, `targetSdk = 36`, `compileSdk = 37` (37 is required by `androidx.core` 1.19.0).
  Kotlin `2.2.10`, AGP `9.2.1`, Java 11 bytecode. These are bleeding-edge; if a sync fails on SDK 37
  or AGP, that's the likely cause.
- Dependencies are managed via the **version catalog** at `gradle/libs.versions.toml` — add/bump
  there and reference as `libs.*` in `app/build.gradle.kts`; don't hardcode versions in build scripts.
- Gradle **configuration cache is enabled** (`gradle.properties`); avoid build-logic that breaks it.
