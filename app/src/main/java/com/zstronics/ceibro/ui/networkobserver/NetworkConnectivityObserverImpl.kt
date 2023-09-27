package com.zstronics.ceibro.ui.networkobserver

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce

internal class NetworkConnectivityObserverImpl(
    private val connectivityManager: ConnectivityManager
) : NetworkConnectivityObserver {
    var appFirstRun = true
    private val networkStatus = MutableStateFlow(
        NetworkConnectivityObserver.Status.Losing
    )

    @OptIn(FlowPreview::class)
    private val debouncedStatus = networkStatus
        .debounce(1000) // Adjust the debounce duration as needed (1 second in this example)

    init {
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if (appFirstRun.not())
                    updateStatus(NetworkConnectivityObserver.Status.Available)
                appFirstRun = false
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                updateStatus(NetworkConnectivityObserver.Status.Losing)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                updateStatus(NetworkConnectivityObserver.Status.Lost)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                updateStatus(NetworkConnectivityObserver.Status.Unavailable)
            }
        })
    }

    private fun updateStatus(status: NetworkConnectivityObserver.Status) {
        networkStatus.value = status
    }

    override fun observe(): Flow<NetworkConnectivityObserver.Status> =
        debouncedStatus

    override fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
