package com.example.acd.tally

import org.junit.Assert.assertEquals
import org.junit.Test

class WordTallyTest {

    @Test
    fun editDistance_basics() {
        assertEquals(0, editDistance("water", "water"))
        assertEquals(1, editDistance("water", "watere"))
        assertEquals(3, editDistance("cat", "dog"))
        assertEquals(3, editDistance("kitten", "sitting"))
        assertEquals(4, editDistance("", "four"))
    }

    @Test
    fun record_isCaseInsensitiveAndCountsExactRepeats() {
        val tally = WordTally.EMPTY.record("Water").record("WATER")
        assertEquals(mapOf("water" to 2), tally.counts)
    }

    @Test
    fun record_mergesNearTypoOfLongWord() {
        // "bathroom" (8) vs "bathrooms" (9): distance 1, budget 3 -> merge, keep first spelling.
        val tally = WordTally.EMPTY.record("bathroom").record("bathrooms")
        assertEquals(mapOf("bathroom" to 2), tally.counts)
    }

    @Test
    fun record_keepsShortDistinctWordsSeparate() {
        // "cat" vs "dog": distance 3, budget 1 for short words -> no merge.
        val tally = WordTally.EMPTY.record("cat").record("dog")
        assertEquals(mapOf("cat" to 1, "dog" to 1), tally.counts)
    }

    @Test
    fun record_blankIsIgnored() {
        assertEquals(WordTally.EMPTY.counts, WordTally.EMPTY.record("   ").counts)
    }

    @Test
    fun recordAll_talliesEveryWord() {
        val tally = WordTally.EMPTY.recordAll(listOf("i", "want", "water", "water"))
        assertEquals(2, tally.counts["water"])
        assertEquals(1, tally.counts["want"])
    }

    @Test
    fun top_ordersByCountThenName_andExcludes() {
        val tally = WordTally(mapOf("water" to 5, "help" to 5, "tired" to 2, "cup" to 1))
        // help & water tie at 5 -> alphabetical; "help" is excluded as a fixed key.
        assertEquals(listOf("water", "tired", "cup"), tally.top(3, setOf("help")))
    }

    @Test
    fun ranked_ordersAllByCountThenName() {
        val tally = WordTally(mapOf("water" to 5, "help" to 5, "cup" to 1))
        assertEquals(listOf("help" to 5, "water" to 5, "cup" to 1), tally.ranked())
    }

    @Test
    fun without_removesTheWord() {
        val tally = WordTally(mapOf("water" to 3, "help" to 1))
        assertEquals(mapOf("help" to 1), tally.without("WATER").counts)
    }

    @Test
    fun toLines_fromLines_roundTrip() {
        val tally = WordTally(mapOf("water" to 3, "help" to 1))
        assertEquals(tally.counts, WordTally.fromLines(tally.toLines()).counts)
    }

    @Test
    fun maxEditsForLength_growsWithLength() {
        assertEquals(1, maxEditsForLength(4))
        assertEquals(2, maxEditsForLength(7))
        assertEquals(3, maxEditsForLength(8))
    }
}
