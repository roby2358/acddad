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
- A screen-select bar (a vertical bar on the right) switches between the
  letters and words screens.

## Features

- **Alphabet entry** *(built)* — the main screen is a grid of large letter keys
  (A–Z) plus a number row (0–9).
- **Word display** *(built)* — characters appear in a display area as they are
  tapped, building into words and phrases, with a trailing `_` cursor marking the
  end of the line.
- **Quick responses** *(built)* — YES / NO keys that insert the word (with smart
  spacing) and speak it aloud immediately.
- **Editing controls** *(built)* — space (no leading/double spaces), backspace,
  clear-word (back to the previous space), and clear; shared across both screens.
- **Words screen** *(built)* — whole-word keys: common hospital/AAC words plus two
  rows of the user's most-used words, learned automatically and saved across
  sessions (tap a learned word 5× in a row to drop it).
- **Screen switcher** *(built)* — a vertical bar switches between the letters and
  words screens.
- **Local text-to-speech** *(built)* — a Speak button reads the displayed text
  aloud using on-device TTS, so it works without a network; it prefers a male
  voice.
- **Diagnostic panel** *(built, hidden)* — typing `UUUUU` reveals a frequency table
  of learned words with a "clear all".

## Status

In active development. The letters and words screens — with learned-word
prediction and on-device TTS — are working; targeting the Fire Max 11 (landscape).
Built with Kotlin + Jetpack Compose. See `USAGE.md` for the controls and
`CLAUDE.md` for architecture and build/test notes.

## License

MIT — Copyright (c) 2026 Rob Young. See [LICENSE](LICENSE).

## References

### Text-to-speech voice selection

The `android.speech.tts` API has no gender field, so a male voice is chosen by
name (an explicit "male" marker, else a known male en-US voice id). Breadcrumbs:

- [TextToSpeech — Android reference](https://developer.android.com/reference/android/speech/tts/TextToSpeech)
- [Voice — Android reference](https://developer.android.com/reference/android/speech/tts/Voice)
- [Google TTS voices & genders (en-us-x-iom / en-us-x-tpd are male)](https://accessibleandroid.com/google-tts-update-high-quality-voices-hidden-features-and-lingering-mysteries/)
