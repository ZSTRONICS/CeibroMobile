package com.zstronics.ceibro.ui.dashboard.myconnectionsv2

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.extensions.getLocalContacts
import com.zstronics.ceibro.resourses.IResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyConnectionV2VM @Inject constructor(
    override val viewState: MyConnectionV2State,
    val sessionManager: SessionManager,
    val dashboardRepository: IDashboardRepository,
    private val resProvider: IResourceProvider,
    private val connectionsV2Dao: ConnectionsV2Dao
) : HiltBaseViewModel<IMyConnectionV2.State>(), IMyConnectionV2.ViewModel {
    val user = sessionManager.getUser().value
    private var _allConnections: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>?> =
        MutableLiveData()
    val allConnections: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>?> =
        _allConnections
    var originalConnections = listOf<AllCeibroConnections.CeibroConnection>()

    private var _allGroupedConnections: MutableLiveData<MutableList<CeibroConnectionGroup>> =
        MutableLiveData()
    val allGroupedConnections: MutableLiveData<MutableList<CeibroConnectionGroup>> =
        _allGroupedConnections

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        viewState.isAutoSyncEnabled.postValue(user?.autoContactSync)
    }

    fun getAllConnectionsV2(callBack: () -> Unit) {
        val userId = sessionManager.getUser().value?.id
        launch {
            val connectionsData = connectionsV2Dao.getAll()
            if (connectionsData != null) {
                val contacts = connectionsData.contacts.sortedByDescending { it.isCeiborUser }
                originalConnections = contacts
                if (contacts.isNotEmpty()) {
                    _allConnections.postValue(contacts as MutableList<AllCeibroConnections.CeibroConnection>?)
                }
                callBack.invoke()
            } else {
                when (val response = dashboardRepository.getAllConnectionsV2(userId ?: "")) {
                    is ApiResponse.Success -> {
                        val contacts = response.data.contacts.sortedByDescending { it.isCeiborUser }
                        callBack.invoke()
                        originalConnections = contacts
                        if (contacts.isNotEmpty()) {
                            _allConnections.postValue(contacts as MutableList<AllCeibroConnections.CeibroConnection>?)
                        }
                    }

                    is ApiResponse.Error -> {
                        callBack.invoke()
                        alert(response.error.message)
                    }
                }
            }
        }
    }

    private fun syncContacts(
        selectedContacts: List<SyncContactsRequest.CeibroContactLight>,
        onSuccess: () -> Unit
    ) {
        val userId = sessionManager.getUser().value?.id
        launch {
            val request = SyncContactsRequest(contacts = selectedContacts)
            // Handle the API response
            loading(true)
            when (val response =
                dashboardRepository.syncContacts(userId ?: "", request)) {
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
        onSuccess: () -> Unit
    ) {
        val phone = sessionManager.getUser().value?.phoneNumber
//        val phone = "+923120619435"
        launch {
            // Handle the API response
            when (val response =
                dashboardRepository.syncContactsEnabled(phone ?: "", enabled = true)) {
                is ApiResponse.Success -> {
                    val contacts = getLocalContacts(resProvider.context)
                    val userObj = sessionManager.getUserObj()
                    userObj?.autoContactSync = true
                    userObj?.let { sessionManager.updateUser(userObj = it) }
                    syncContacts(contacts, onSuccess)
                }
                is ApiResponse.Error -> {
                    alert(response.error.message)
                }
            }
        }
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
                .contains(search.trim(), true)) ||
                    (!it.contactFirstName.isNullOrEmpty() && it.contactFirstName.lowercase()
                        .contains(search.trim(), true)) ||
                    (!it.contactSurName.isNullOrEmpty() && it.contactSurName.lowercase()
                        .contains(search.trim(), true)) ||
                    it.phoneNumber.contains(search.trim()) ||
                    (it.userCeibroData?.companyName != null && it.userCeibroData.companyName.lowercase()
                        .contains(search.trim(), true)) ||
                    (it.userCeibroData?.jobTitle != null && it.userCeibroData.jobTitle.lowercase()
                        .contains(search.trim(), true))
        }
        if (filtered.isNotEmpty())
            _allConnections.postValue(filtered as MutableList<AllCeibroConnections.CeibroConnection>?)
        else
            _allConnections.postValue(mutableListOf())
    }

    fun groupDataByFirstLetter(data: List<AllCeibroConnections.CeibroConnection>) {
        val sections = mutableListOf<CeibroConnectionGroup>()

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
                CeibroConnectionGroup(
                    mapKey.toString().uppercase()[0],
                    groupedData[mapKey]?.sortedBy { it.contactFirstName?.lowercase() }
                        ?: emptyList()
                )
            )
        }
        _allGroupedConnections.value = sections
    }

    data class CeibroConnectionGroup(
        val sectionLetter: Char,
        val items: List<AllCeibroConnections.CeibroConnection>
    )
}