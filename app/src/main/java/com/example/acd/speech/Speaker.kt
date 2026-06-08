package com.example.acd.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import java.util.Locale

/** Readiness of the on-device text-to-speech engine, used to drive the Speak button's look. */
enum class SpeakState { LOADING, READY, ERROR }

internal fun resolveSpeakState(initSucceeded: Boolean, languageAvailable: Boolean): SpeakState {
    if (initSucceeded && languageAvailable) return SpeakState.READY
    return SpeakState.ERROR
}

/** Known male en-US voice ids for Google's TTS engine (no gender field exists to query). */
private val MALE_VOICE_HINTS = listOf("-iom-", "-tpd-")

/**
 * Chooses a male voice by name, since the TTS API exposes no gender. Prefers an explicit
 * "male" marker, then a known male voice id. Returns null when none look male.
 */
internal fun pickMaleVoiceName(voiceNames: List<String>): String? {
    val explicit = voiceNames.firstOrNull { name ->
        val lower = name.lowercase()
        "male" in lower && "female" !in lower
    }
    if (explicit != null) return explicit
    return voiceNames.firstOrNull { name ->
        val lower = name.lowercase()
        MALE_VOICE_HINTS.any { hint -> hint in lower }
    }
}

/**
 * Drives on-device TTS. [state] is observable so the UI reflects loading/ready/error,
 * and is owned here rather than recomputed at call sites. Create via [rememberSpeaker].
 */
class Speaker(private val context: Context) {

    var state: SpeakState by mutableStateOf(SpeakState.LOADING)
        private set

    private var tts: TextToSpeech? = null

    fun start() {
        tts = TextToSpeech(context) { status ->
            val initSucceeded = status == TextToSpeech.SUCCESS
            val languageReady = initSucceeded && languageAvailable()
            if (languageReady) preferMaleVoice()
            state = resolveSpeakState(initSucceeded, languageReady)
        }
    }

    private fun languageAvailable(): Boolean {
        val result = tts?.setLanguage(Locale.US) ?: return false
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    /** Best-effort switch to a male English voice; leaves the default if none is identifiable. */
    private fun preferMaleVoice() {
        val engine = tts ?: return
        val englishVoices = (engine.voices ?: return)
            .filter { it.locale.language == "en" && it.locale.country == "US" }
            .sortedBy { it.isNetworkConnectionRequired }
        val name = pickMaleVoiceName(englishVoices.map { it.name }) ?: return
        engine.setVoice(englishVoices.first { it.name == name })
    }

    fun speak(text: String) {
        if (state != SpeakState.READY) return
        if (text.isBlank()) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        state = SpeakState.LOADING
    }

    private companion object {
        const val UTTERANCE_ID = "acd-phrase"
    }
}

/** Provides a [Speaker] tied to the composition's lifecycle; started on enter, shut down on exit. */
@Composable
fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    val inspecting = LocalInspectionMode.current
    val speaker = remember { Speaker(context.applicationContext) }
    DisposableEffect(speaker) {
        if (!inspecting) speaker.start()
        onDispose { speaker.shutdown() }
    }
    return speaker
}
