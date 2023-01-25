package com.zstronics.ceibro.ui.socket

import com.zstronics.ceibro.BuildConfig
import com.zstronics.ceibro.data.base.CookiesManager
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketHandler {

    private lateinit var mSocket: Socket

    const val SEND_MESSAGE = "SEND_MESSAGE"
    const val TYPING_START = "TYPING_START"
    const val RECEIVE_MESSAGE = "RECEIVE_MESSAGE"
    const val CHAT_EVENT_REP_OVER_SOCKET = "CHAT_EVENT_REP_OVER_SOCKET"
    const val CHAT_EVENT_REQ_OVER_SOCKET = "CHAT_EVENT_REQ_OVER_SOCKET"
    const val CEIBRO_LIVE_EVENT_BY_USER = "CEIBRO_LIVE_EVENT_BY_USER"
    const val CEIBRO_LIVE_EVENT_BY_SERVER = "CEIBRO_LIVE_EVENT_BY_SERVER"

    @Synchronized
    fun setSocket() {
        try {
            val options = IO.Options()

            options.forceNew = true
//            options.transports = arrayOf( Polling.NAME )
            options.reconnectionAttempts = Integer.MAX_VALUE;
            options.timeout = 10000
            options.query = "token=${CookiesManager.jwtToken}"
            mSocket = IO.socket(BuildConfig.SOCKET_URL, options)

//            mSocket.io().on(RECEIVE_MESSAGE) { args ->
//                args
//            }

//            mSocket.io().on(SEND_MESSAGE) { args ->
//                args
//            }

            mSocket.io().on(CHAT_EVENT_REQ_OVER_SOCKET) { args ->
                args
            }
            mSocket.io().on(CEIBRO_LIVE_EVENT_BY_USER) { args ->
                args
            }

        } catch (exception: URISyntaxException) {
            exception.message
        }
    }

    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket.connect()
    }

    @Synchronized
    fun closeConnection() {
        offAllEventOObservers()
        mSocket.disconnect()
    }

    @Synchronized
    fun offAllEventOObservers() {
        mSocket.io().off(CHAT_EVENT_REP_OVER_SOCKET)
        mSocket.io().off(CHAT_EVENT_REQ_OVER_SOCKET)
        mSocket.io().off(CEIBRO_LIVE_EVENT_BY_USER)
        mSocket.io().off(CEIBRO_LIVE_EVENT_BY_SERVER)
    }

    @Synchronized
    fun sendChatRequest(body: String){
        mSocket.emit(CHAT_EVENT_REQ_OVER_SOCKET, body)
    }

    @Synchronized
    fun sendLiveEventRequest(body: String){
        mSocket.emit(CEIBRO_LIVE_EVENT_BY_USER, body)
    }
}