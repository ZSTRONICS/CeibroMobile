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

            mSocket.io().on(SEND_MESSAGE) { args ->
                args
            }

            mSocket.io().on(TYPING_START) { args ->
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
        mSocket.io().off(TYPING_START)
        mSocket.io().off(RECEIVE_MESSAGE)
        mSocket.io().off(SEND_MESSAGE)
    }

    @Synchronized
    fun sendMessage(message: String){
        mSocket.emit(SEND_MESSAGE, message)
    }
}