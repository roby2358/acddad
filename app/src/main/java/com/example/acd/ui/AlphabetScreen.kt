package com.example.acd.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Column(
        modifier = modifier.fillMaxSize().padding(GAP),
        verticalArrangement = Arrangement.spacedBy(GAP),
    ) {
        WordDisplay(text = phrase.text, modifier = Modifier.fillMaxWidth().weight(1f))
        Keyboard(
            onKey = { action -> phrase = action(phrase) },
            onSpace = { phrase = phrase.space() },
            onBackspace = { phrase = phrase.backspace() },
            onClearWord = { phrase = phrase.clearWord() },
            onClear = { phrase = phrase.cleared() },
            modifier = Modifier.fillMaxWidth().weight(4f),
        )
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
