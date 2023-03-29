package com.zstronics.ceibro.ui.admin.admins

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.admins.AdminUserObj
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminsVM @Inject constructor(
    override val viewState: AdminsState,
    private val dashboardRepository: DashboardRepository
) : HiltBaseViewModel<IAdmins.State>(), IAdmins.ViewModel {

    private val _allAdmins: MutableLiveData<MutableList<AdminUserObj>> = MutableLiveData()
    val allAdmins: LiveData<MutableList<AdminUserObj>> = _allAdmins


    override fun onResume() {
        super.onResume()
        loadAdmins()
    }

    private fun loadAdmins() {
        launch {
            loading(true)
            when (val response = dashboardRepository.getAdminsOrUsersList(role = "admin")) {
                is ApiResponse.Success -> {
                    loading(false)
                    val data = response.data
                    _allAdmins.postValue(data.result as MutableList<AdminUserObj>)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }
}