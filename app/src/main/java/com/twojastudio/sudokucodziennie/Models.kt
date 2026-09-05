package com.twojastudio.sudokucodziennie

enum class Difficulty(val label: String, val clues: Int) {
    EASY("Łatwy", 42),
    MEDIUM("Średni", 34),
    HARD("Trudny", 29)
}

data class SudokuPuzzle(
    val puzzle: List<Int>,
    val solution: List<Int>
)
