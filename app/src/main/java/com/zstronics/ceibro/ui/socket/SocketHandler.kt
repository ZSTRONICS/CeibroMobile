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
//        mSocket.io().off(RECEIVE_MESSAGE)
        mSocket.io().off(CHAT_EVENT_REQ_OVER_SOCKET)
    }

    @Synchronized
    fun sendRequest(body: String){
        mSocket.emit(CHAT_EVENT_REQ_OVER_SOCKET, body)
    }
}