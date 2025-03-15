package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.update

/**
 * The ViewModel component holds and exposes the state the UI consumes. The UI state is application
 * data transformed by ViewModel. ViewModel lets your app follow the architecture principle of
 * driving the UI from the model.
 *
 * ViewModel stores the app-related data that isn't destroyed when the activity is destroyed and
 * recreated by the Android framework. Unlike the activity instance, ViewModel objects are not
 * destroyed. The app automatically retains ViewModel objects during configuration changes so that
 * the data they hold is immediately available after the recomposition.
 *
 * To implement ViewModel in your app, extend the ViewModel class, which comes from the architecture
 * components library and stores app data within that class.
 */
class GameViewModel : ViewModel() {

    // StateFlow is a data holder observable flow that emits the current and new state updates.
    // Its value property reflects the current state value. To update state and send it to the flow,
    // assign a new value to the value property of the MutableStateFlow class.
    // In Android, StateFlow works well with classes that must maintain an observable immutable state.
    // A StateFlow can be exposed from the GameUiState so that the composables can listen for UI
    // state updates and make the screen state survive configuration changes.

    // Game UI state
    private val _uiState = MutableStateFlow(GameUiState())

    //The asStateFlow() makes this mutable state flow a read-only state flow.
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // the current scrambled word.
    private lateinit var currentWord: String

    // Set of words used in the game
    private var usedWords: MutableSet<String> = mutableSetOf()

    // Display the guess word. Use mutableStateOf() so that Compose observes this value.
    var userGuess by mutableStateOf("")
        private set

    init {
        resetGame()
    }

    private fun pickRandomWordAndShuffle(): String {
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord = allWords.random()
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        // Scramble the word
        tempWord.shuffle()
        while (String(tempWord).equals(word)) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    // Use this function to start and restart the game.
    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }


    // In the GameLayout() composable, updating the user's guess word is one of event callbacks that
    // flows up from GameScreen to the ViewModel. The data gameViewModel.userGuess will flow down
    // from the ViewModel to the GameScreen.
    fun updateUserGuess(guessedWord: String){
        userGuess = guessedWord
    }

    // Verify if the user's guess is the same as the currentWord. Reset userGuess to empty string.
    fun checkUserGuess() {

        if (userGuess.trim().equals(currentWord, ignoreCase = true)) {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    //currentWordCount = currentState.currentWordCount.inc(),
                    )
            }
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)

        } else {
            // User's guess is wrong, show an error
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // Reset user guess
        updateUserGuess("")
    }

    private fun updateGameState(updatedScore: Int) {

        if (usedWords.size == MAX_NO_OF_WORDS){
            //Last round in the game, update isGameOver to true, don't pick a new word
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        } else{
            // Normal round in the game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    currentWordCount = currentState.currentWordCount.inc(),
                    score = updatedScore
                )
            }
        }

    }

    fun skipWord() {
        updateGameState(_uiState.value.score)
        // Reset user guess
        updateUserGuess("")
    }
}