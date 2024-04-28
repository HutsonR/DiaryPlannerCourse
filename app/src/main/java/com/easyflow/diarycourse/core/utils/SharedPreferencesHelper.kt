package com.easyflow.diarycourse.core.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesHelper {
    private const val PREFS_NAME = "app_prefs"
    private const val PREF_TOUCH_ID = "fingerprintTouchId"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setTouchId(context: Context, id: String) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putString(PREF_TOUCH_ID, id).apply()
    }

    fun getTouchId(context: Context): String? {
        val prefs = getSharedPreferences(context)
        val savedTouchId = prefs.getString(PREF_TOUCH_ID, "")
        return if (savedTouchId.isNullOrEmpty()) null else savedTouchId
    }

}