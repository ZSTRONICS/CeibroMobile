package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ForwardVM @Inject constructor(
    override val viewState: ForwardState,
    val sessionManager: SessionManager,
    val dashboardRepository: IDashboardRepository,
    private val connectionsV2Dao: ConnectionsV2Dao
) : HiltBaseViewModel<IForward.State>(), IForward.ViewModel {
    val user = sessionManager.getUser().value

    private var _allConnections: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>?> =
        MutableLiveData()
    val allConnections: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>?> =
        _allConnections
    var originalConnections = listOf<AllCeibroConnections.CeibroConnection>()

    var selectedContacts: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        MutableLiveData()

    var oldSelectedContacts: ArrayList<String> = arrayListOf()


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val selectedContact = bundle?.getStringArrayList("assignToContacts")
        if (!selectedContact.isNullOrEmpty()) {
            oldSelectedContacts = selectedContact
        }
    }

    fun getAllConnectionsV2(callBack: () -> Unit) {
        val userId = user?.id

        launch {
            val connectionsData = connectionsV2Dao.getAll()
            if (connectionsData.isNotEmpty()) {
                processConnectionsData(connectionsData, callBack)
                callBack.invoke()
            } else {
                when (val response = dashboardRepository.getAllConnectionsV2(userId ?: "")) {
                    is ApiResponse.Success -> {
                        processConnectionsData(response.data.contacts, callBack)
                        callBack.invoke()
                    }
                    is ApiResponse.Error -> {
                        callBack.invoke()
                        alert(response.error.message)
                    }
                }
            }
        }
    }

    private fun processConnectionsData(
        contactsResponse: List<AllCeibroConnections.CeibroConnection>,
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
            appendRecentConnections(callBack)
        } else {
            if (allContacts.isNotEmpty()) {
                originalConnections = allContacts
                _allConnections.value = allContacts
            }
            appendRecentConnections(callBack)
        }
    }

    private fun appendRecentConnections(callBack: () -> Unit) {
        launch {
            when (val response = dashboardRepository.getRecentCeibroConnections()) {
                is ApiResponse.Success -> {
                    val newItemsList = response.data.recentContacts // Your 10 items here
                    if (newItemsList.isNotEmpty()) {
                        val currentList: MutableList<AllCeibroConnections.CeibroConnection>? =
                            _allConnections.value
                        val updatedList: MutableList<AllCeibroConnections.CeibroConnection> =
                            currentList?.toMutableList() ?: mutableListOf()
                        updatedList.addAll(0, newItemsList)
                        _allConnections.value = updatedList
                        originalConnections = updatedList
                    }
                    callBack.invoke()
                }
                is ApiResponse.Error -> {
                    callBack.invoke()
                    alert(response.error.message)
                }
            }
        }
    }

    fun updateContacts(allContacts: MutableList<AllCeibroConnections.CeibroConnection>) {
        originalConnections = allContacts
        _allConnections.postValue(allContacts as MutableList<AllCeibroConnections.CeibroConnection>?)
    }


    fun filterContacts(search: String) {
        if (search.isEmpty()) {
            if (originalConnections.isNotEmpty()) {
                _allConnections.postValue(originalConnections as MutableList<AllCeibroConnections.CeibroConnection>?)
            }

            return
        }
        val filtered = originalConnections.filter {
            (!it.contactFullName.isNullOrEmpty() && it.contactFullName.lowercase()
                .contains(search, true)) ||
                    (!it.contactFirstName.isNullOrEmpty() && it.contactFirstName.lowercase()
                        .contains(search, true)) ||
                    (!it.contactSurName.isNullOrEmpty() && it.contactSurName.lowercase()
                        .contains(search, true))
        }
        if (filtered.isNotEmpty())
            _allConnections.postValue(filtered as MutableList<AllCeibroConnections.CeibroConnection>?)
        else
            _allConnections.postValue(mutableListOf())
    }

    fun List<AllCeibroConnections.CeibroConnection>.groupDataByFirstLetter(): List<AllCeibroConnections.CeibroConnection> {
        val groupedData = this.groupBy {
            if (it.contactFirstName?.firstOrNull()?.isLetter() == true) {
                it.contactFirstName.first().lowercase()
            } else {
                '#'.toString()
            }
        }.toSortedMap(
            compareBy<String> { it != "#" }
                .then(compareBy { it.lowercase() })
                .then(compareByDescending { it == "#" })
        )

        val sortedItems = mutableListOf<AllCeibroConnections.CeibroConnection>()
        for (mapKey in groupedData.keys) {
            val sortedGroupItems =
                groupedData[mapKey]?.sortedBy { it.contactFirstName?.lowercase() }
                    ?: emptyList()
            sortedItems.addAll(sortedGroupItems)
        }

        return sortedItems
    }

    data class ForwardConnectionGroup(
        val sectionLetter: Char,
        var items: List<AllCeibroConnections.CeibroConnection>
    )

}