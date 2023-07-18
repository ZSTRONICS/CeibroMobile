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

    private var _allGroupedConnections: MutableLiveData<MutableList<ForwardConnectionGroup>> =
        MutableLiveData()
    val allGroupedConnections: MutableLiveData<MutableList<ForwardConnectionGroup>> =
        _allGroupedConnections

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
                processConnectionsData(connectionsData)
                callBack.invoke()
            } else {
                when (val response = dashboardRepository.getAllConnectionsV2(userId ?: "")) {
                    is ApiResponse.Success -> {
                        processConnectionsData(response.data.contacts)
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

    private fun processConnectionsData(contactsResponse: List<AllCeibroConnections.CeibroConnection>) {
        val allContacts = contactsResponse.sortedByDescending { it.isCeiborUser }.toMutableList()
        val oldSelectedContacts = selectedContacts.value
        if (!oldSelectedContacts.isNullOrEmpty()) {
            for (allItem in allContacts) {
                for (selectedItem in oldSelectedContacts) {
                    if (allItem.id == selectedItem.id) {
                        val index = allContacts.indexOf(allItem)
                        allContacts[index] = selectedItem
                    }
                }
            }
            _allConnections.postValue(allContacts)
            originalConnections = allContacts

        } else {
            if (allContacts.isNotEmpty()) {
                originalConnections = allContacts
                _allConnections.postValue(allContacts as MutableList<AllCeibroConnections.CeibroConnection>?)
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


    fun groupDataByFirstLetter(data: List<AllCeibroConnections.CeibroConnection>) {
        val sections = mutableListOf<ForwardConnectionGroup>()

        val groupedData = data.groupBy {
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

        for (mapKey in groupedData.keys) {
            sections.add(
                ForwardConnectionGroup(
                    mapKey.toString().uppercase()[0],
                    groupedData[mapKey]?.sortedBy { it.contactFirstName?.lowercase() }
                        ?: emptyList()
                )
            )
        }
        _allGroupedConnections.value = sections
    }

    data class ForwardConnectionGroup(
        val sectionLetter: Char,
        var items: List<AllCeibroConnections.CeibroConnection>
    )

}