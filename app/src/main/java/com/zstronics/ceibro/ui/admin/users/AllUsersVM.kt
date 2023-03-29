package com.zstronics.ceibro.ui.admin.users

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
class AllUsersVM @Inject constructor(
    override val viewState: AllUsersState,
    private val dashboardRepository: DashboardRepository
) : HiltBaseViewModel<IAllUsers.State>(), IAllUsers.ViewModel {

    private val _allUsers: MutableLiveData<MutableList<AdminUsersResponse.AdminUserData>> = MutableLiveData()
    val allUsers: LiveData<MutableList<AdminUsersResponse.AdminUserData>> = _allUsers


    fun loadUsers(userRVLayout: RecyclerView) {
        launch {
            userRVLayout.loadSkeleton(R.layout.layout_admin_user_box) {
                itemCount(11)
                color(R.color.appLightGrey)
            }
            when (val response = dashboardRepository.getAdminsOrUsersList(role = "user")) {
                is ApiResponse.Success -> {
                    userRVLayout.hideSkeleton()
                    val data = response.data
                    _allUsers.postValue(data.result as MutableList<AdminUsersResponse.AdminUserData>)
                }

                is ApiResponse.Error -> {
                    userRVLayout.hideSkeleton()
                }
            }
        }
    }

}