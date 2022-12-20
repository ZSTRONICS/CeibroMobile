package com.zstronics.ceibro.ui.invitations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitations
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitationsItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InvitationsVM @Inject constructor(
    override val viewState: InvitationsState,
    private val dashboardRepository: DashboardRepository
) : HiltBaseViewModel<IInvitations.State>(), IInvitations.ViewModel {

    private val _allInvites: MutableLiveData<MutableList<MyInvitationsItem>> = MutableLiveData()
    val allInvites: LiveData<MutableList<MyInvitationsItem>> = _allInvites

    override fun onResume() {
        super.onResume()
        loadInvitations()
    }

    override fun loadInvitations() {
        launch {
            loading(true)
            when (val response = dashboardRepository.getAllInvites()) {

                is ApiResponse.Success -> {
                    loading(false)
                    val data = response.data
                    _allInvites.postValue(data.invites as MutableList<MyInvitationsItem>?)
                }

                is ApiResponse.Error -> {
                    loading(false)
                }
            }
        }
    }

}