package com.easyflow.diarycourse.core.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.easyflow.diarycourse.MainActivity
import com.easyflow.diarycourse.R

class NotificationHelper(val context: Context) {
    companion object {
        private const val CHANNEL_ID = "diary_reminder_channel_id"
        private const val NOTIFICATION_ID = 1
    }

//    From https://dev.to/blazebrain/building-a-reminder-app-with-local-notifications-using-workmanager-api-385f
    fun createNotification(title: String, message: String){
        // 1
        createNotificationChannel()
        // 2
        val intent = Intent(context, MainActivity:: class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // 3
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        // 4
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setSound(alarmSound)
            .setAutoCancel(true)
            .build()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        // 5
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

//    1 - Call the createNotificationChannel function created above.
//
//    2 - Create an Intent that calls the MainActivity when it runs. Adding an intent is to start up the app when the user clicks on the notification in the menu tray.
//
//    We also add flags to the intent:
//
//    Intent.FLAG_ACTIVITY_NEW_TASK will ensure the MainActivity opens up as a new Task on the history stack. It comes up as the root and appears as new in the history stack.
//    Intent.FLAG_ACTIVITY_CLEAR_TASK will cause any existing task associated with the activity to clear out before the activity starts.
//    3 - Since we are not launching the intent immediately, create a pending intent and pass the intent created to it. PendingIntent in itself is a description of an Intent and what action it’s to perform. It gives an external application(like NotificationManager in our case)access to launch tasks for us as if we were launching them now, with the same set of permissions we would use. We call getActivity, which means the PendingIntent is to start a newActivity when it launches.
//
//    4 - Next, we create the notification object by using NotificationCompat.Builder and pass in the channelID. We go-ahead to set other configurations for our notifications like smallIcon, largeIcon, message(which will come from the message parameter goes into this function), the notificationStyle(we’ll be using the BigPictureStyle), we also pass in the icon we created in step 4. We also set the contentIntent(the pendingIntent we created in step 3) and then the notificationPriority(pass it as default). Lastly, we call build to add all these configurations as part of the object.
//
//    5 - We create the notification using the NotifcationManagerCompat, passing in the NotificationID and the notification Object we created in Step 5.
//
//    With this, our NotificationHelper class is good to go, and we can start triggering “instant” notifications by simply calling the createNotifcation function, passing in the title and description, and getting a notification.
//
//    However, this does not fulfill our aim as we want the notifications to pop up at a time we set, not instantly, i.e. we want a delayed notification. This is where WorkManager API comes in; using this API; we would be able to create a notification that would come up when we want.

    private fun createNotificationChannel(){
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH ).apply {
            description = "Reminder Channel Description"
        }
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lightColor = Color.GREEN
        channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        val notificationManager =  context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}