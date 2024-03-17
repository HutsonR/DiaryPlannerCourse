package com.easyflow.diarycourse.core.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlin.random.Random

class ReminderWorker(val context: Context, params: WorkerParameters) : Worker(context, params){
    override fun doWork(): Result {
        val title = inputData.getString("title")
        val message = inputData.getString("message")

        NotificationHelper(context).createNotification(
            title.toString(),
            message.toString())

        return Result.success()
    }
}