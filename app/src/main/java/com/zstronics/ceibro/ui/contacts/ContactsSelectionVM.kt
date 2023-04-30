package com.zstronics.ceibro.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
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
    private val sessionManager: SessionManager,
    private val resProvider: IResourceProvider,
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

    fun syncContacts(selectedContacts: List<SyncContactsRequest.CeibroContactLight>) {

    }
}