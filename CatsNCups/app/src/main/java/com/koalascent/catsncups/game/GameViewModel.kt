package com.koalascent.catsncups.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class GameViewModel(
    private val generator: Random = Random(System.currentTimeMillis()),
    private val livesObservable: MutableLiveData<Int> = MutableLiveData(),
    private var lives: Int = INITIAL_LIVES,
    private val scoreObservable: MutableLiveData<Int> = MutableLiveData(),
    private var score: Int = INITIAL_POINTS,
    private val gameStateObservable: MutableLiveData<GameState> = MutableLiveData(),
    private val gameActionObservable: MutableLiveData<GameAction> = MutableLiveData(),
    private var lastActionType: GameActionType = GameActionType.NONE,
    private var comboHits: Int = 0
) : ViewModel() {

    init {
        restartGame()
    }

    fun observeLives(): LiveData<Int> {
        return livesObservable
    }

    fun observeScore(): LiveData<Int> {
        return scoreObservable
    }

    fun observeAction(): LiveData<GameAction> {
        return gameActionObservable
    }

    fun observeGameState(): LiveData<GameState> {
        return gameStateObservable
    }

    fun restartGame() {
        comboHits = 0
        updateLives(INITIAL_LIVES)
        updateScore(INITIAL_POINTS)
        actionFeedback(GameActionType.NONE)
        gameStateObservable.value = GameState.STARTED
    }

    fun nextMove() {
        gameStateObservable.value = GameState.ONGOING
    }

    fun evaluateGuess(guessedCup: Int) {
        var correctCup = generator.nextInt() % MAX_CUPS
        if (correctCup < 0) correctCup = -correctCup

        if (correctCup == guessedCup - 1) {
            userGuessedCorrectly(correctCup)
        } else {
            userGuessedWrong(correctCup)
        }

        // did we run out of lives?
        if (lives == 0) {
            gameStateObservable.value = GameState.OVER
        }
    }

    private fun userGuessedCorrectly(correctCup: Int) {
        updateLives(lives + 1)
        actionFeedback(GameActionType.HIT, correctCup)
        comboHits++
        rewardUser()
    }

    private fun userGuessedWrong(correctCup: Int) {
        updateLives(lives - 1)
        actionFeedback(GameActionType.MISS, correctCup)
        comboHits = 0
    }

    private fun rewardUser() {
        val bonus = when(comboHits) {
            1 -> 5
            in 2..3 -> 10
            in 4..5 -> 20
            in 6..8 -> 50
            else -> 100
        }
        updateScore(score + bonus)
    }

    private fun actionFeedback(type: GameActionType, correctCup: Int = 0) {
        lastActionType = type
        gameActionObservable.value = GameAction(type, correctCup)
    }

    private fun updateLives(newValue: Int) {
        lives = newValue
        livesObservable.value = lives
    }

    private fun updateScore(newValue: Int) {
        score = newValue
        scoreObservable.value = score
    }

    companion object {
        private const val MAX_CUPS = 3
        private const val INITIAL_LIVES = 9
        private const val INITIAL_POINTS = 0
    }
}