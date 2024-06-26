package com.easyflow.diarycourse.core.utils.fingerprint.utils

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.easyflow.diarycourse.core.utils.fingerprint.common.BiometricAuthListener

object BiometricUtils {

    /*
     * Check whether the Device is Capable of the Biometric
     */
    private fun hasBiometricCapability(context: Context): Int {
        return BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
    }

    fun isBiometricReady(context: Context) =
        hasBiometricCapability(context) == BiometricManager.BIOMETRIC_SUCCESS

    //setting up a biometric
    private fun setBiometricPromptInfo(
        title: String,
        subtitle: String,
        description: String,
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setConfirmationRequired(false)
            .setNegativeButtonText("Отмена")
            .build()

    }

    /*
     * Initiate the Biometric Prompt
     */
    private fun initBiometricPrompt(
        activity: FragmentActivity,
        listener: BiometricAuthListener
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener.onBiometricAuthenticateError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                listener.onBiometricAuthenticateSuccess(result)
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    /*
     * Display the Biometric Prompt
     */
    fun showBiometricPrompt(
        title: String = "Аутентификация",
        subtitle: String = "Выполните вход, чтобы продолжить",
        description: String = "Приложите Ваш отпечаток пальца",
        activity: FragmentActivity,
        listener: BiometricAuthListener,
        cryptoObject: BiometricPrompt.CryptoObject? = null,
    ) {
        val promptInfo = setBiometricPromptInfo(
            title,
            subtitle,
            description,
        )

        val biometricPrompt = initBiometricPrompt(activity, listener)
        biometricPrompt.apply {
            if (cryptoObject == null) {
                Log.d("debugTag","BiometricUtils cryptoObject is null")
                authenticate(promptInfo)
            }
            else {
                Log.d("debugTag","BiometricUtils cryptoObject not null")
                authenticate(promptInfo, cryptoObject)
            }
        }
    }
}