package com.zstronics.ceibro.ui.socket

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.zstronics.ceibro.BuildConfig
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import io.socket.client.IO
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import java.net.URISyntaxException

object SocketHandler {

    private var mSocket: Socket? = null
    private var requireActivity: FragmentActivity? = null

    const val SEND_MESSAGE = "SEND_MESSAGE"
    const val TYPING_START = "TYPING_START"
    const val RECEIVE_MESSAGE = "RECEIVE_MESSAGE"
    const val CHAT_EVENT_REP_OVER_SOCKET = "CHAT_EVENT_REP_OVER_SOCKET"
    const val CHAT_EVENT_REQ_OVER_SOCKET = "CHAT_EVENT_REQ_OVER_SOCKET"
    const val CEIBRO_LIVE_EVENT_BY_USER = "CEIBRO_LIVE_EVENT_BY_USER"
    const val CEIBRO_LIVE_EVENT_BY_SERVER = "CEIBRO_LIVE_EVENT_BY_SERVER"
    const val CEIBRO_HEARTBEAT = "heartbeat"
    const val CEIBRO_HEARTBEAT_ACK = "heartbeatAck"
    const val CEIBRO_EVENT_ACK = "eventAck"
    const val CEIBRO_LOGOUT = "logout"
    const val CLEAR_DATA = "clearData"
    var hbCounter = 0
    var handler = android.os.Handler()
    var delayMillis: Long = 10000 // 10 seconds
    var runnable = object : Runnable {
        override fun run() {
            sendHeartbeat()
            handler.postDelayed(this, delayMillis)
        }
    }

    enum class TaskEvent {
        TASK_CREATED, TASK_UPDATE_PUBLIC, TASK_UPDATE_PRIVATE, SUB_TASK_CREATED, SUB_TASK_UPDATE_PUBLIC, SUB_TASK_UPDATE_PRIVATE,
        TASK_SUBTASK_UPDATED, COMMENT_WITH_FILES, SUBTASK_NEW_COMMENT, TASK_FORWARDED, TASK_FORWARDED_TO_ME, TASK_SEEN, NEW_TASK_COMMENT, TASK_DONE, CANCELED_TASK,
        TASK_HIDDEN, TASK_SHOWN, JOINED_TASK, UN_CANCEL_TASK
    }

    enum class ProjectEvent {
        PROJECT_CREATED, PROJECT_UPDATED, REFRESH_PROJECTS, ROLE_CREATED, ROLE_UPDATED, REFRESH_ROLES,
        PROJECT_GROUP_CREATED, PROJECT_GROUP_UPDATED, REFRESH_PROJECT_GROUP, PROJECT_MEMBERS_ADDED, PROJECT_MEMBERS_UPDATED, REFRESH_PROJECT_MEMBERS,
        REFRESH_ROOT_DOCUMENTS, REFRESH_FOLDER
    }

    enum class UserEvent {
        USER_UPDATED,USER_INFO_UPDATED, REFRESH_ALL_USERS, REFRESH_CONNECTIONS, REFRESH_INVITATIONS
    }

    enum class FileAttachmentEvents {
        FILE_UPLOAD_PROGRESS, FILE_UPLOADED, FILES_UPLOAD_COMPLETED
    }

    fun setActivityContext(requireActivity: FragmentActivity) {
        this.requireActivity = requireActivity
    }



