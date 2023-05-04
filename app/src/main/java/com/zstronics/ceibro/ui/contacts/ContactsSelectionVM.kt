package com.zstronics.ceibro.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.extensions.getLocalContacts
import com.zstronics.ceibro.resourses.IResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContactsSelectionVM @Inject constructor(
    override val viewState: ContactsSelectionState,
    override var validator: Validator?,
    val sessionManager: SessionManager,
    private val resProvider: IResourceProvider,
    private val dashboardRepository: IDashboardRepository
) : HiltBaseViewModel<IContactsSelection.State>(), IContactsSelection.ViewModel, IValidator {
    private val _contacts: MutableLiveData<List<SyncContactsRequest.CeibroContactLight>> =
        MutableLiveData()
    val contacts: LiveData<List<SyncContactsRequest.CeibroContactLight>> = _contacts

    fun loadContacts() {
        launch(Dispatcher.LongOperation) {
            val contacts = getLocalContacts(resProvider.context)
            _contacts.postValue(contacts)
        }
    }

    override fun syncContacts(
        selectedContacts: List<SyncContactsRequest.CeibroContactLight>,
        onSuccess: () -> Unit
    ) {
        val userId = sessionManager.getUserId()
        launch {
            val request = SyncContactsRequest(contacts = selectedContacts)
            // Handle the API response
            loading(true)
            when (val response =
                dashboardRepository.syncContacts(userId, request)) {
                is ApiResponse.Success -> {
                    loading(false)
                    onSuccess.invoke()
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

    fun syncContactsEnabled(
        enabled: Boolean,
        onSuccess: () -> Unit
    ) {
        val phone = sessionManager.getUser().value?.phoneNumber
//        val phone = "+923120619435"
        launch {
            // Handle the API response
            when (val response =
                dashboardRepository.syncContactsEnabled(phone ?: "", enabled = enabled)) {
                is ApiResponse.Success -> {
                    sessionManager.updateAutoSync(enabled)
                    if (enabled) {
                        val contacts = getLocalContacts(resProvider.context)
                        syncContacts(contacts, onSuccess)
                    }
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
    }
}