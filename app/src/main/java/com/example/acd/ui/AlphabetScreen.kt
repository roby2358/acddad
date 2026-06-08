package com.example.acd.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.acd.speech.SpeakState
import com.example.acd.speech.rememberSpeaker
import com.example.acd.tally.rememberWordMemory
import com.example.acd.text.Phrase
import com.example.acd.ui.theme.AcdTheme

private const val KEYS_PER_ROW = 7
private val GAP = 8.dp

private typealias KeyAction = (Phrase) -> Phrase

/** A tappable key: [label] is shown on the button, [apply] edits the phrase when pressed. */
private data class Key(val label: String, val apply: KeyAction)

private fun charKey(c: Char): Key = Key(c.toString()) { it.append(c.toString()) }
private fun wordKey(word: String): Key = Key(word) { it.appendWord(word) }

private val DIGIT_KEYS: List<Key> = ('0'..'9').map(::charKey)
private val LETTER_KEYS: List<Key> = ('A'..'Z').map(::charKey)
private val WORD_KEYS: List<Key> = listOf(wordKey("YES"), wordKey("NO"))

private const val WORDS_PER_ROW = 4
private const val LEARNED_SLOTS = 2 * WORDS_PER_ROW
private const val LEARNED_REMOVE_TAPS = 5
private val BAR_WIDTH = 88.dp

/** Fixed words-panel vocabulary (3 rows): common hospital communication-board needs. */
private val FIXED_WORDS = listOf(
    "help", "pain", "water", "bathroom",
    "more", "less", "medicine", "blanket",
    "hungry", "cold", "hot", "tired",
)
private val WORD_PANEL_KEYS: List<Key> = FIXED_WORDS.map(::wordKey)

/** Words already shown as fixed keys (here, plus YES/NO on the alphabet panel); kept off the learned rows. */
private val SCREEN_WORDS: Set<String> = (FIXED_WORDS + listOf("yes", "no")).map { it.lowercase() }.toSet()

/** The selectable panels; the switcher bar cycles to the next one. */
private enum class Panel { ALPHABET, WORDS }

private fun Panel.next(): Panel = Panel.entries[(ordinal + 1) % Panel.entries.size]

private fun phraseSaver(): Saver<Phrase, String> = Saver(
    save = { it.text },
    restore = { Phrase(it) },
)

/**
 * Letter keys followed by the YES/NO word keys, split into rows of [KEYS_PER_ROW].
 * Short rows are left-aligned with blank (null) slots padding the end, so every key
 * lines up under the row above.
 */
private fun letterRows(): List<List<Key?>> =
    (LETTER_KEYS + WORD_KEYS).chunked(KEYS_PER_ROW).map { row -> padRowEnd(row, KEYS_PER_ROW) }

private fun <T> padRowEnd(items: List<T>, width: Int): List<T?> {
    val blanks = width - items.size
    return items + List<T?>(blanks) { null }
}

/**
 * The main alphabet-entry screen: a word display above a grid of large letter
 * keys. Tapping a key edits the [Phrase] held in state and shown above.
 */
