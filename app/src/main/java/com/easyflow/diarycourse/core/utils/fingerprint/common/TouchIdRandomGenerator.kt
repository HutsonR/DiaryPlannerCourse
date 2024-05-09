package com.easyflow.diarycourse.core.utils.fingerprint.common

import android.util.Base64
import java.security.SecureRandom

class TouchIdRandomGenerator {

    private val generator =
        SecureRandom.getInstanceStrong()

    fun generateTouchId(): TouchId {
        val bytes = ByteArray(TOUCH_ID_LENGTH)
        generator.nextBytes(bytes)
        val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return TouchId(bytes, base64String)
    }

    data class TouchId(
        val bytes: ByteArray,
        val base64String: String
    )

    companion object {
        private const val TOUCH_ID_LENGTH = 128
    }

}