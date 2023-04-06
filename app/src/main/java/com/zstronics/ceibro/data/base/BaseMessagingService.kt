package com.zstronics.ceibro.data.base

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

class BaseMessagingService : FirebaseMessagingService()  {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle incoming message from OneSignal
        try {
            val keys = remoteMessage.data.keys
            val customData = remoteMessage.data["custom"]
            val customJsonObj = customData?.let { JSONObject(it) }
            val dataObj: JSONObject? = customJsonObj?.get("a") as JSONObject?
            val data = dataObj?.get("data")

            println("NotificationContent: ${remoteMessage.data}")
        }
        catch (_: Exception) {

        }

    }
}