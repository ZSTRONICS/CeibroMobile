package com.zstronics.ceibro.ui.connections

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class ConnectionsVM @Inject constructor(
    override val viewState: ConnectionsState,
    private val dashboardRepository: DashboardRepository
) : HiltBaseViewModel<IConnections.State>(), IConnections.ViewModel {

    private val _allConnections: MutableLiveData<MutableList<MyConnection>> = MutableLiveData()
    val allConnections: LiveData<MutableList<MyConnection>> = _allConnections


    override fun loadConnections(connectionRV: RecyclerView) {
        launch {
            connectionRV.loadSkeleton(R.layout.layout_connection_box) {
                itemCount(10)
                color(R.color.appLightGrey)
            }
            when (val response = dashboardRepository.getAllConnections()) {

                is ApiResponse.Success -> {
                    connectionRV.hideSkeleton()
                    val data = response.data
                    _allConnections.postValue(data.myConnections as MutableList<MyConnection>?)
                }

                is ApiResponse.Error -> {
                    connectionRV.hideSkeleton()
                }
            }
        }
    }

}