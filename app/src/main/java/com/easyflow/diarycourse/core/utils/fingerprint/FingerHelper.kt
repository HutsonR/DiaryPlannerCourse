//package com.easyflow.diarycourse.core.utils.fingerprint
//
//import android.content.Context;
//import android.hardware.biometrics.BiometricPrompt
//import android.os.Build;
//import android.security.keystore.KeyProperties;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.annotation.RequiresApi
//import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
//import androidx.fragment.app.Fragment;
//import com.easyflow.diarycourse.core.utils.SharedPreferencesHelper
//
//import java.security.NoSuchAlgorithmException;
//
//import javax.crypto.Cipher;
//import javax.crypto.NoSuchPaddingException;
//import javax.inject.Inject;
//
//
//@RequiresApi(Build.VERSION_CODES.P)
//class FingerHelper(
//    fragment: Fragment?,
//    context: Context,
//    listener: Listener
//) : BiometricPrompt.AuthenticationCallback() {
//    private val listener: Listener
//    private val biometricPrompt: BiometricPrompt
//    private val biometricListener: BiometricPrompt.AuthenticationCallback =
//        object : BiometricPrompt.AuthenticationCallback() {
//            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                super.onAuthenticationError(errorCode, errString)
//                if (errorCode != BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS) {
//                    listener.authenticationError(errString.toString())
//                } else {
//                    listener.authenticationCanceled()
//                }
//            }
//
//            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                super.onAuthenticationSucceeded(result)
//                Log.d("debugTag", "onAuthenticationSucceeded")
//                if (result.cryptoObject != null) {
//                    Log.d(
//                        "debugTag",
//                        "onAuthenticationSucceeded result!=null " + result.cryptoObject
//                    )
//                    val cipher = result.cryptoObject.cipher
//                    val encoded: String? = SharedPreferencesHelper.getTouchId(context)
//                    var decoded: String? = ""
//                    Log.d("debugTag", "onAuthenticationSucceeded cipher " + cipher.hashCode())
//                    if (cipher != null) {
//                        decoded = CryptoUtils().decode(encoded, cipher)
//                        Log.d("debugTag", "onAuthenticationSucceeded decoded $decoded")
//                    }
//                    if (decoded != null) {
//                        listener.authenticationSucceeded(decoded)
//                    }
//                }
//            }
//
//            override fun onAuthenticationFailed() {
//                super.onAuthenticationFailed()
//                listener.authenticationFailed()
//            }
//        }
//
//    /**
//     * Constructor with parameters.
//     */
//    init {
//        this.listener = listener
//        biometricPrompt = BiometricPrompt(
//            fragment,
//            biometricListener
//        )
//    }
//
//    /**
//     * Start authentication through fingerprint sensor.
//     */
//    fun startAuth(
//        titleText: String?,
//        negativeButtonText: String?
//    ) {
//        val crypto = makeCrypto()
//        if (crypto != null) {
//            biometricPrompt.authenticate(
//                Builder()
//                    .setTitle(titleText)
//                    .setNegativeButtonText(negativeButtonText)
//                    .build(),
//                crypto
//            )
//        } else {
//            listener.authenticationFailed()
//        }
//    }
//
//    @Nullable
//    private fun makeCrypto(): BiometricPrompt.CryptoObject? {
//        val `object`: FingerprintManagerCompat.CryptoObject =
//            CryptoUtils.getCryptoObject() ?: return null
//        Log.d("debugTag", "makeCrypto object cipher")
//        val cipher = `object`.cipher ?: return null
//        return BiometricPrompt.CryptoObject(cipher)
//    }
//
//    override fun onAuthenticationFailed() {
//        listener.authenticationFailed()
//    }
//
//    interface Listener {
//        fun authenticationError(errString: String?)
//        fun authenticationSucceeded(touchId: String?)
//        fun authenticationFailed()
//        fun authenticationCanceled()
//    }
//}