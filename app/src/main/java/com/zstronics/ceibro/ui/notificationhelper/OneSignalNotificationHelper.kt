package com.zstronics.ceibro.ui.notificationhelper

import android.content.Context
import com.google.gson.Gson
import com.onesignal.OSNotification
import com.onesignal.OSNotificationReceivedEvent
import com.onesignal.OneSignal
import com.zstronics.ceibro.data.repos.NotificationTaskData
import com.zstronics.ceibro.ui.dashboard.DashboardFragment

class OneSignalNotificationHelper : OneSignal.OSRemoteNotificationReceivedHandler {
    override fun remoteNotificationReceived(
        context: Context,
        osNotificationReceivedEvent: OSNotificationReceivedEvent
    ) {

        val notification: OSNotification = osNotificationReceivedEvent.notification
        val title = notification.title
        val body = notification.body

        val additionalData = notification.additionalData
        val type = additionalData.getString("type")
        val taskObj = additionalData.getString("payload")

        val gson = Gson()
        val task = gson.fromJson(taskObj, NotificationTaskData::class.java)

        println("NotificationContent:title ${title}")
        println("NotificationContent:body ${body}")
        println("NotificationContent:task ${task}")

        val notificationHelper = NotificationHelper.getInstance(context)
        osNotificationReceivedEvent.complete(null)
        notificationHelper.createNotification(
            task,
            notificationId =
            DashboardFragment.index++,
            notificationType = type,
            title = task.topic,
            message = task.description,
            context = context
        )
    }
}