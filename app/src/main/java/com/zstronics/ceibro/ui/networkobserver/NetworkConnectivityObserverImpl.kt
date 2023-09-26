package com.zstronics.ceibro.ui.networkobserver

import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class NetworkConnectivityObserverImpl(
    connectivityManager: ConnectivityManager
) : NetworkConnectivityObserver {
    var appFirstRun = true
    private val networkStatus = MutableSharedFlow<NetworkConnectivityObserver.Status>(
        replay = 1,
        extraBufferCapacity = 1,
        BufferOverflow.DROP_OLDEST
    )

    init {
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if (appFirstRun.not())
                    networkStatus.tryEmit(NetworkConnectivityObserver.Status.Available)
                appFirstRun = false
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                networkStatus.tryEmit(NetworkConnectivityObserver.Status.Losing)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                networkStatus.tryEmit(NetworkConnectivityObserver.Status.Lost)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                networkStatus.tryEmit(NetworkConnectivityObserver.Status.Unavailable)
            }
        })
    }

    override fun observe(): Flow<NetworkConnectivityObserver.Status> =
        networkStatus.asSharedFlow()
}
