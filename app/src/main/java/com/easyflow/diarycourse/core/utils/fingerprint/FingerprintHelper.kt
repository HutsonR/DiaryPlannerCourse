package com.easyflow.diarycourse.core.utils.fingerprint

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import com.easyflow.diarycourse.core.utils.SharedPreferencesHelper

class FingerprintHelper(val context: Context) : FingerprintManagerCompat.AuthenticationCallback() {

    private var mCancellationSignal: CancellationSignal? = null

    fun startAuth(cryptoObject: FingerprintManagerCompat.CryptoObject?) {
        mCancellationSignal = CancellationSignal()
        val manager = FingerprintManagerCompat.from(context)
        manager.authenticate(cryptoObject, 0, mCancellationSignal, this, null)
    }

    fun cancel() {
        mCancellationSignal?.cancel()
    }

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
        Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
        Toast.makeText(context, helpString, Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult) {
        val cipher = result.cryptoObject.cipher
        val encoded: String? =
            SharedPreferencesHelper.getTouchId(context)
        val decoded: String? = cipher?.let { CryptoUtils().decode(encoded, it) }

        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        Log.d("debugTag", "init ${bytes.joinToString { it.toString() }}")
        Log.d("debugTag", "decoded $decoded")
        Toast.makeText(context, "success", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationFailed() {
        Toast.makeText(context, "try again", Toast.LENGTH_SHORT).show()
    }
}