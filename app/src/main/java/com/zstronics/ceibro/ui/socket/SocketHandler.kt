package com.zstronics.ceibro.ui.socket

import com.zstronics.ceibro.BuildConfig
import com.zstronics.ceibro.data.base.CookiesManager
import io.socket.client.IO
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import java.net.URISyntaxException

object SocketHandler {

    private var mSocket: Socket? = null

    const val SEND_MESSAGE = "SEND_MESSAGE"
    const val TYPING_START = "TYPING_START"
    const val RECEIVE_MESSAGE = "RECEIVE_MESSAGE"
    const val CHAT_EVENT_REP_OVER_SOCKET = "CHAT_EVENT_REP_OVER_SOCKET"
    const val CHAT_EVENT_REQ_OVER_SOCKET = "CHAT_EVENT_REQ_OVER_SOCKET"
    const val CEIBRO_LIVE_EVENT_BY_USER = "CEIBRO_LIVE_EVENT_BY_USER"
    const val CEIBRO_LIVE_EVENT_BY_SERVER = "CEIBRO_LIVE_EVENT_BY_SERVER"
    var hbCounter = 0
    var handler = android.os.Handler()
    var delayMillis: Long = 2000 // 2 seconds
    var runnable = object : Runnable {
        override fun run() {
            sendHeartbeat()
            handler.postDelayed(this, delayMillis)
        }
    }

    enum class TaskEvent {
        TASK_CREATED, TASK_UPDATE_PUBLIC, TASK_UPDATE_PRIVATE, SUB_TASK_CREATED, SUB_TASK_UPDATE_PUBLIC, SUB_TASK_UPDATE_PRIVATE,
        TASK_SUBTASK_UPDATED, COMMENT_WITH_FILES, SUBTASK_NEW_COMMENT, TASK_FORWARDED, TASK_SEEN, NEW_TASK_COMMENT, TASK_DONE, CANCELED_TASK,
        TASK_HIDDEN, TASK_SHOWN
    }

    enum class ProjectEvent {
        PROJECT_CREATED, PROJECT_UPDATED, REFRESH_PROJECTS, ROLE_CREATED, ROLE_UPDATED, REFRESH_ROLES,
        PROJECT_GROUP_CREATED, PROJECT_GROUP_UPDATED, REFRESH_PROJECT_GROUP, PROJECT_MEMBERS_ADDED, PROJECT_MEMBERS_UPDATED, REFRESH_PROJECT_MEMBERS,
        REFRESH_ROOT_DOCUMENTS, REFRESH_FOLDER
    }

    enum class UserEvent {
        USER_INFO_UPDATED, REFRESH_ALL_USERS, REFRESH_CONNECTIONS, REFRESH_INVITATIONS
    }

    enum class FileAttachmentEvents {
        FILE_UPLOAD_PROGRESS, FILE_UPLOADED, FILES_UPLOAD_COMPLETED
    }

    @Synchronized
    fun setSocket() {
        try {
            val options = IO.Options()

            options.forceNew = true
//            options.transports = arrayOf( Polling.NAME )
            options.reconnectionAttempts = Integer.MAX_VALUE
            options.timeout = 10000
            options.auth = mapOf("token" to CookiesManager.jwtToken) // Use auth instead of query

            mSocket = IO.socket(BuildConfig.SOCKET_URL, options)

            mSocket?.on(
                Socket.EVENT_DISCONNECT
            ) {
                println("Heartbeat, Socket Disconnected")
                handler.removeCallbacks(runnable)
            }

            mSocket?.on(
                Socket.EVENT_CONNECT_ERROR
            ) {
//                println("Heartbeat, Socket EVENT_CONNECT_ERROR")
                handler.removeCallbacks(runnable)
                hbCounter = 0
                handler.postDelayed(runnable, delayMillis)
            }

            mSocket?.on(
                Socket.EVENT_CONNECT
            ) {
                handler.removeCallbacks(runnable)
                hbCounter = 0
                handler.postDelayed(runnable, delayMillis)
                println("Heartbeat, Socket on Connect")

            }

            mSocket?.on(
                "heartbeatAck"
            ) {
                hbCounter -= 1
            }

            EventBus.getDefault().post(LocalEvents.InitSocketEventCallBack())

        } catch (exception: URISyntaxException) {
            exception.message
        }
    }

    fun sendHeartbeat() {
        if (mSocket != null) {
            if (mSocket?.connected() == true) {
                mSocket?.emit("heartbeat")
                hbCounter += 1
//                println("Heartbeat Sent $hbCounter")
                if (hbCounter == 6) {
                    // reconnect logic here
                    reconnectSocket()
                }
                if (hbCounter > 5) {
                    hbCounter = 0
                }
            } else {
//                println("Heartbeat Socket not connected")
                handler.removeCallbacks(runnable)
                reconnectSocket()
            }
        }
//        else {
//            println("Heartbeat Socket is null")
//            handler.removeCallbacks(runnable)
//            reconnectSocket()
//        }
    }

    @Synchronized
    fun reconnectSocket() {
        disconnectSocket()
        setSocket()
//        println("Heartbeat, Socket Re-Connecting...")
        establishConnection()
    }

    @Synchronized
    fun getSocket(): Socket? {
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket?.connect()
//        println("Heartbeat Socket connecting...")
    }

    @Synchronized
    fun disconnectSocket() {
//        println("Disconnecting Socket")
        mSocket?.disconnect()

    }

    @Synchronized
    fun closeConnectionAndRemoveObservers() {
        offAllEventOObservers()
        mSocket?.disconnect()
    }

    @Synchronized
    fun offAllEventOObservers() {
        mSocket?.io()?.off(CHAT_EVENT_REP_OVER_SOCKET)
        mSocket?.io()?.off(CHAT_EVENT_REQ_OVER_SOCKET)
        mSocket?.io()?.off(CEIBRO_LIVE_EVENT_BY_USER)
        mSocket?.io()?.off(CEIBRO_LIVE_EVENT_BY_SERVER)
        mSocket?.io()?.off("heartbeat")
        mSocket?.io()?.off("heartbeatAck")
        handler.removeCallbacks(runnable)
//        EventBus.getDefault().unregister(this)
    }

    @Synchronized
    fun sendChatRequest(body: String) {
        mSocket?.emit(CHAT_EVENT_REQ_OVER_SOCKET, body)
    }

    @Synchronized
    fun sendLiveEventRequest(body: String) {
        mSocket?.emit(CEIBRO_LIVE_EVENT_BY_USER, body)
    }


}