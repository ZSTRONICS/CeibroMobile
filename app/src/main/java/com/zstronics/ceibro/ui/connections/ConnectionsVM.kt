package com.zstronics.ceibro.ui.connections

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.AllConnectionsResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConnectionsVM @Inject constructor(
    override val viewState: ConnectionsState,
    private val dashboardRepository: DashboardRepository
) : HiltBaseViewModel<IConnections.State>(), IConnections.ViewModel {

    private val _allConnections: MutableLiveData<MutableList<MyConnection>> = MutableLiveData()
    val allConnections: LiveData<MutableList<MyConnection>> = _allConnections

    override fun onResume() {
        super.onResume()
        loadConnections()
    }

    override fun loadConnections() {
        launch {
            loading(true)
            when (val response = dashboardRepository.getAllConnections()) {

                is ApiResponse.Success -> {
                    loading(false)
                    val data = response.data
                    _allConnections.postValue(data.myConnections as MutableList<MyConnection>?)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

}