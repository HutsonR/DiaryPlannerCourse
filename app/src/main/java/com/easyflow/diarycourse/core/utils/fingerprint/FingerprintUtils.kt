package com.easyflow.diarycourse.core.utils.fingerprint

import android.app.KeyguardManager
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import androidx.core.hardware.fingerprint.FingerprintManagerCompat


class FingerprintUtils {

    enum class SensorState {
        NOT_SUPPORTED,
        NOT_BLOCKED,
        // если устройство не защищено пином, рисунком или паролем
        NO_FINGERPRINTS,
        // если на устройстве нет отпечатков
        READY
    }

    private fun checkFingerprintCompatibility(context: Context): Boolean {
        return FingerprintManagerCompat.from(context).isHardwareDetected
    }

    private fun checkSensorState(context: Context): SensorState {
        return if (checkFingerprintCompatibility(context)) {
            val keyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (!keyguardManager.isKeyguardSecure) {
                return SensorState.NOT_BLOCKED
            }
            val fingerprintManager = FingerprintManagerCompat.from(context)
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                SensorState.NO_FINGERPRINTS
            } else SensorState.READY
        } else {
            SensorState.NOT_SUPPORTED
        }
    }

    fun isSensorStateAt(
        state: SensorState,
        context: Context
    ): Boolean {
        return checkSensorState(context) == state
    }

}