@Composable
fun AlphabetScreen(modifier: Modifier) {
    var phrase by rememberSaveable(stateSaver = phraseSaver()) { mutableStateOf(Phrase.EMPTY) }
    var panel by rememberSaveable { mutableStateOf(Panel.ALPHABET) }
    val speaker = rememberSpeaker()
    val memory = rememberWordMemory()

    // Tapping the same learned word LEARNED_REMOVE_TAPS times in a row forgets it; any other action resets.
    var streakWord by remember { mutableStateOf<String?>(null) }
    var streakCount by remember { mutableStateOf(0) }
    val resetStreak = { streakWord = null; streakCount = 0 }

    val onKey: (KeyAction) -> Unit = { action ->
        resetStreak()
        phrase = action(phrase)
    }
    val onBackspace = { onKey(Phrase::backspace) }
    val onClearWord = { onKey(Phrase::clearWord) }
    val onClear = { onKey(Phrase::cleared) }
    val onSpace = {
        resetStreak()
        val spaced = phrase.space()
        if (spaced !== phrase) memory.recordWord(phrase.lastWord())
        phrase = spaced
    }
    val onLearnedWord: (String) -> Unit = { word ->
        streakCount = if (word == streakWord) streakCount + 1 else 1
        streakWord = word
        phrase = phrase.appendWord(word)
        if (streakCount >= LEARNED_REMOVE_TAPS) {
            memory.forget(word)
            resetStreak()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(GAP),
        verticalArrangement = Arrangement.spacedBy(GAP),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(GAP),
        ) {
            WordDisplay(text = phrase.text, modifier = Modifier.weight(1f).fillMaxHeight())
            SpeakButton(
                state = speaker.state,
                onSpeak = {
                    resetStreak()
                    speaker.speak(phrase.text)
                    val spoken = phrase.words()
                    if (spoken.isNotEmpty()) memory.recordLine(spoken)
                },
                modifier = Modifier.fillMaxHeight().aspectRatio(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(4f),
            horizontalArrangement = Arrangement.spacedBy(GAP),
        ) {
            val panelModifier = Modifier.weight(1f).fillMaxHeight()
            when (panel) {
                Panel.ALPHABET -> Keyboard(
                    onKey = onKey,
                    onSpace = onSpace,
                    onBackspace = onBackspace,
                    onClearWord = onClearWord,
                    onClear = onClear,
                    modifier = panelModifier,
                )
                Panel.WORDS -> WordsPanel(
                    onKey = onKey,
                    onLearnedWord = onLearnedWord,
                    onClearWord = onClearWord,
                    onClear = onClear,
                    learnedWords = memory.top(LEARNED_SLOTS, SCREEN_WORDS),
                    modifier = panelModifier,
                )
            }
            SwitcherBar(
                onClick = { resetStreak(); panel = panel.next() },
                modifier = Modifier.fillMaxHeight().width(BAR_WIDTH),
            )
        }
    }
}

@Composable
private fun WordDisplay(text: String, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(GAP),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 44.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Square button that speaks the current phrase. Enabled (and showing a speaker glyph) only
 * when TTS is ready; otherwise greyed out, showing a clock while loading or a dot on error.
 */
@Composable
private fun SpeakButton(state: SpeakState, onSpeak: () -> Unit, modifier: Modifier) {
    Button(
        onClick = onSpeak,
        enabled = state == SpeakState.READY,
        modifier = modifier,
        shape = RoundedCornerShape(GAP),
        contentPadding = PaddingValues(GAP),
    ) {
        SpeakIcon(state = state, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun SpeakIcon(state: SpeakState, modifier: Modifier) {
    val tint = LocalContentColor.current
    Canvas(modifier = modifier) {
        when (state) {
            SpeakState.READY -> drawSpeaker(tint)
            SpeakState.LOADING -> drawClock(tint)
            SpeakState.ERROR -> drawDot(tint)
        }
    }
}

/** Speaker source dot with three radiating sound waves. */
private fun DrawScope.drawSpeaker(color: Color) {
    val s = size.minDimension
    val center = Offset(s * 0.30f, s * 0.5f)
    drawCircle(color = color, radius = s * 0.10f, center = center)
    val stroke = Stroke(width = s * 0.06f, cap = StrokeCap.Round)
    for (i in 1..3) {
        val r = s * (0.13f + i * 0.12f)
        drawArc(
            color = color,
            startAngle = -50f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(center.x - r, center.y - r),
            size = Size(r * 2, r * 2),
            style = stroke,
        )
    }
}

/** Clock face with two hands, shown while the engine is still loading. */
private fun DrawScope.drawClock(color: Color) {
    val s = size.minDimension
    val center = Offset(s * 0.5f, s * 0.5f)
    val r = s * 0.38f
    val width = s * 0.06f
    drawCircle(color = color, radius = r, center = center, style = Stroke(width = width))
    drawLine(color, center, Offset(center.x, center.y - r * 0.55f), strokeWidth = width, cap = StrokeCap.Round)
    drawLine(color, center, Offset(center.x + r * 0.42f, center.y), strokeWidth = width, cap = StrokeCap.Round)
}

/** A single thick dot, shown when TTS failed to initialise. */
private fun DrawScope.drawDot(color: Color) {
    drawCircle(color = color, radius = size.minDimension * 0.22f, center = center)
}

/** Unlabeled vertical bar left of the keys; tapping it switches to the next panel. */
@Composable
private fun SwitcherBar(onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(GAP),
        color = MaterialTheme.colorScheme.primary,
    ) {}
}

/**
 * A clear-word/clear control row, then the fixed hospital words, then two rows of [learnedWords]
 * (the most-used words, padded with blanks). Each word inserts via [Phrase.appendWord].
 */
@Composable
private fun WordsPanel(
    onKey: (KeyAction) -> Unit,
    onLearnedWord: (String) -> Unit,
    onClearWord: () -> Unit,
    onClear: () -> Unit,
    learnedWords: List<String>,
    modifier: Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(GAP)) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(GAP),
        ) {
            KeyButton("clear word", onClearWord, Modifier.weight(3f).fillMaxHeight())
            KeyButton("clear", onClear, Modifier.weight(1f).fillMaxHeight())
        }
        WORD_PANEL_KEYS.chunked(WORDS_PER_ROW).forEach { row ->
            KeyRow(padRowEnd(row, WORDS_PER_ROW), onKey, Modifier.fillMaxWidth().weight(1f))
        }
        val learnedSlots: List<String?> = List(LEARNED_SLOTS) { i -> learnedWords.getOrNull(i) }
        learnedSlots.chunked(WORDS_PER_ROW).forEach { row ->
            LearnedRow(row, onLearnedWord, Modifier.fillMaxWidth().weight(1f))
        }
    }
}

/** A row of learned-word keys; each reports the raw word so the screen can track the remove-streak. */
@Composable
private fun LearnedRow(words: List<String?>, onLearnedWord: (String) -> Unit, modifier: Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(GAP)) {
        words.forEach { word ->
            if (word == null) {
                Spacer(Modifier.weight(1f).fillMaxHeight())
            } else {
                KeyButton(word, { onLearnedWord(word) }, Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun Keyboard(
    onKey: (KeyAction) -> Unit,
    onSpace: () -> Unit,
    onBackspace: () -> Unit,
    onClearWord: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(GAP)) {
        ControlRow(onSpace, onBackspace, onClearWord, onClear, Modifier.fillMaxWidth().weight(1f))
        KeyRow(DIGIT_KEYS, onKey, Modifier.fillMaxWidth().weight(1f))
        letterRows().forEach { row ->
            KeyRow(row, onKey, Modifier.fillMaxWidth().weight(1f))
        }
    }
}

@Composable
private fun KeyRow(keys: List<Key?>, onKey: (KeyAction) -> Unit, modifier: Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(GAP)) {
        keys.forEach { key ->
            if (key == null) {
                Spacer(Modifier.weight(1f).fillMaxHeight())
            } else {
                KeyButton(key.label, { onKey(key.apply) }, Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun ControlRow(
    onSpace: () -> Unit,
    onBackspace: () -> Unit,
    onClearWord: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(GAP)) {
        KeyButton("space", onSpace, Modifier.weight(2.5f).fillMaxHeight())
        KeyButton("⌫", onBackspace, Modifier.weight(1f).fillMaxHeight())
        KeyButton("clear word", onClearWord, Modifier.weight(1.5f).fillMaxHeight())
        KeyButton("clear", onClear, Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
private fun KeyButton(label: String, onClick: () -> Unit, modifier: Modifier) {
    Button(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(GAP)) {
        Text(text = label, fontSize = 28.sp, fontWeight = FontWeight.Medium)
    }
}

// Fire Max 11 in landscape: 2000x1200 px at ~213 dpi ≈ 1502x901 dp.
@Preview(showBackground = true, widthDp = 1502, heightDp = 901)
@Composable
private fun AlphabetScreenPreview() {
    AcdTheme {
        AlphabetScreen(modifier = Modifier)
    }
}
