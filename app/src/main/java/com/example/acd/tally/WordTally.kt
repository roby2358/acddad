package com.example.acd.tally

/**
 * Frequency counts of words the user has spoken or completed with a space. Immutable; each
 * `record` returns a new tally. Android-free so the matching/ranking rules are unit tested.
 *
 * Matching is fuzzy and length-aware: a recorded word is merged into an existing entry when it
 * is within an edit-distance budget that grows with word length (see [maxEditsForLength]), so
 * typos of long words consolidate without conflating short distinct words like "cat"/"dog".
 */
data class WordTally(val counts: Map<String, Int>) {

    fun record(raw: String): WordTally {
        val word = raw.trim().lowercase()
        if (word.isEmpty()) return this
        val key = bestMatch(word) ?: word
        return WordTally(counts + (key to (counts[key] ?: 0) + 1))
    }

    fun recordAll(words: List<String>): WordTally = words.fold(this) { tally, word -> tally.record(word) }

    /**
     * The [n] most-frequent words, highest count first (ties broken alphabetically), skipping any
     * word in [excluding] — used to keep words already shown as fixed keys off the learned rows.
     */
    fun top(n: Int, excluding: Set<String>): List<String> =
        counts.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key }
            .filterNot { it in excluding }
            .take(n)

    fun without(word: String): WordTally = WordTally(counts - word.trim().lowercase())

    fun toLines(): List<String> = counts.map { "${it.value}\t${it.key}" }

    /** The existing word closest to [word] within budget, preferring smaller distance then higher count. */
    private fun bestMatch(word: String): String? {
        var best: String? = null
        var bestDistance = Int.MAX_VALUE
        for (existing in counts.keys) {
            val distance = editDistance(word, existing)
            if (distance > maxEditsForLength(maxOf(word.length, existing.length))) continue
            val better = distance < bestDistance ||
                (distance == bestDistance && (counts[existing] ?: 0) > (counts[best] ?: -1))
            if (better) {
                best = existing
                bestDistance = distance
            }
        }
        return best
    }

    companion object {
        val EMPTY = WordTally(emptyMap())

        fun fromLines(lines: List<String>): WordTally {
            val counts = lines.mapNotNull { line ->
                val parts = line.split('\t')
                if (parts.size != 2) return@mapNotNull null
                val count = parts[0].toIntOrNull() ?: return@mapNotNull null
                parts[1] to count
            }.toMap()
            return WordTally(counts)
        }
    }
}

/** Edit-distance budget for fuzzy matching: more edits allowed for longer words. */
internal fun maxEditsForLength(length: Int): Int = when {
    length <= 4 -> 1
    length <= 7 -> 2
    else -> 3
}

/** Levenshtein distance between [a] and [b] (single-character insert/delete/substitute = 1). */
internal fun editDistance(a: String, b: String): Int {
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length
    var prev = IntArray(b.length + 1) { it }
    var curr = IntArray(b.length + 1)
    for (i in 1..a.length) {
        curr[0] = i
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            curr[j] = minOf(prev[j] + 1, curr[j - 1] + 1, prev[j - 1] + cost)
        }
        val swap = prev; prev = curr; curr = swap
    }
    return prev[b.length]
}
