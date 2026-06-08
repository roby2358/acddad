# ACD — Assistive Communication Device

A Kindle Fire app to help a stroke patient with expressive aphasia communicate
by spelling out words and phrases on screen.

## Purpose

The user can still spell, but has difficulty producing speech. ACD gives them
a tablet-sized surface of large alphabet buttons to tap out words, which are
shown on a word display area as they build up.

## Form factor

- Target device: Kindle Fire (tablet, landscape).
- Primary input: large on-screen buttons, designed for tap accuracy rather
  than typing speed.
- Layout: alphabet keys dominate the screen, with a word/phrase display above
  them.
- A screen-select bar lets the user switch between screens (e.g. alphabet,
  and other screens to be defined).

## Features

- **Alphabet entry** *(built)* — the main screen is a grid of large letter keys
  (A–Z) plus a number row (0–9).
- **Word display** *(built)* — characters appear in a display area as they are
  tapped, building into words and phrases.
- **Quick responses** *(built)* — YES / NO keys that insert whole words, adding
  a separating space when needed.
- **Editing controls** *(built)* — space, backspace, clear-word (back to the
  previous space), and clear.
- **Local text-to-speech** *(built)* — a Speak button reads the displayed text
  aloud using on-device TTS, so it works without a network; it prefers a male
  voice.
- **Screen switcher** *(planned)* — a persistent bar for moving between screens.
  The app is currently single-screen.

## Status

In active development. The alphabet-entry screen with TTS is working; targeting
the Fire Max 11 (landscape). Built with Kotlin + Jetpack Compose; see `CLAUDE.md`
for architecture and build/test notes.

## License

MIT — Copyright (c) 2026 Rob Young. See [LICENSE](LICENSE).

## References

### Text-to-speech voice selection

The `android.speech.tts` API has no gender field, so a male voice is chosen by
name (an explicit "male" marker, else a known male en-US voice id). Breadcrumbs:

- [TextToSpeech — Android reference](https://developer.android.com/reference/android/speech/tts/TextToSpeech)
- [Voice — Android reference](https://developer.android.com/reference/android/speech/tts/Voice)
- [Google TTS voices & genders (en-us-x-iom / en-us-x-tpd are male)](https://accessibleandroid.com/google-tts-update-high-quality-voices-hidden-features-and-lingering-mysteries/)
