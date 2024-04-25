package com.easyflow.diarycourse.core.utils.alarm

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.domain.models.ScheduleItem

/*
* Original
* https://medium.com/@nipunvirat0/how-to-schedule-alarm-in-android-using-alarm-manager-7a1c3b23f1bb
*/
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val alarmItem = intent?.getParcelableExtra("EXTRA_MESSAGE") as? ScheduleItem
        alarmItem ?: return // Обработка случая, когда объект alarmItem равен null

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val channelId = "alarm_id"
        context?.let { ctx ->
            val notificationManager =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle("Напоминание")
                .setContentText(alarmItem.text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSound(alarmSound)
                .setAutoCancel(true)

            notificationManager.notify(1, builder.build())
        }
    }
}