package com.zstronics.ceibro.ui.groupsv2.fragment

import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.contacts.dbCeibroUserToLightTaskMembers
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupAssigneeVM @Inject constructor(
    override val viewState: GroupAssigneeState,
    val sessionManager: SessionManager,
    val dashboardRepository: IDashboardRepository,
    private val connectionsV2Dao: ConnectionsV2Dao
) : HiltBaseViewModel<IGroupAssignee.State>(), IGroupAssignee.ViewModel {
    val user = sessionManager.getUser().value

    private var _allConnections: MutableLiveData<MutableList<TaskMemberDetail>> =
        MutableLiveData()
    val allConnections: MutableLiveData<MutableList<TaskMemberDetail>> =
        _allConnections
    var originalConnections = listOf<TaskMemberDetail>()
    private var _recentAllConnections: MutableLiveData<MutableList<TaskMemberDetail>?> =
        MutableLiveData(mutableListOf())
    val recentAllConnections: MutableLiveData<MutableList<TaskMemberDetail>?> =
        _recentAllConnections
    var recentOriginalConnections = listOf<TaskMemberDetail>()

    var selectedContacts: MutableLiveData<MutableList<TaskMemberDetail>> =
        MutableLiveData()
    var disableSelectedContacts: MutableLiveData<MutableList<TaskMemberDetail>> =
        MutableLiveData()
    var isConfirmer: MutableLiveData<Boolean> = MutableLiveData(false)
    var isViewer: MutableLiveData<Boolean> = MutableLiveData(false)

    val contactsToRemoveFromAll: ArrayList<TaskMemberDetail> = ArrayList()


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val selfAssigned = bundle?.getBoolean("self-assign")
        val isConfirmer = bundle?.getBoolean("isConfirmer")
        val isViewer = bundle?.getBoolean("isViewer")
        isConfirmer?.let {
            this.isConfirmer.value = it
        } ?: kotlin.run {
            this.isConfirmer.value = false
        }
//        isViewer?.let {
        this.isViewer.value = isViewer ?: false
//        } ?: kotlin.run {
//            this.isViewer.value = false
//        }

        val disabledContacts = bundle?.getParcelableArray("disabledContacts")
        val disabledContactList =
            disabledContacts?.map { it as TaskMemberDetail }
                ?.toMutableList()
        if (!disabledContactList.isNullOrEmpty()) {
            disableSelectedContacts.postValue(disabledContactList as MutableList<TaskMemberDetail>?)
        }

        val selectedContact = bundle?.getParcelableArray("contacts")
        val selectedContactList =
            selectedContact?.map { it as TaskMemberDetail }
                ?.toMutableList()
        if (!selectedContactList.isNullOrEmpty()) {

            val list = selectedContactList.distinctBy { it.phoneNumber }
            selectedContacts.postValue(list as MutableList<TaskMemberDetail>?)
        }
        //existing Contacts
        val existingContacts = bundle?.getParcelableArray("existingContacts")

        if (existingContacts != null) {
            for (contact in existingContacts) {
                if (contact is TaskMemberDetail) {
                    selectedContactList?.find { it.phoneNumber == contact.phoneNumber }?.let {
                        contact.isChecked = true
                    }
                    contactsToRemoveFromAll.add(contact)
                }
            }
        }

        val selectedExistingContacts =
            existingContacts?.map { it as TaskMemberDetail }
                ?.toMutableList()
        if (!selectedExistingContacts.isNullOrEmpty()) {

            val list = contactsToRemoveFromAll.distinctBy { it.phoneNumber }

            _recentAllConnections.postValue(list as MutableList<TaskMemberDetail>?)
        }
        val handler = Handler()
        handler.postDelayed(Runnable {
            if (selfAssigned != null) {
                viewState.isSelfAssigned.value = selfAssigned
            }
        }, 50)

    }

    fun getAllConnectionsV2(callBack: () -> Unit) {
        // loadRecentConnections()
        launch {
            val connectionsData = connectionsV2Dao.getAll()

            val list = connectionsData.filter { it.isCeiborUser }
            val taskMemberDetail = list.dbCeibroUserToLightTaskMembers().toMutableList()
            processConnectionsData(taskMemberDetail, callBack)
        }
    }

    private fun processConnectionsData(
        contactsResponse: List<TaskMemberDetail>,
        callBack: () -> Unit
    ) {

        val allContacts = contactsResponse.groupDataByFirstLetter().toMutableList()
        val oldSelectedContacts = selectedContacts.value

        if (!oldSelectedContacts.isNullOrEmpty()) {
            oldSelectedContacts.forEach { oldContact ->
                val matchingContact = allContacts.find { it.id == oldContact.id }
                matchingContact?.isChecked = true
            }
            _allConnections.value = allContacts
            originalConnections = allContacts
            callBack.invoke()
        } else {
            if (allContacts.isNotEmpty()) {
                originalConnections = allContacts
                _allConnections.value = allContacts
            } else {
                _allConnections.postValue(mutableListOf())
            }
            callBack.invoke()
        }
        updateAllConnections()
    }

    /* private fun loadRecentConnections() {
         launch {
             when (val response = dashboardRepository.getRecentCeibroConnections()) {
                 is ApiResponse.Success -> {
                     val newItemsList =
                         response.data.recentContacts.distinctBy { it.id } // Your 10 items here

                     val allContacts = newItemsList.groupDataByFirstLetter().toMutableList()
                     val oldSelectedContacts = selectedContacts.value

                     var updatedAllContacts: MutableList<TaskMemberDetail> = mutableListOf()
                     updatedAllContacts = allContacts.filter { it.isCeiborUser }.toMutableList()


                     if (!oldSelectedContacts.isNullOrEmpty()) {
                         oldSelectedContacts.forEach { oldContact ->
                             val matchingContact = updatedAllContacts.find { it.id == oldContact.id }
                             matchingContact?.isChecked = true
                         }
                         _recentAllConnections.postValue(updatedAllContacts)
                         recentOriginalConnections = updatedAllContacts
                     } else {
                         if (updatedAllContacts.isNotEmpty()) {
                             recentOriginalConnections = updatedAllContacts
                             _recentAllConnections.value = updatedAllContacts
                         } else {
                             _recentAllConnections.postValue(mutableListOf())
                         }
                     }
                 }

                 is ApiResponse.Error -> {
                     _recentAllConnections.postValue(mutableListOf())
                     recentOriginalConnections = mutableListOf()
                     alert(response.error.message)
                 }
             }
         }
     }*/

    fun updateContacts(allContacts: MutableList<TaskMemberDetail>) {
        _allConnections.postValue(allContacts as MutableList<TaskMemberDetail>?)
    }

    fun updateOriginalContacts(allContacts: MutableList<TaskMemberDetail>) {
        originalConnections = allContacts
    }

    fun updateRecentContacts(allContacts: MutableList<TaskMemberDetail>) {
        _recentAllConnections.postValue(allContacts as MutableList<TaskMemberDetail>?)
    }

    fun updateRecentOriginalContacts(allContacts: MutableList<TaskMemberDetail>) {
        recentOriginalConnections = allContacts
    }

    fun filterContacts(search: String) {
        if (search.isEmpty()) {
            if (originalConnections.isNotEmpty()) {
                _allConnections.postValue(originalConnections as MutableList<TaskMemberDetail>?)
            }

            return
        }
        val filtered = originalConnections.filter {
            (!it.firstName.isNullOrEmpty() && it.surName.lowercase()
                .contains(search, true)) ||
                    (!it.firstName.isNullOrEmpty() && it.surName.lowercase()
                        .contains(search, true)) ||
                    (!it.firstName.isNullOrEmpty() && it.surName.lowercase()
                        .contains(search, true))
        }
        if (filtered.isNotEmpty())
            _allConnections.postValue(filtered as MutableList<TaskMemberDetail>?)
        else
            _allConnections.postValue(mutableListOf())
    }

    fun filterRecentContacts(search: String) {
        if (search.isEmpty()) {
            if (recentOriginalConnections.isNotEmpty()) {
                _recentAllConnections.postValue(recentOriginalConnections as MutableList<TaskMemberDetail>?)
            }
            return
        }
        val filtered = recentOriginalConnections.filter {
            (!it.firstName.isNullOrEmpty() && it.surName.lowercase()
                .contains(search, true)) ||
                    (!it.firstName.isNullOrEmpty() && it.surName.lowercase()
                        .contains(search, true)) ||
                    (!it.firstName.isNullOrEmpty() && it.surName.lowercase()
                        .contains(search, true))
        }
        if (filtered.isNotEmpty())
            _recentAllConnections.postValue(filtered as MutableList<TaskMemberDetail>?)
        else
            _recentAllConnections.postValue(mutableListOf())
    }

    fun List<TaskMemberDetail>.groupDataByFirstLetter(): List<TaskMemberDetail> {
        val groupedData = this.groupBy {
            if (it.firstName.firstOrNull()?.isLetter() == true) {
                it.firstName.first().lowercase()
            } else {
                '#'.toString()
            }
        }.toSortedMap(
            compareBy<String> { it != "#" }
                .then(compareBy { it.lowercase() })
                .then(compareByDescending { it == "#" })
        )

        val sortedItems = mutableListOf<TaskMemberDetail>()
        for (mapKey in groupedData.keys) {
            val sortedGroupItems =
                groupedData[mapKey]?.sortedBy { it.firstName.lowercase() }
                    ?: emptyList()
            sortedItems.addAll(sortedGroupItems)
        }

        return sortedItems
    }

    fun updateAllConnections() {
        val list = ArrayList<TaskMemberDetail>()
        list.clear()
        val allConnections = _allConnections.value
        val recentConnections = contactsToRemoveFromAll
        if (recentConnections.isEmpty() || allConnections == null) {
            return
        }

        val connectionsToRemove = mutableListOf<TaskMemberDetail>()

        allConnections.forEach { connectionFromAllList ->
            recentConnections.forEach { recent ->
                if (recent.phoneNumber == connectionFromAllList.phoneNumber) {
                    connectionsToRemove.add(connectionFromAllList)
                }
            }
        }

        // Remove the connections after the iteration is complete
        allConnections.removeAll(connectionsToRemove)
        if (allConnections.isNullOrEmpty()) {
            _allConnections.value = mutableListOf()
        } else {
            _allConnections.value = allConnections!!
        }

    }

}
