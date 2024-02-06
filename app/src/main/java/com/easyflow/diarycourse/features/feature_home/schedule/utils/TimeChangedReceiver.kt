package com.easyflow.diarycourse.features.feature_home.schedule.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class TimeChangedReceiver(private val listener: TimeChangedListener) : BroadcastReceiver() {

    interface TimeChangedListener {
        fun onTimeChanged()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIME_TICK) {
            listener.onTimeChanged()
        }
    }

    fun register(context: Context) {
        val filter = IntentFilter(Intent.ACTION_TIME_TICK)
        context.registerReceiver(this, filter)
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }
}