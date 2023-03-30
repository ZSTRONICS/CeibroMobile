package com.zstronics.ceibro.ui.admin.admins

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.admins.AdminUsersResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import javax.inject.Inject

@HiltViewModel
class AdminsVM @Inject constructor(
    override val viewState: AdminsState,
    private val dashboardRepository: DashboardRepository
) : HiltBaseViewModel<IAdmins.State>(), IAdmins.ViewModel {

    private val _allAdmins: MutableLiveData<MutableList<AdminUsersResponse.AdminUserData>> = MutableLiveData()
    val allAdmins: LiveData<MutableList<AdminUsersResponse.AdminUserData>> = _allAdmins


    fun loadAdmins(adminsRVLayout: RecyclerView) {
        launch {
            adminsRVLayout.loadSkeleton(R.layout.layout_admin_user_box) {
                itemCount(11)
                color(R.color.appLightGrey)
            }
            when (val response = dashboardRepository.getAdminsOrUsersList(role = "admin")) {
                is ApiResponse.Success -> {
                    adminsRVLayout.hideSkeleton()
                    val data = response.data
                    _allAdmins.postValue(data.result as MutableList<AdminUsersResponse.AdminUserData>)
                }

                is ApiResponse.Error -> {
                    adminsRVLayout.hideSkeleton()
                }
            }
        }
    }
}