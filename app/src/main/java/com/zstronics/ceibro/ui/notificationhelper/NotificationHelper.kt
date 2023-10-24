package com.zstronics.ceibro.ui.notificationhelper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.zstronics.ceibro.R

class NotificationHelper(context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var summaryNotification1: Notification? = null
    private var summaryNotificationId = 100

    init {
        createNotificationChannel(context)

    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                "my_notification_channel",
                "My Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "My custom notification channel"
            channel.enableLights(true)
            channel.lightColor = Color.RED
            notificationManager.createNotificationChannel(channel)



            summaryNotification1 = NotificationCompat.Builder(context, CHANNEL_ID_1)
                .setContentTitle("Task")
                .setContentText("new Events")
                .setSmallIcon(R.drawable.app_logo)
//                .setStyle(NotificationCompat.InboxStyle().setSummaryText("Tasks"))
                .setGroup(groupKey)
                .setGroupSummary(true)
                .build()
        }
    }

    fun createNotification(
        notificationId: Int,
        moduleName: String,
        title: String,
        message: String,
        context: Context
    ) {

//        summaryNotificationId++
        val intent = Intent(context, context::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val bigTextStyle = NotificationCompat.BigTextStyle().setBigContentTitle(title)
            .bigText(message)

        val blueColor = ContextCompat.getColor(context, R.color.appBlue)
        val whiteColor = ContextCompat.getColor(context, R.color.white)

        val replyActionText = "Reply"
        val forwardActionText = "Forward"
        val openActionText = "Open"

        val replyActionTextBlue = SpannableString(replyActionText)
        replyActionTextBlue.setSpan(
            ForegroundColorSpan(blueColor), // Set the text color to blue
            0, replyActionTextBlue.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val forwardActionTextBlue = SpannableString(forwardActionText)
        forwardActionTextBlue.setSpan(
            ForegroundColorSpan(blueColor), // Set the text color to blue
            0, forwardActionTextBlue.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val openActionTextBlue = SpannableString(openActionText)
        openActionTextBlue.setSpan(
            ForegroundColorSpan(blueColor), // Set the text color to blue
            0, openActionTextBlue.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val replyIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val forwardIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val openIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(context, CHANNEL_ID_1)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setGroup(groupKey)
            .setStyle(bigTextStyle)
            .setContentIntent(replyIntent)
            .addAction(
                R.drawable.app_logo, replyActionTextBlue, replyIntent
            ).addAction(
                R.drawable.app_logo, forwardActionTextBlue, forwardIntent
            ).addAction(
                R.drawable.app_logo, openActionTextBlue, openIntent
            )
            .build()// Set this to true for the group summary notification
//        notificationManager.notify(notificationId, notification)
//        notificationManager.notify(summaryNotificationId, summaryNotification1)
        notificationManager.apply {
            notify(notificationId, notification)
            notify(summaryNotificationId, summaryNotification1)
        }
    }

    companion object {
        @Volatile
        private var instance: NotificationHelper? = null
        const val groupKey = "my_notification_group"
        val CHANNEL_ID_1 = "my_notification_channel"

        fun getInstance(context: Context): NotificationHelper {
            return instance ?: synchronized(this) {
                instance ?: NotificationHelper(context).also { instance = it }
            }
        }
    }


}
