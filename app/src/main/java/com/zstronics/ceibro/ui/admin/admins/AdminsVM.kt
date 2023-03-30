package com.zstronics.ceibro.ui.admin.admins

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
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
class AdminsVM @Inject constructor(
    override val viewState: AdminsState,
    private val dashboardRepository: DashboardRepository
) : HiltBaseViewModel<IAdmins.State>(), IAdmins.ViewModel {

    private val _allAdmins: MutableLiveData<List<AdminUsersResponse.AdminUserData>> = MutableLiveData()
    val allAdmins: LiveData<List<AdminUsersResponse.AdminUserData>> = _allAdmins

    var originalAdmins: List<AdminUsersResponse.AdminUserData> = listOf()

    init {
        EventBus.getDefault().register(this)
    }

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
                    originalAdmins = data.result
                    _allAdmins.postValue(originalAdmins)
                }

                is ApiResponse.Error -> {
                    adminsRVLayout.hideSkeleton()
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onApplySearch(filter: LocalEvents.ApplySearchOnAdmins) {
        if (filter.query?.isEmpty() == true) {
            _allAdmins.postValue(originalAdmins)
            return
        }
        if (filter.query != null) {
            val filtered =
                originalAdmins.filter {
                    it.firstName.contains(filter.query, true) || it.surName.contains(filter.query, true)
                }
            _allAdmins.postValue(filtered)
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