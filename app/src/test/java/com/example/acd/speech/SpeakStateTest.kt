package com.example.acd.speech

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeakStateTest {

    @Test
    fun ready_whenInitAndLanguageBothSucceed() {
        assertEquals(SpeakState.READY, resolveSpeakState(initSucceeded = true, languageAvailable = true))
    }

    @Test
    fun error_whenInitFails() {
        assertEquals(SpeakState.ERROR, resolveSpeakState(initSucceeded = false, languageAvailable = true))
    }

    @Test
    fun error_whenLanguageUnavailable() {
        assertEquals(SpeakState.ERROR, resolveSpeakState(initSucceeded = true, languageAvailable = false))
    }
}
