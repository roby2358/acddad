package com.example.acd.tally

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * Holds the [WordTally] as observable state and persists it across launches in SharedPreferences.
 * The tally itself stays pure; this is the thin Android-bound glue. Create via [rememberWordMemory].
 */
class WordMemory(context: Context) {

    private val prefs by lazy { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    var tally: WordTally by mutableStateOf(WordTally.EMPTY)
        private set

    fun load() {
        val blob = prefs.getString(KEY, "").orEmpty()
        tally = WordTally.fromLines(blob.lines().filter { it.isNotBlank() })
    }

    /** Record a single completed word (e.g. when space ends a word). */
    fun recordWord(word: String) = update(tally.record(word))

    /** Record every word in a spoken line. */
    fun recordLine(words: List<String>) = update(tally.recordAll(words))

    /** Drop a word from the tally entirely (it can be re-learned by later use). */
    fun forget(word: String) = update(tally.without(word))

    fun top(n: Int, excluding: Set<String>): List<String> = tally.top(n, excluding)

    private fun update(next: WordTally) {
        tally = next
        prefs.edit().putString(KEY, next.toLines().joinToString("\n")).apply()
    }

    private companion object {
        const val PREFS = "acd-word-tally"
        const val KEY = "counts"
    }
}

@Composable
fun rememberWordMemory(): WordMemory {
    val context = LocalContext.current
    val inspecting = LocalInspectionMode.current
    val memory = remember { WordMemory(context.applicationContext) }
    LaunchedEffect(memory) { if (!inspecting) memory.load() }
    return memory
}
