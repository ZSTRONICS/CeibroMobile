package com.zstronics.ceibro.ui.notificationhelper

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.zstronics.Notifyme
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.EXTRA
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.data.repos.NotificationTaskData


class NotificationHelper(context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var summaryNotification1: Notification? = null
    private var summaryNotificationId = 100

    init {
        createNotificationChannel(context)

    }

    private fun createNotificationChannel(context: Context) {

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

    @SuppressLint("RemoteViewLayout")
    fun createNotification(
        task: NotificationTaskData,
        notificationId: Int,
        notificationType: String,
        title: String,
        message: String,
        context: Context
    ) {

        if (notificationType.equals("newTask", true)) {

            val bundle = Bundle()
            bundle.putInt(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
            bundle.putInt(NAVIGATION_Graph_START_DESTINATION_ID, R.id.commentFragment)


            val intent = Intent(context, Notifyme::class.java)

            intent.putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
            intent.putExtra(NAVIGATION_Graph_START_DESTINATION_ID, R.id.commentFragment)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) // Add these flags to clear the existing activity
            intent.putExtra(EXTRA, bundle)


            /*val pendingIntent = NavDeepLinkBuilder(context)
                .setComponentName(NavHostPresenterActivity::class.java)
                .setGraph(R.navigation.home_nav_graph)
                .setDestination(R.id.commentFragment)
                .setArguments(bundle)
                .createPendingIntent()*/

            /* val pendingIntent = PendingIntent.getActivity(
                 context, 0, intent,
                 PendingIntent.FLAG_IMMUTABLE
             )*/

            val bigTextStyle = NotificationCompat.BigTextStyle().setBigContentTitle(title)
                .bigText(message)

            val blueColor = ContextCompat.getColor(context, R.color.appBlue)
            ContextCompat.getColor(context, R.color.white)

            val replyActionText = "Reply"
            val forwardActionText = "Forward"
            val openActionText = "Open"

            val replyActionTextBlue = SpannableString(replyActionText)
            replyActionTextBlue.setSpan(
                ForegroundColorSpan(blueColor),
                0, replyActionTextBlue.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val forwardActionTextBlue = SpannableString(forwardActionText)
            forwardActionTextBlue.setSpan(
                ForegroundColorSpan(blueColor),
                0, forwardActionTextBlue.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val openActionTextBlue = SpannableString(openActionText)
            openActionTextBlue.setSpan(
                ForegroundColorSpan(blueColor),
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
            val titleWithCreatorName = task.creator + "\n" + title
            val creatorName = task.creator

            val customNotificationLayout =
                RemoteViews(context.packageName, R.layout.custom_notification_view)
            customNotificationLayout.setTextViewText(R.id.firstLine, creatorName)
            customNotificationLayout.setTextViewText(R.id.secondLine, title)
            customNotificationLayout.setTextViewText(R.id.largeText, message)


            val notification = NotificationCompat.Builder(context, CHANNEL_ID_1)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(creatorName)
                .setContentText(title)
                .setLargeIcon(decodeBase64ToBitmap(task.avatar))
                .setCustomBigContentView(customNotificationLayout)
                .setCustomContentView(customNotificationLayout)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // Enable expanded view
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(groupKey)
//                .setStyle(NotificationCompat.InboxStyle().setSummaryText(creatorName))
//                .setStyle(bigTextStyle)
                .addAction(
                    R.drawable.app_logo, replyActionTextBlue, replyIntent
                ).addAction(
                    R.drawable.app_logo, forwardActionTextBlue, forwardIntent
                ).addAction(
                    R.drawable.app_logo, openActionTextBlue, openIntent
                )
                .build()
            notificationManager.apply {
                notify(notificationId, notification)
                notify(summaryNotificationId, summaryNotification1)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: NotificationHelper? = null
        const val groupKey = "my_notification_group"
        const val CHANNEL_ID_1 = "my_notification_channel"

        fun getInstance(context: Context): NotificationHelper {
            return instance ?: synchronized(this) {
                instance ?: NotificationHelper(context).also { instance = it }
            }
        }
    }

    fun decodeBase64ToBitmap(encodedString: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
