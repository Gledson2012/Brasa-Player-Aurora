package com.example.ui.components

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private fun Context.hasVibratePermission(): Boolean {
    return checkCallingOrSelfPermission(android.Manifest.permission.VIBRATE) ==
        PackageManager.PERMISSION_GRANTED
}

private fun Context.getVibrator(): Vibrator? {
    if (!hasVibratePermission()) return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
}

/**
 * Triggers a light haptic feedback tick.
 * Uses different APIs based on Android version for best compatibility.
 */
fun Context.hapticTick() {
    try {
        val vibrator = getVibrator() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10)
        }
    } catch (_: Exception) { }
}

/**
 * Triggers a heavier haptic feedback for important actions (e.g. play, delete).
 */
fun Context.hapticHeavyClick() {
    try {
        val vibrator = getVibrator() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, 200))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    } catch (_: Exception) { }
}
