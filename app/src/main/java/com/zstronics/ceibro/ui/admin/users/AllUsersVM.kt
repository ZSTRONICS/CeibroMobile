package com.zstronics.ceibro.ui.admin.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.admins.AdminUsersResponse
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class AllUsersVM @Inject constructor(
    override val viewState: AllUsersState,
    private val dashboardRepository: DashboardRepository
) : HiltBaseViewModel<IAllUsers.State>(), IAllUsers.ViewModel {

    private val _allUsers: MutableLiveData<List<AdminUsersResponse.AdminUserData>> = MutableLiveData()
    val allUsers: LiveData<List<AdminUsersResponse.AdminUserData>> = _allUsers

    var originalUsers: List<AdminUsersResponse.AdminUserData> = listOf()

    init {
        EventBus.getDefault().register(this)
    }

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
                    originalUsers = data.result
                    _allUsers.postValue(originalUsers)
                }

                is ApiResponse.Error -> {
                    userRVLayout.hideSkeleton()
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onApplySearch(filter: LocalEvents.ApplySearchOnAllUsers) {
        if (filter.query?.isEmpty() == true) {
            _allUsers.postValue(originalUsers)
            return
        }
        if (filter.query != null) {
            val filtered =
                originalUsers.filter {
                    it.firstName.contains(filter.query, true) || it.surName.contains(filter.query, true)
                }
            _allUsers.postValue(filtered)
        }
        else {
            alert("Unable to search")
        }
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

}