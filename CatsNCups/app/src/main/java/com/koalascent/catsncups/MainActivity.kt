package com.koalascent.catsncups

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.view.View.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.koalascent.catsncups.game.GameAction
import com.koalascent.catsncups.game.GameActionType
import com.koalascent.catsncups.game.GameState
import com.koalascent.catsncups.game.GameViewModel
import com.koalascent.catsncups.haptic.HapticFeedbackHelper
import kotlinx.android.extensions.CacheImplementation
import kotlinx.android.extensions.ContainerOptions
import kotlinx.android.synthetic.main.activity_main.*

@ContainerOptions(CacheImplementation.SPARSE_ARRAY)
class MainActivity : AppCompatActivity() {

    private lateinit var gameModel: GameViewModel
    private lateinit var livesView: TextView
    private lateinit var scoreView: TextView
    private lateinit var cupButtons: ArrayList<ImageButton>
    private lateinit var handler: Handler
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        livesView = findViewById(R.id.livesTextView)
        scoreView = findViewById(R.id.roundTextView)
        vibrator = HapticFeedbackHelper.vibratorFor(this)

        cupButtons = arrayListOf(
            findViewById<ImageButton>(R.id.cupButton1).also { it.setOnClickListener(click) },
            findViewById<ImageButton>(R.id.cupButton2).also { it.setOnClickListener(click) },
            findViewById<ImageButton>(R.id.cupButton3).also { it.setOnClickListener(click) }
        )

        handler = Handler()
        gameModel = ViewModelProviders.of(this).get(GameViewModel::class.java)

        gameModel.observeLives().observe(this, Observer { lives ->
            livesView.text = getString(R.string.lives, lives)
        })

        gameModel.observeScore().observe(this, Observer { score ->
            scoreView.text = getString(R.string.score, score)
        })

        gameModel.observeAction().observe(this, Observer { action ->
            performAction(action)
        })

        gameModel.observeGameState().observe(this, Observer { gameState ->
            if (gameState == GameState.STARTED) {
                showGameStart()
                resetCups()
                showCups()
                gameModel.nextMove()
            } else if (gameState == GameState.OVER) {
                hideCups()
                showGameOverDialog()
            }
        })
    }

    private val click: OnClickListener = OnClickListener { v ->
        val guessedCup = when (v.id) {
            R.id.cupButton1 -> 1
            R.id.cupButton2 -> 2
            R.id.cupButton3 -> 3
            else -> 0
        }
        if (guessedCup > 0) {
            handler.removeCallbacksAndMessages(null)
            resetCups()
            gameModel.evaluateGuess(guessedCup)
        }
    }

    private fun performAction(action: GameAction) {
        livesView.setTextColor(ContextCompat.getColor(this, livesColorFor(action)))

        when (action.type) {
            GameActionType.HIT -> {
                showCat(R.drawable.cup_win1, action.correctCup)
                HapticFeedbackHelper.vibrateFor(action.type, vibrator)
            }
            GameActionType.MISS -> {
                showCat(R.drawable.cup_lose1, action.correctCup)
                HapticFeedbackHelper.vibrateFor(action.type, vibrator)
            }
            else -> {
            }
        }
    }

    private fun livesColorFor(action: GameAction): Int = when (action.type) {
        GameActionType.HIT -> R.color.hitColor
        GameActionType.MISS -> R.color.missColor
        else -> R.color.noneColor
    }

    private fun showCat(imageResource: Int, cup: Int) {
        cupButtons[cup].let { selectedButton ->
            selectedButton.setImageResource(imageResource)
            handler.postDelayed({
                resetCups()
            }, 700)
        }
    }

    private fun showCups() {
        cupButtons.forEach { it.visibility = VISIBLE }
    }

    private fun hideCups() {
        cupButtons.forEach { it.visibility = INVISIBLE }
    }

    private fun resetCups() {
        cupButtons.forEach { it.setImageResource(R.drawable.cup_empty) }
    }

    private fun showGameStart() {
        Snackbar.make(rootLayout, R.string.instructions, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.dismiss) {}
            .show()
    }

    // create dialogue to inform end of game and ask if user wants to continue
    private fun showGameOverDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.gameOver))
            .setMessage(getString(R.string.game_over_message, gameModel.observeScore().value))
            .setCancelable(false)
            .setPositiveButton(R.string.newGame) { _, _ -> gameModel.restartGame() }
            .setNegativeButton(R.string.quit) { _, _ -> finish() }
            .create()
            .show()
    }
}