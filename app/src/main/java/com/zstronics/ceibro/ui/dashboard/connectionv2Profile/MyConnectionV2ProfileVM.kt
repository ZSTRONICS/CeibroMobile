package com.zstronics.ceibro.ui.dashboard.connectionv2Profile

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.dashboard.myconnectionsv2.MyConnectionV2Fragment.Companion.CONNECTION_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyConnectionV2ProfileVM @Inject constructor(
    override val viewState: MyConnectionV2ProfileState,
    val sessionManager: SessionManager,
    val dashboardRepository: IDashboardRepository
) : HiltBaseViewModel<IMyConnectionV2Profile.State>(), IMyConnectionV2Profile.ViewModel {
    val user = sessionManager.getUser().value
    private val _connection: MutableLiveData<AllCeibroConnections.CeibroConnection> =
        MutableLiveData()
    val connection: MutableLiveData<AllCeibroConnections.CeibroConnection> = _connection
    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val connection =
            bundle?.getParcelable<AllCeibroConnections.CeibroConnection>(CONNECTION_KEY)
        connection?.let { connectionNotNull ->
            _connection.postValue(connectionNotNull)
        }
    }

    fun blockUser(contactId: String, callBack: () -> Unit) {
        loading(true)
        launch {
            when (val response = dashboardRepository.blockUser(contactId)) {

                is ApiResponse.Success -> {
                    callBack.invoke()
                    loading(false)
                    updateBlockStatus(true)
                }

                is ApiResponse.Error -> {
                    callBack.invoke()
                    loading(false, response.error.message)
                }
            }
        }
    }

    fun unblockUser(contactId: String, callBack: () -> Unit) {
        loading(true)
        launch {
            when (val response = dashboardRepository.blockUser(contactId)) {
                is ApiResponse.Success -> {
                    callBack.invoke()
                    loading(false)
                    updateBlockStatus(false)
                }

                is ApiResponse.Error -> {
                    callBack.invoke()
                    loading(false, response.error.message)
                }
            }
        }
    }

    private fun updateBlockStatus(isBlocked: Boolean) {
        val localConnection = _connection.value
        localConnection?.isBlocked = isBlocked
        localConnection?.let {
            _connection.value = it
        }
    }
}