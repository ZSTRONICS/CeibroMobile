package com.zstronics.ceibro.data.base

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.zstronics.ceibro.base.KEY_Firebase_Token
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.data.sessions.SharedPreferenceManager
import org.json.JSONObject

class BaseMessagingService : FirebaseMessagingService()  {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val sessionManager = getSessionManager(SharedPreferenceManager(this))
        println("FirebaseToken-Refreshed token: $token")
        sessionManager.saveStringValue(KEY_Firebase_Token, token)
    }

    private fun getSessionManager(
        sharedPreferenceManager: SharedPreferenceManager
    ) = SessionManager(sharedPreferenceManager)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle incoming message from OneSignal
        println("FirebaseToken-MessageReceive: TITLE: ${remoteMessage.notification?.title}   BODY: ${remoteMessage.notification?.body}")
        try {
            val keys = remoteMessage.data.keys
//            val customData = remoteMessage.data["custom"]
//            val alert = remoteMessage.data["alert"]
//            val title = remoteMessage.data["title"]
//            val customJsonObj = customData?.let { JSONObject(it) }
//            val dataObj: JSONObject = customJsonObj?.get("a") as JSONObject
//            val type = dataObj.get("type") as String
//            val taskData = dataObj.get("payload") as String
//
//            val gson = Gson()
//            val task = gson.fromJson(taskData, CeibroTaskV2::class.java)

//            val notificationTitle: String =
//                if (task.topic?.topic.isNullOrEmpty())
//                    "" else task.topic?.topic.toString()
//
//            val notificationHelper = NotificationHelper.getInstance(applicationContext)
//            notificationHelper.createNotification(
//                notificationId = DashboardFragment.index++,
//                moduleName = type,
//                title = notificationTitle,
//                message = task.description,
//                context = applicationContext
//            )

            println("FirebaseToken-NotificationContent: data: ${remoteMessage.data}")
        }
        catch (_: Exception) {

        }

    }
}