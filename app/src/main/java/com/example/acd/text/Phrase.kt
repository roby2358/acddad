package com.example.acd.text

/**
 * The text the user has spelled so far.
 *
 * Immutable and free of Android dependencies so the spelling rules can be unit
 * tested directly. Each edit returns a new [Phrase]; the UI holds the current
 * one in state.
 */
data class Phrase(val text: String) {

    fun append(insert: String): Phrase = Phrase(text + insert)

    /** Appends a word, inserting a separating space unless the text is empty or already ends with one. */
    fun appendWord(word: String): Phrase {
        if (text.isEmpty() || text.endsWith(" ")) return append(word)
        return Phrase("$text $word")
    }

    /** Adds a single space, but never a leading or doubled one. */
    fun space(): Phrase {
        if (text.isEmpty()) return this
        if (text.endsWith(" ")) return this
        return Phrase("$text ")
    }

    fun backspace(): Phrase {
        if (text.isEmpty()) return this
        return Phrase(text.dropLast(1))
    }

    /** Removes the last word, leaving the text up to and including the previous space. */
    fun clearWord(): Phrase {
        val trimmed = text.trimEnd(' ')
        val lastSpace = trimmed.lastIndexOf(' ')
        if (lastSpace < 0) return EMPTY
        return Phrase(trimmed.substring(0, lastSpace + 1))
    }

    fun cleared(): Phrase = Phrase("")

    companion object {
        val EMPTY = Phrase("")
    }
}
