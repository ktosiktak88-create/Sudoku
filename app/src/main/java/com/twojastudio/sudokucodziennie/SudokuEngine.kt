package com.twojastudio.sudokucodziennie

import kotlin.random.Random

object SudokuEngine {
    fun generate(difficulty: Difficulty, random: Random = Random.Default): SudokuPuzzle {
        val solved = IntArray(81)
        check(fillBoard(solved, random))
        val puzzle = solved.copyOf()
        val order = (0 until 81).shuffled(random)
        var filled = 81
        for (index in order) {
            if (filled <= difficulty.clues) break
            val backup = puzzle[index]
            puzzle[index] = 0
            val copy = puzzle.copyOf()
            if (countSolutions(copy, 2) != 1) puzzle[index] = backup else filled--
        }
        return SudokuPuzzle(puzzle.toList(), solved.toList())
    }

    private fun fillBoard(board: IntArray, random: Random): Boolean {
        val empty = findBestEmptyCell(board) ?: return true
        for (number in (1..9).shuffled(random)) {
            if (isAllowed(board, empty, number)) {
                board[empty] = number
                if (fillBoard(board, random)) return true
                board[empty] = 0
            }
        }
        return false
    }

    private fun countSolutions(board: IntArray, limit: Int): Int {
        val empty = findBestEmptyCell(board) ?: return 1
        var count = 0
        for (number in 1..9) {
            if (isAllowed(board, empty, number)) {
                board[empty] = number
                count += countSolutions(board, limit - count)
                board[empty] = 0
                if (count >= limit) return count
            }
        }
        return count
    }

    private fun findBestEmptyCell(board: IntArray): Int? {
        var best = -1
        var bestCount = 10
        for (index in board.indices) {
            if (board[index] != 0) continue
            var count = 0
            for (n in 1..9) if (isAllowed(board, index, n)) count++
            if (count < bestCount) {
                best = index
                bestCount = count
                if (count <= 1) break
            }
        }
        return best.takeIf { it >= 0 }
    }

    private fun isAllowed(board: IntArray, index: Int, value: Int): Boolean {
        val row = index / 9
        val col = index % 9
        for (c in 0 until 9) if (board[row * 9 + c] == value) return false
        for (r in 0 until 9) if (board[r * 9 + col] == value) return false
        val br = (row / 3) * 3
        val bc = (col / 3) * 3
        for (r in br until br + 3) for (c in bc until bc + 3) if (board[r * 9 + c] == value) return false
        return true
    }
}
