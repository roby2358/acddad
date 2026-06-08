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

private val DIGITS: List<Char> = ('0'..'9').toList()
private val LETTERS: List<Char> = ('A'..'Z').toList()
private const val KEYS_PER_ROW = 7
private val GAP = 8.dp

private fun phraseSaver(): Saver<Phrase, String> = Saver(
    save = { it.text },
    restore = { Phrase(it) },
)

/**
 * A-Z split into rows of [KEYS_PER_ROW]. Short rows are left-aligned with blank
 * (null) slots padding the end, so every key lines up under the row above.
 */
private fun letterRows(): List<List<Char?>> =
    LETTERS.chunked(KEYS_PER_ROW).map { row -> padRowEnd(row, KEYS_PER_ROW) }

private fun padRowEnd(keys: List<Char>, width: Int): List<Char?> {
    val blanks = width - keys.size
    return keys + List<Char?>(blanks) { null }
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
            onCharacter = { phrase = phrase.append(it) },
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
    onCharacter: (Char) -> Unit,
    onSpace: () -> Unit,
    onBackspace: () -> Unit,
    onClearWord: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(GAP)) {
        ControlRow(onSpace, onBackspace, onClearWord, onClear, Modifier.fillMaxWidth().weight(1f))
        KeyRow(DIGITS, onCharacter, Modifier.fillMaxWidth().weight(1f))
        letterRows().forEach { row ->
            KeyRow(row, onCharacter, Modifier.fillMaxWidth().weight(1f))
        }
    }
}

@Composable
private fun KeyRow(keys: List<Char?>, onCharacter: (Char) -> Unit, modifier: Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(GAP)) {
        keys.forEach { key ->
            if (key == null) {
                Spacer(Modifier.weight(1f).fillMaxHeight())
            } else {
                KeyButton(key.toString(), { onCharacter(key) }, Modifier.weight(1f).fillMaxHeight())
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