    @Synchronized
    fun setSocket() {
        try {
            val options = IO.Options()

            options.forceNew = true
            options.transports = arrayOf("websocket")
            options.reconnection = true
            options.reconnectionAttempts = Integer.MAX_VALUE
            options.timeout = 10000
            options.auth =
                mapOf("token" to CookiesManager.jwtToken) // Use auth for token instead of query
            options.query =
                "secureUUID=${CookiesManager.secureUUID}&deviceType=${CookiesManager.deviceType}&androidId=${CookiesManager.androidId}"
//            println("QueryOnSocket: ${options.query}")

            mSocket = IO.socket(BuildConfig.SOCKET_URL, options)
            val sharedViewModel =
                requireActivity?.let { ViewModelProvider(it).get(SharedViewModel::class.java) }

            mSocket?.on(
                Socket.EVENT_DISCONNECT
            ) {
                println("Heartbeat, Socket Disconnected")
                if (sharedViewModel != null) {
                    sharedViewModel.isConnectedToServer.postValue(false)
                }
            }

            mSocket?.on(
                Socket.EVENT_CONNECT_ERROR
            ) {
                println("Heartbeat, Socket EVENT_CONNECT_ERROR")
                handler.removeCallbacks(runnable)
//                hbCounter = 0
//                handler.postDelayed(runnable, delayMillis)
                establishConnection()
                if (sharedViewModel != null) {
                    sharedViewModel.isConnectedToServer.postValue(false)
                }
            }

            mSocket?.on(
                Socket.EVENT_CONNECT
            ) {
                handler.removeCallbacks(runnable)
                hbCounter = 0
                handler.postDelayed(runnable, delayMillis)
                println("Heartbeat, Socket Connected")

                if (sharedViewModel != null) {
                    sharedViewModel.isConnectedToServer.postValue(true)
                    sharedViewModel.socketOnceConnected.postValue(true)
                }
                //EventBus.getDefault().post(LocalEvents.InitSocketEventCallBack())
            }

            mSocket?.on(
                CEIBRO_HEARTBEAT_ACK
            ) {
//                println("Heartbeat, CEIBRO_HEARTBEAT_ACK... connected = ${mSocket?.connected()}")
//                hbCounter -= 1
            }

            EventBus.getDefault().post(LocalEvents.InitSocketEventCallBack())

        } catch (exception: URISyntaxException) {
            exception.message
        }
    }

    fun sendHeartbeat() {
        if (mSocket != null) {
            if (mSocket?.connected() == true) {
                mSocket?.emit(CEIBRO_HEARTBEAT)
//                hbCounter += 1
////                println("Heartbeat Sent $hbCounter")
//                if (hbCounter == 6) {
//                    // reconnect logic here
//                    reconnectSocket()
//                }
//                if (hbCounter > 5) {
//                    hbCounter = 0
//                }
            } else {
//                println("Heartbeat Socket not connected")
//                handler.removeCallbacks(runnable)
//                reconnectSocket()
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
//        println("Heartbeat, Socket connecting... ${mSocket?.connected()}")
    }

    @Synchronized
    fun disconnectSocket() {
        handler.removeCallbacks(runnable)
        mSocket?.disconnect()

    }

    @Synchronized
    fun closeConnectionAndRemoveObservers() {
        offAllEventOObservers()
        disconnectSocket()
    }

    @Synchronized
    fun offAllEventOObservers() {
        mSocket?.io()?.off(CHAT_EVENT_REP_OVER_SOCKET)
        mSocket?.io()?.off(CHAT_EVENT_REQ_OVER_SOCKET)
        mSocket?.io()?.off(CEIBRO_LIVE_EVENT_BY_USER)
        mSocket?.io()?.off(CEIBRO_LIVE_EVENT_BY_SERVER)
        mSocket?.io()?.off(CEIBRO_HEARTBEAT)
        mSocket?.io()?.off(CEIBRO_HEARTBEAT_ACK)
        mSocket?.io()?.off(CEIBRO_EVENT_ACK)
        mSocket?.io()?.off(CEIBRO_LOGOUT)
        mSocket?.io()?.off(CLEAR_DATA)
        handler.removeCallbacks(runnable)
//        EventBus.getDefault().unregister(this)
    }

    @Synchronized
    fun sendChatRequest(body: String) {
        mSocket?.emit(CHAT_EVENT_REQ_OVER_SOCKET, body)
    }

    @Synchronized
    fun sendEventAck(uuid: String) {
        mSocket?.emit(CEIBRO_EVENT_ACK, uuid)
    }

    @Synchronized
    fun sendLogout() {
        mSocket?.emit(CEIBRO_LOGOUT)
    }

    @Synchronized
    fun sendLiveEventRequest(body: String) {
        mSocket?.emit(CEIBRO_LIVE_EVENT_BY_USER, body)
    }


}