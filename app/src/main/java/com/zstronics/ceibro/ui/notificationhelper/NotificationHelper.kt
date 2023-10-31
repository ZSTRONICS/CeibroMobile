package com.zstronics.ceibro.ui.notificationhelper

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
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
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.zstronics.ceibro.NotificationActivity
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.BUNDLE_EXTRA
import com.zstronics.ceibro.base.TYPE_EXTRA
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.data.repos.NotificationTaskData
import com.zstronics.ceibro.ui.splash.SplashActivity


class NotificationHelper(context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var summaryNotification1: Notification? = null
    private var summaryNotificationId = 100
    private var singleNotificationId = 0

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
        singleNotificationId = System.currentTimeMillis().toInt()
        if (notificationType.equals("newTask", true)) {

            val replyIntent = cretePendingIntentForReply(context, task)
            val forwardIntent = cretePendingIntentForForward(context, task)
            val openIntent = cretePendingIntentForOpen(context, task)

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

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                val customNotificationLayout =
                    RemoteViews(context.packageName, R.layout.custom_notification_view)
                customNotificationLayout.setTextViewText(R.id.firstLine, task.creator)
                customNotificationLayout.setTextViewText(R.id.secondLine, title)
                customNotificationLayout.setTextViewText(R.id.largeText, message)


                val notification = NotificationCompat.Builder(context, CHANNEL_ID_1)
                    .setSmallIcon(R.drawable.app_logo)
                    .setContentTitle(task.creator)
                    .setContentText(title)
                    .setLargeIcon(decodeBase64ToBitmap("UklGRiADAABXRUJQVlA4WAoAAAAQAAAAMQAAMQAAQUxQSEsBAAABkELbtrFtz2fbtm3bNrKtaiWbzd9vq9q2bVuvPp/7vTv+KSImgPufUNPQ2gpKMXrw8oMXj2o8UjRRHHf9XFrL//yrRQFDft8SkV+PkfSNsnTfEsHs4hK9ASFfxHBNDyBXwPA3GSCJx7DUCRD1h2UKIOA7SwWA7zeGL94AqTyGb74AuUKGn8EAYT8ZXtsBmD9lOKoM4POJYVYCwP8bwxQHaLJfSGtCkI15QDofisBpNQkIL8M5TPcPhF1SICrnCT0cppz3ZcJuGynxSVoV7X0nIgifz+UYiMcge/qJaImVd2sgSn2zNGIGb/GWNvfn6SYveTYFv9azv5bE+fFQqbUUTW7865LYRc9LaUaPlhD3SJM8P0Nc0SQl8yDeOJAqliD/xpG6MJaqKJJbQYYpyidAjsgRjB6A3NIjuH0E+ehOSPgHwksnFCyh1q0BAFZQOCCuAQAAkAoAnQEqMgAyAD6dSp1MJaQioiQYC2iwE4llAM2lg7gAlgFe3YWNQJUD3J7hb5Wd7qTnVO24IDSFKQRpKIXRHvDIvOBFanAloNQeQahgA3aGS7mPfEUUf5EHow4AAP78XNrKpbaALBfTbfZnCaeBdI3QPzV72GcOsA7Z5bZO7qaiuo9XyM8JA8/RJ9X/13Fi5ZfYJneUmZks0WbstacfbSIypOflQ+AfxZnQOZZaReeZtGLQywHbbGwAaCBjtrDixBWo0y4AFTT6feE30ZbEOySXTsfraQsba09/09vX5FX419TP0Vw0NdcZaC9m4OccuRrXG+quzIbdsPk6BHWpbJWsXp5gk7DHomPqvzgpaGYhDvi14WkdEInuGo4qKW4sR9yQfT8f4PuxuCz9TVutEbliZ2fPbEmnTg+CCBLkiagLPpRXWYAWxV7HJzrP67VYukn+6Rs9diLi82otLArOxZHZmQMesLHp2a8E49RLBN55dmhKu6txxUFoADM4eFZ5nHT4AOim0E+7SsSxnKrgrxC+xAjD8JAmWAV1RZQl2tbjSoJoy3Ei5+1Z+EAAAA=="))
                    .setCustomBigContentView(customNotificationLayout)
                    .setCustomContentView(customNotificationLayout)
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // Enable expanded view
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setGroup(groupKey)
                    .addAction(
                        R.drawable.app_logo, replyActionTextBlue, replyIntent
                    ).addAction(
                        R.drawable.app_logo, forwardActionTextBlue, forwardIntent
                    ).addAction(
                        R.drawable.app_logo, openActionTextBlue, openIntent
                    )
                    .build()

                notificationManager.apply {
                    notify(singleNotificationId, notification)
                    notify(summaryNotificationId, summaryNotification1)
                }
            }, 120)
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

    private fun decodeBase64ToBitmap(encodedString: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isAppOnForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    private fun cretePendingIntentForReply(
        context: Context,
        taskData: NotificationTaskData
    ): PendingIntent? {
        val requestCode = System.currentTimeMillis().toInt() + 1
        val intentReply = Intent(context, NotificationActivity::class.java)

        val bundle = Bundle()
        bundle.putParcelable("notificationTaskData", taskData)
        bundle.putInt(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
        bundle.putInt(NAVIGATION_Graph_START_DESTINATION_ID, R.id.commentFragment)
        bundle.putInt("notificationId", singleNotificationId)

        intentReply.action = "ACTION_CLOSE_NOTIFICATION"
        intentReply.putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
        intentReply.putExtra(NAVIGATION_Graph_START_DESTINATION_ID, R.id.commentFragment)
        intentReply.putExtra("notificationTaskData", taskData)
        intentReply.putExtra(TYPE_EXTRA, 1)
        intentReply.putExtra("notificationId", singleNotificationId)
        intentReply.putExtra(BUNDLE_EXTRA, bundle)

        return PendingIntent.getActivity(
            context, requestCode, intentReply, PendingIntent.FLAG_IMMUTABLE
        )
    }


    private fun cretePendingIntentForForward(
        context: Context,
        taskData: NotificationTaskData
    ): PendingIntent? {
        val requestCode = System.currentTimeMillis().toInt() + 2
        val intentReply =  Intent(context, NotificationActivity::class.java)

        val bundle = Bundle()
        bundle.putParcelable("notificationTaskData", taskData)
        bundle.putInt(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
        bundle.putInt(NAVIGATION_Graph_START_DESTINATION_ID, R.id.forwardTaskFragment)
        bundle.putInt("notificationId", singleNotificationId)

        intentReply.putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
        intentReply.putExtra(NAVIGATION_Graph_START_DESTINATION_ID, R.id.forwardTaskFragment)
        intentReply.putExtra("notificationTaskData", taskData)
        intentReply.putExtra(TYPE_EXTRA, 2)
        intentReply.putExtra("notificationId", singleNotificationId)
        intentReply.putExtra(BUNDLE_EXTRA, bundle)
        return PendingIntent.getActivity(
            context, requestCode, intentReply, PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun cretePendingIntentForOpen(
        context: Context,
        taskData: NotificationTaskData
    ): PendingIntent? {
        val requestCode = System.currentTimeMillis().toInt() + 3
        val intentReply =  Intent(context, NotificationActivity::class.java)

        val bundle = Bundle()
        bundle.putParcelable("notificationTaskData", taskData)
        bundle.putInt(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
        bundle.putInt(NAVIGATION_Graph_START_DESTINATION_ID, R.id.taskDetailV2Fragment)
        bundle.putInt("notificationId", singleNotificationId)

        intentReply.putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
        intentReply.putExtra(NAVIGATION_Graph_START_DESTINATION_ID, R.id.taskDetailV2Fragment)
        intentReply.putExtra("notificationTaskData", taskData)
        intentReply.putExtra(TYPE_EXTRA, 3)
        intentReply.putExtra("notificationId", singleNotificationId)
        intentReply.putExtra(BUNDLE_EXTRA, bundle)
        return PendingIntent.getActivity(
            context, requestCode, intentReply, PendingIntent.FLAG_IMMUTABLE
        )
    }
}
