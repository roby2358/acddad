package com.example.acd.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class PhraseTest {

    @Test
    fun append_addsLetterToEnd() {
        assertEquals("CAT", Phrase("CA").append("T").text)
    }

    @Test
    fun append_startingFromEmpty() {
        assertEquals("A", Phrase.EMPTY.append("A").text)
    }

    @Test
    fun append_addsWholeWord() {
        assertEquals("YES", Phrase.EMPTY.append("YES").text)
    }

    @Test
    fun appendWord_addsSeparatingSpaceWhenNeeded() {
        assertEquals("WORLD YES", Phrase("WORLD").appendWord("YES").text)
    }

    @Test
    fun appendWord_doesNotDoubleSpace() {
        assertEquals("I AM YES", Phrase("I AM ").appendWord("YES").text)
    }

    @Test
    fun appendWord_onEmpty_hasNoLeadingSpace() {
        assertEquals("NO", Phrase.EMPTY.appendWord("NO").text)
    }

    @Test
    fun space_addsSingleTrailingSpace() {
        assertEquals("HI ", Phrase("HI").space().text)
    }

    @Test
    fun space_onEmpty_doesNothing() {
        assertSame(Phrase.EMPTY, Phrase.EMPTY.space())
    }

    @Test
    fun space_whenAlreadyEndsWithSpace_doesNothing() {
        assertEquals("HI ", Phrase("HI ").space().text)
    }

    @Test
    fun backspace_removesLastCharacter() {
        assertEquals("CA", Phrase("CAT").backspace().text)
    }

    @Test
    fun backspace_onEmpty_returnsSameInstance() {
        assertSame(Phrase.EMPTY, Phrase.EMPTY.backspace())
    }

    @Test
    fun clearWord_removesLastWordBackToPreviousSpace() {
        assertEquals("CAT ", Phrase("CAT DOG").clearWord().text)
    }

    @Test
    fun clearWord_ignoresTrailingSpaces() {
        assertEquals("CAT ", Phrase("CAT DOG  ").clearWord().text)
    }

    @Test
    fun clearWord_withSingleWord_clearsEverything() {
        assertEquals("", Phrase("HELLO").clearWord().text)
    }

    @Test
    fun clearWord_onEmpty_isEmpty() {
        assertEquals("", Phrase.EMPTY.clearWord().text)
    }

    @Test
    fun cleared_isEmpty() {
        assertEquals("", Phrase("HELLO").cleared().text)
    }
}
