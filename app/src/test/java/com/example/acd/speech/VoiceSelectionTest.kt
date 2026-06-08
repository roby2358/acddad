package com.example.acd.speech

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceSelectionTest {

    @Test
    fun prefersExplicitMaleMarker() {
        val names = listOf("en-us-female-1", "en-us-male-1")
        assertEquals("en-us-male-1", pickMaleVoiceName(names))
    }

    @Test
    fun fallsBackToKnownGoogleMaleId() {
        val names = listOf("en-us-x-sfg-local", "en-us-x-iom-local")
        assertEquals("en-us-x-iom-local", pickMaleVoiceName(names))
    }

    @Test
    fun returnsNullWhenNoVoiceLooksMale() {
        val names = listOf("en-us-x-sfg-local", "en-us-x-tpf-local")
        assertNull(pickMaleVoiceName(names))
    }

    @Test
    fun doesNotMistakeFemaleForMale() {
        // "female" contains the substring "male" — must not be treated as male.
        assertNull(pickMaleVoiceName(listOf("en-us-female-1")))
    }
}
