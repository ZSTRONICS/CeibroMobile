package com.zstronics.ceibro.ui.dashboard.manual_contact_selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.extensions.getLocalContacts
import com.zstronics.ceibro.resourses.IResourceProvider
import com.zstronics.ceibro.ui.contacts.toLightContacts
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManualContactsSelectionVM @Inject constructor(
    override val viewState: ManualContactsSelectionState,
    override var validator: Validator?,
    val sessionManager: SessionManager,
    private val resProvider: IResourceProvider,
    val connectionsV2Dao: ConnectionsV2Dao
) : HiltBaseViewModel<IManualContactsSelection.State>(), IManualContactsSelection.ViewModel,
    IValidator {
    val user = sessionManager.getUser().value
    private val _contacts: MutableLiveData<List<SyncContactsRequest.CeibroContactLight>> =
        MutableLiveData()
    val contacts: LiveData<List<SyncContactsRequest.CeibroContactLight>> = _contacts
    var originalContacts = listOf<SyncContactsRequest.CeibroContactLight>()

    override fun onResume() {
        super.onResume()
        loadContacts()
    }

    private fun loadContacts() {
        launch(Dispatcher.LongOperation) {
            val contacts = getLocalContacts(resProvider.context)
            val preSelectedContacts = connectionsV2Dao.getAll().toLightContacts()
            // Loop through each contact in viewModel.contacts
            contacts.forEach { contact ->
                // Check if the contact exists in preSelectedContacts based on phone number
                val matchedContact = preSelectedContacts.find {
                    it.phoneNumber == contact.phoneNumber
                }
                // Set the isChecked property based on the existence in preSelectedContacts
                contact.isChecked = matchedContact != null
            }
            originalContacts = contacts
            _contacts.postValue(contacts)
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
            "${it.contactFirstName.lowercase()} ${it.contactSurName.lowercase()}".contains(
                search,
                true
            ) ||
                    it.phoneNumber.contains(search)
        }
        if (filtered.isNotEmpty())
            _contacts.postValue(filtered as MutableList<SyncContactsRequest.CeibroContactLight>?)
        else
            _contacts.postValue(mutableListOf())
    }
}