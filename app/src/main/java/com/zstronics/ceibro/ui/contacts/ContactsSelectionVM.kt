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
    var originalContacts = listOf<SyncContactsRequest.CeibroContactLight>()

    private var _contactsGroup: MutableLiveData<MutableList<ContactSelectionGroup>> =
        MutableLiveData()
    val contactsGroup: MutableLiveData<MutableList<ContactSelectionGroup>> =
        _contactsGroup


    fun loadContacts() {
        launch(Dispatcher.LongOperation) {
            val contacts = getLocalContacts(resProvider.context)
            originalContacts = contacts
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

    fun filterContacts(search: String) {
        if (search.isEmpty()) {
            if (originalContacts.isNotEmpty()) {
                _contacts.postValue(originalContacts as MutableList<SyncContactsRequest.CeibroContactLight>?)
            }

            return
        }
        val filtered = originalContacts.filter {
            it.contactFirstName.lowercase().contains(search) || it.contactSurName.lowercase()
                .contains(search) || it.phoneNumber.contains(search)
        }
        if (filtered.isNotEmpty())
            _contacts.postValue(filtered as MutableList<SyncContactsRequest.CeibroContactLight>?)
        else
            _contacts.postValue(mutableListOf())
    }

    fun groupDataByFirstLetter(data: List<SyncContactsRequest.CeibroContactLight>) {
        val sections = mutableListOf<ContactSelectionGroup>()

        val groupedData = data.groupBy {
            if (it.contactFirstName.firstOrNull()?.isLetter() == true) {
                it.contactFirstName.first().lowercase()
            } else {
                '#'.toString()
            }
        }.toSortedMap(
            compareBy<String> { it != "#" }
                .then(compareBy { it.lowercase() })
                .then(compareByDescending { it == "#" })
        )

        for (mapKey in groupedData.keys) {
            sections.add(
                ContactSelectionGroup(
                    mapKey.toString().uppercase()[0],
                    groupedData[mapKey]?.sortedBy { it.contactFirstName.lowercase() }
                        ?: emptyList()
                )
            )
        }
        _contactsGroup.value = sections
    }

    data class ContactSelectionGroup(
        val sectionLetter: Char,
        var items: List<SyncContactsRequest.CeibroContactLight>
    )
}