package com.zstronics.ceibro.ui.invitations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.repos.auth.login.LoginRequest
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitations
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitationsItem
import com.zstronics.ceibro.data.repos.dashboard.invites.SendInviteRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InvitationsVM @Inject constructor(
    override val viewState: InvitationsState,
    override var validator: Validator?,
    private val dashboardRepository: DashboardRepository
) : HiltBaseViewModel<IInvitations.State>(), IInvitations.ViewModel, IValidator {

    private val _allInvites: MutableLiveData<MutableList<MyInvitationsItem>> = MutableLiveData()
    val allInvites: LiveData<MutableList<MyInvitationsItem>> = _allInvites

    override fun onResume() {
        super.onResume()
        loadInvitations()
    }

    override fun onInvite() {
        sendInvite(viewState.inviteEmail.value.toString())
    }

    override fun sendInvite(email: String) {

        val request = SendInviteRequest(email = email)
        launch {
            loading(true)
            when (val response = dashboardRepository.sendInvite(request)) {

                is ApiResponse.Success -> {
                    loading(false, response.data.message)
                    viewState.inviteEmail.value = ""
//                    clickEvent?.postValue(113)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
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

    override fun acceptOrRejectInvitation(accepted: Boolean, inviteId: String, position: Int) {
        loading(true)
        launch {
            when (val response = dashboardRepository.acceptOrRejectInvitation(accepted, inviteId)) {
                is ApiResponse.Success -> {
                    loading(false, response.data.message)
                    val allInvitesTemp = _allInvites.value
                    allInvitesTemp?.removeAt(position)
                    _allInvites.value = allInvitesTemp
//                    _allInvites.postValue(allInvitesTemp)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

}