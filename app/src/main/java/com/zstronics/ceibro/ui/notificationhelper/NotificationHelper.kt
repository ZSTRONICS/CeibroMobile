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
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.NotificationActivity
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.BUNDLE_EXTRA
import com.zstronics.ceibro.base.TYPE_EXTRA
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.NotificationTaskData
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.data.sessions.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID


class NotificationHelper(context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var summaryNotification1: Notification? = null
    private var summaryNotificationId = 100
    private var singleNotificationId = 0
    var sessionManager: SessionManager? = null

    init {
        sessionManager = getSessionManager(SharedPreferenceManager(context))
        createNotificationChannel(context)
    }

    private fun getSessionManager(
        sharedPreferenceManager: SharedPreferenceManager
    ) = SessionManager(sharedPreferenceManager)

    private fun createNotificationChannel(context: Context) {

        val channel = NotificationChannel(
            CHANNEL_ID_1,
            "Ceibro Channel",
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
    suspend fun createNotification(
        task: NotificationTaskData,
        notificationType: String,
        title: String,
        message: String,
        context: Context
    ) {
        println("TaskData:commentNewData -> ${task}")
        var loggedInUserId = ""
        if (sessionManager != null) {
            if (sessionManager!!.getUser().value?.id.isNullOrEmpty()) {
                sessionManager!!.setUser()
                loggedInUserId = sessionManager!!.getUserId()
            } else {
                loggedInUserId = sessionManager!!.getUser().value?.id.toString()
            }
        } else {
            sessionManager = getSessionManager(SharedPreferenceManager(context))
            sessionManager!!.setUser()
            loggedInUserId = sessionManager!!.getUserId()
        }
        println("NotificationHelper-SessionManager:: loggedInUserId = ${loggedInUserId}")

        if (task.userId == loggedInUserId) {

            singleNotificationId = generateUniqueKey()
            if (notificationType.equals("newTask", true)) {

                var replyIntent: PendingIntent? = null
                var forwardIntent: PendingIntent? = null
                var openIntent: PendingIntent? = null
                GlobalScope.launch(Dispatchers.Default) {
                    replyIntent = createPendingIntentForReply(context, task)
                    forwardIntent = createPendingIntentForForward(context, task)
                    openIntent = createPendingIntentForOpen(context, task)
                }.join()

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
                    val collapsedView =
                        RemoteViews(context.packageName, R.layout.custom_notification_view)
                    val expandedView =
                        RemoteViews(context.packageName, R.layout.custom_notification_view)

                    collapsedView.setTextViewText(R.id.firstLine, task.creator)
                    collapsedView.setTextViewText(R.id.secondLine, title)
                    collapsedView.setViewVisibility(R.id.largeText, View.GONE)

                    expandedView.setTextViewText(R.id.firstLine, task.creator)
                    expandedView.setTextViewText(R.id.secondLine, title)
                    expandedView.setTextViewText(R.id.largeText, message)
                    expandedView.setViewVisibility(R.id.largeText, View.VISIBLE)

                    val notification = NotificationCompat.Builder(context, CHANNEL_ID_1)
                        .setSmallIcon(R.drawable.app_logo)
                        .setContentTitle(task.creator)
                        .setContentText(title)
                        .setLargeIcon(decodeBase64ToBitmap(task.avatar))
                        .setCustomBigContentView(expandedView)
                        .setCustomContentView(collapsedView)
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
                        .setContentIntent(openIntent) // Set the pending intent for the whole notification
                        .build()

                    notificationManager.apply {
                        notify(summaryNotificationId, summaryNotification1)
                        notify(singleNotificationId, notification)
                    }
                }, 160)

            } else if (notificationType.equals("comment", true)) {

                val currentFragmentID = CeibroApplication.CookiesManager.navigationGraphStartDestination
                if (currentFragmentID == R.id.taskDetailTabV2Fragment) {
                    val detailViewTask: CeibroTaskV2? = CeibroApplication.CookiesManager.taskDataForDetails
                    if (task.taskId == detailViewTask?.id) {
                        //Do nothing, because user is already in detail view of same task whose comment notification has came
                        //println("NotificationActivityFragmentCheck = Detail View And Same task")
                    } else {
                        //Detail View opened but with Different task, so show notification
                        //println("NotificationActivityFragmentCheck = Detail View But other task")
                        var replyIntent: PendingIntent? = null
                        var openIntent: PendingIntent? = null
                        GlobalScope.launch(Dispatchers.Default) {
                            replyIntent = createPendingIntentForReply(context, task)
                            openIntent = createPendingIntentForOpen(context, task)
                        }.join()

                        val blueColor = ContextCompat.getColor(context, R.color.appBlue)
                        ContextCompat.getColor(context, R.color.white)

                        val replyActionText = "Reply"
                        val openActionText = "Open"

                        val replyActionTextBlue = SpannableString(replyActionText)
                        replyActionTextBlue.setSpan(
                            ForegroundColorSpan(blueColor),
                            0, replyActionTextBlue.length,
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
                            val collapsedView =
                                RemoteViews(context.packageName, R.layout.custom_notification_view)
                            val expandedView =
                                RemoteViews(context.packageName, R.layout.custom_notification_view)

                            collapsedView.setTextViewText(R.id.firstLine, task.creator)
                            collapsedView.setTextViewText(R.id.secondLine, title)
                            collapsedView.setViewVisibility(R.id.largeText, View.GONE)

                            expandedView.setTextViewText(R.id.firstLine, task.creator)
                            expandedView.setTextViewText(R.id.secondLine, title)
                            expandedView.setTextViewText(R.id.largeText, message)
                            expandedView.setViewVisibility(R.id.largeText, View.VISIBLE)

                            val notification = NotificationCompat.Builder(context, CHANNEL_ID_1)
                                .setSmallIcon(R.drawable.app_logo)
                                .setContentTitle(task.creator)
                                .setContentText(title)
                                .setLargeIcon(decodeBase64ToBitmap(task.avatar))
                                .setCustomBigContentView(expandedView)
                                .setCustomContentView(collapsedView)
                                .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // Enable expanded view
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setGroup(groupKey)
                                .addAction(
                                    R.drawable.app_logo, replyActionTextBlue, replyIntent
                                ).addAction(
                                    R.drawable.app_logo, openActionTextBlue, openIntent
                                )
                                .setContentIntent(openIntent) // Set the pending intent for the whole notification
                                .build()

                            notificationManager.apply {
                                notify(summaryNotificationId, summaryNotification1)
                                notify(singleNotificationId, notification)
                            }
                        }, 160)

                    }
                } else {
                    //println("NotificationActivityFragmentCheck = Not detail View")
                    var replyIntent: PendingIntent? = null
                    var openIntent: PendingIntent? = null
                    GlobalScope.launch(Dispatchers.Default) {
                        replyIntent = createPendingIntentForReply(context, task)
                        openIntent = createPendingIntentForOpen(context, task)
                    }.join()

                    val blueColor = ContextCompat.getColor(context, R.color.appBlue)
                    ContextCompat.getColor(context, R.color.white)

                    val replyActionText = "Reply"
                    val openActionText = "Open"

                    val replyActionTextBlue = SpannableString(replyActionText)
                    replyActionTextBlue.setSpan(
                        ForegroundColorSpan(blueColor),
                        0, replyActionTextBlue.length,
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
                        val collapsedView =
                            RemoteViews(context.packageName, R.layout.custom_notification_view)
                        val expandedView =
                            RemoteViews(context.packageName, R.layout.custom_notification_view)

                        collapsedView.setTextViewText(R.id.firstLine, task.creator)
                        collapsedView.setTextViewText(R.id.secondLine, title)
                        collapsedView.setViewVisibility(R.id.largeText, View.GONE)

                        expandedView.setTextViewText(R.id.firstLine, task.creator)
                        expandedView.setTextViewText(R.id.secondLine, title)
                        expandedView.setTextViewText(R.id.largeText, message)
                        expandedView.setViewVisibility(R.id.largeText, View.VISIBLE)

                        val notification = NotificationCompat.Builder(context, CHANNEL_ID_1)
                            .setSmallIcon(R.drawable.app_logo)
                            .setContentTitle(task.creator)
                            .setContentText(title)
                            .setLargeIcon(decodeBase64ToBitmap(task.avatar))
                            .setCustomBigContentView(expandedView)
                            .setCustomContentView(collapsedView)
                            .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // Enable expanded view
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setGroup(groupKey)
                            .addAction(
                                R.drawable.app_logo, replyActionTextBlue, replyIntent
                            ).addAction(
                                R.drawable.app_logo, openActionTextBlue, openIntent
                            )
                            .setContentIntent(openIntent) // Set the pending intent for the whole notification
                            .build()

                        notificationManager.apply {
                            notify(summaryNotificationId, summaryNotification1)
                            notify(singleNotificationId, notification)
                        }
                    }, 160)
                }

            }
        }
    }

    companion object {
        @Volatile
        private var instance: NotificationHelper? = null
        const val groupKey = "my_notification_group"
        const val CHANNEL_ID_1 = "my_notification_channel"
        var requestCodeCounter = 0

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

    private fun createPendingIntentForReply(
        context: Context,
        taskData: NotificationTaskData
    ): PendingIntent? {
        val requestCode = generateUniqueKey()
        val intentReply = Intent(context, NotificationActivity::class.java)

        val bundle = Bundle()
//        bundle.putParcelable("notificationTaskData", taskData)
//        bundle.putInt(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
//        bundle.putInt(NAVIGATION_Graph_START_DESTINATION_ID, R.id.commentFragment)
//        bundle.putInt("notificationId", singleNotificationId)

        intentReply.action = "ACTION_CLOSE_NOTIFICATION"
        intentReply.putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
//        intentReply.putExtra(NAVIGATION_Graph_START_DESTINATION_ID, R.id.commentFragment)
        intentReply.putExtra(NAVIGATION_Graph_START_DESTINATION_ID, R.id.taskDetailTabV2Fragment)
        intentReply.putExtra("notificationTaskData", taskData)
        intentReply.putExtra(TYPE_EXTRA, 1)
        intentReply.putExtra("notificationId", singleNotificationId)
        intentReply.putExtra(BUNDLE_EXTRA, bundle)

        return PendingIntent.getActivity(
            context, requestCode, intentReply, PendingIntent.FLAG_IMMUTABLE
        )
    }


    private fun createPendingIntentForForward(
        context: Context,
        taskData: NotificationTaskData
    ): PendingIntent? {
        val requestCode = generateUniqueKey()
        val intentReply = Intent(context, NotificationActivity::class.java)

        val bundle = Bundle()
//        bundle.putParcelable("notificationTaskData", taskData)
//        bundle.putInt(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
//        bundle.putInt(NAVIGATION_Graph_START_DESTINATION_ID, R.id.forwardTaskFragment)
//        bundle.putInt("notificationId", singleNotificationId)

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

    private fun createPendingIntentForOpen(
        context: Context,
        taskData: NotificationTaskData
    ): PendingIntent? {
        val requestCode = generateUniqueKey()
        val intentReply = Intent(context, NotificationActivity::class.java)

        val bundle = Bundle()
//        bundle.putParcelable("notificationTaskData", taskData)
//        bundle.putInt(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
//        bundle.putInt(NAVIGATION_Graph_START_DESTINATION_ID, R.id.taskDetailTabV2Fragment)
//        bundle.putInt("notificationId", singleNotificationId)

        intentReply.putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
        intentReply.putExtra(NAVIGATION_Graph_START_DESTINATION_ID, R.id.taskDetailTabV2Fragment)
        intentReply.putExtra("notificationTaskData", taskData)
        intentReply.putExtra(TYPE_EXTRA, 3)
        intentReply.putExtra("notificationId", singleNotificationId)
        intentReply.putExtra(BUNDLE_EXTRA, bundle)
        return PendingIntent.getActivity(
            context, requestCode, intentReply, PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun generateUniqueKey(): Int {
        val uniqueId = UUID.randomUUID().toString()
        val uniqueCode = System.currentTimeMillis().toInt() + uniqueId.hashCode() + requestCodeCounter++
//        println("NotificationActivityFragmentCheck = uniqueCode ${uniqueCode}")
        return uniqueCode
    }
}
