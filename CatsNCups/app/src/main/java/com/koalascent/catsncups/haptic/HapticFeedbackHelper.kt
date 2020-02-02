package com.koalascent.catsncups.haptic

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import com.koalascent.catsncups.game.GameActionType

object HapticFeedbackHelper {

    fun vibratorFor(context: Context): Vibrator {
        return context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
    }

    fun vibrateFor(action: GameActionType, vibrator: Vibrator) {
        val duration: Long = if (GameActionType.MISS == action)  10 else 60

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration) // deprecated method necessary for older APIs
        }
    }
}