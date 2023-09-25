package com.zstronics.ceibro.ui.networkobserver

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged

internal class NetworkConnectivityObserverImpl(
    connectivityManager: ConnectivityManager
) : NetworkConnectivityObserver {

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
                networkStatus.tryEmit(NetworkConnectivityObserver.Status.Available)
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

        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            networkStatus.tryEmit(NetworkConnectivityObserver.Status.Available)
        } else {
            networkStatus.tryEmit(NetworkConnectivityObserver.Status.Unavailable)
        }
    }

    override fun observe(): Flow<NetworkConnectivityObserver.Status> =
        networkStatus.asSharedFlow()
}
