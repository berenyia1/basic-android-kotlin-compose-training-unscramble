package com.example.unscramble.ui

/**
 * UI State
 * The UI is what the user sees, and the UI state is what the app says they should see.
 * The UI is the visual representation of the UI state. Any changes to the UI state immediately
 * are reflected in the UI.
 */
data class GameUiState(
    val currentScrambledWord: String = "",
    val isGuessedWordWrong: Boolean = false,
    val score: Int = 0,
    val currentWordCount: Int = 1,
    val isGameOver: Boolean = false,
)
