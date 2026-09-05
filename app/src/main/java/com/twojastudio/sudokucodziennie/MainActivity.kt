package com.twojastudio.sudokucodziennie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SudokuTheme { SudokuApp() } }
    }
}

@Composable
private fun SudokuTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = if (dark) {
        darkColorScheme(
            primary = Color(0xFF9DB0FF),
            primaryContainer = Color(0xFF273C8A),
            secondary = Color(0xFF76D6C5),
            background = Color(0xFF0B1220),
            surface = Color(0xFF111827)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF3858C8),
            primaryContainer = Color(0xFFDCE2FF),
            secondary = Color(0xFF006B5D),
            background = Color(0xFFF7F8FC),
            surface = Color(0xFFFFFBFF)
        )
    }
    MaterialTheme(colorScheme = colors, content = content)
}

@Composable
private fun SudokuApp() {
    var difficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var puzzle by remember { mutableStateOf<SudokuPuzzle?>(null) }
    var board by remember { mutableStateOf(List(81) { 0 }) }
    var selected by remember { mutableIntStateOf(-1) }
    var mistakes by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableIntStateOf(0) }
    var request by remember { mutableIntStateOf(1) }
    var daily by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }

    LaunchedEffect(request, difficulty, daily) {
        puzzle = null
        val generated = withContext(Dispatchers.Default) {
            val random = if (daily) {
                Random((LocalDate.now().toEpochDay() * 104729L).toInt())
            } else Random.Default
            SudokuEngine.generate(difficulty, random)
        }
        puzzle = generated
        board = generated.puzzle
        selected = -1
        mistakes = 0
        seconds = 0
        won = false
    }

    LaunchedEffect(puzzle, won) {
        while (puzzle != null && !won) {
            delay(1000)
            seconds++
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Sudoku Codziennie",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (daily) "Sudoku dnia • ${difficulty.label}" else "Klasyczne • ${difficulty.label}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.entries.forEach { level ->
                    FilterChip(
                        selected = difficulty == level,
                        onClick = { difficulty = level; daily = false; request++ },
                        label = { Text(level.label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Czas ${formatTime(seconds)}", fontWeight = FontWeight.SemiBold)
                Text("Błędy $mistakes", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))

            if (puzzle == null) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                SudokuBoard(
                    puzzle = puzzle!!,
                    board = board,
                    selected = selected,
                    onSelect = { selected = it },
                    modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp)
                )

                Spacer(Modifier.height(10.dp))
                NumberPad(
                    onNumber = { number ->
                        val current = puzzle ?: return@NumberPad
                        if (selected !in 0..80 || current.puzzle[selected] != 0 || won) return@NumberPad
                        val updated = board.toMutableList()
                        updated[selected] = number
                        if (number != current.solution[selected]) mistakes++
                        board = updated
                        if (updated == current.solution) won = true
                    },
                    onErase = {
                        val current = puzzle
                        if (current != null && selected in 0..80 && current.puzzle[selected] == 0 && !won) {
                            board = board.toMutableList().also { it[selected] = 0 }
                        }
                    }
                )

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { daily = true; request++ },
                        modifier = Modifier.weight(1f)
                    ) { Text("Sudoku dnia") }
                    Button(
                        onClick = { daily = false; request++ },
                        modifier = Modifier.weight(1f)
                    ) { Text("Nowa gra") }
                }
            }

            if (won) {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Brawo! 🎉", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("Rozwiązano w ${formatTime(seconds)} • błędy: $mistakes")
                    }
                }
            }
        }
    }
}

@Composable
private fun SudokuBoard(
    puzzle: SudokuPuzzle,
    board: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(4.dp))) {
        for (row in 0 until 9) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 9) {
                    val index = row * 9 + col
                    val given = puzzle.puzzle[index] != 0
                    val value = board[index]
                    val isSelected = selected == index
                    val related = selected >= 0 && (selected / 9 == row || selected % 9 == col ||
                        ((selected / 27) == (index / 27) && ((selected % 9) / 3) == (col / 3)))
                    val background = when {
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        related -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.surface
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(background)
                            .border(
                                width = if (col % 3 == 0 || row % 3 == 0) 0.8.dp else 0.35.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            .clickable { onSelect(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (value != 0) {
                            val wrong = !given && value != puzzle.solution[index]
                            Text(
                                value.toString(),
                                fontSize = 20.sp,
                                fontWeight = if (given) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    wrong -> MaterialTheme.colorScheme.error
                                    given -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberPad(onNumber: (Int) -> Unit, onErase: () -> Unit) {
    Column(Modifier.fillMaxWidth().widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (start in listOf(1, 4, 7)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (number in start..start + 2) {
                    OutlinedButton(onClick = { onNumber(number) }, modifier = Modifier.weight(1f)) {
                        Text(number.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        OutlinedButton(onClick = onErase, modifier = Modifier.fillMaxWidth()) { Text("Usuń cyfrę") }
    }
}

private fun formatTime(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
