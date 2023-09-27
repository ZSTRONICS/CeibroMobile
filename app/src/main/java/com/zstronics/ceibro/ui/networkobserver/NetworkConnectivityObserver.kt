package com.zstronics.ceibro.ui.networkobserver

import kotlinx.coroutines.flow.Flow

interface NetworkConnectivityObserver {
    fun observe(): Flow<Status>

    fun isNetworkAvailable(): Boolean
    enum class Status {
        Available, Unavailable, Losing, Lost
    }
}
