package com.zstronics.ceibro.ui.tasks.v2.newtask.assignee

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AssigneeVM @Inject constructor(
    override val viewState: AssigneeState,
    val sessionManager: SessionManager,
    val dashboardRepository: IDashboardRepository,
    private val connectionsV2Dao: ConnectionsV2Dao
) : HiltBaseViewModel<IAssignee.State>(), IAssignee.ViewModel {
    val user = sessionManager.getUser().value

    private var _allConnections: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        MutableLiveData()
    val allConnections: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        _allConnections
    var originalConnections = listOf<AllCeibroConnections.CeibroConnection>()
    private var _recentAllConnections: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>?> =
        MutableLiveData(mutableListOf())
    val recentAllConnections: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>?> =
        _recentAllConnections
    var recentOriginalConnections = listOf<AllCeibroConnections.CeibroConnection>()

    var selectedContacts: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        MutableLiveData()
    var isConfirmer: MutableLiveData<Boolean> = MutableLiveData(false)
    var isViewer: MutableLiveData<Boolean> = MutableLiveData(false)


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
        isViewer?.let {
            this.isViewer.value = it
        } ?: kotlin.run {
            this.isViewer.value = false
        }
        val selectedContact = bundle?.getParcelableArray("contacts")
        val selectedContactList =
            selectedContact?.map { it as AllCeibroConnections.CeibroConnection }
                ?.toMutableList()
        if (!selectedContactList.isNullOrEmpty()) {
            selectedContacts.postValue(selectedContactList as MutableList<AllCeibroConnections.CeibroConnection>?)
        }
        val handler = Handler()
        handler.postDelayed(Runnable {
            if (selfAssigned != null) {
                viewState.isSelfAssigned.value = selfAssigned
            }
        }, 50)

    }

    fun getAllConnectionsV2(callBack: () -> Unit) {
        loadRecentConnections()
        launch {
            val connectionsData = connectionsV2Dao.getAll()
            if (isConfirmer.value == true || isViewer.value == true) {
                val list = connectionsData.filter { it.isCeiborUser }
                processConnectionsData(list, callBack)
            } else {
                processConnectionsData(connectionsData, callBack)
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
    }

    private fun loadRecentConnections() {
        launch {
            when (val response = dashboardRepository.getRecentCeibroConnections()) {
                is ApiResponse.Success -> {
                    val newItemsList =
                        response.data.recentContacts.distinctBy { it.id } // Your 10 items here

                    val allContacts = newItemsList.groupDataByFirstLetter().toMutableList()
                    val oldSelectedContacts = selectedContacts.value

                    if (!oldSelectedContacts.isNullOrEmpty()) {
                        oldSelectedContacts.forEach { oldContact ->
                            val matchingContact = allContacts.find { it.id == oldContact.id }
                            matchingContact?.isChecked = true
                        }
                        _recentAllConnections.postValue(allContacts)
                        recentOriginalConnections = allContacts
                    } else {
                        if (allContacts.isNotEmpty()) {
                            recentOriginalConnections = allContacts
                            _recentAllConnections.value = allContacts
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
    }

    fun updateContacts(allContacts: MutableList<AllCeibroConnections.CeibroConnection>) {
        _allConnections.postValue(allContacts as MutableList<AllCeibroConnections.CeibroConnection>?)
    }

    fun updateOriginalContacts(allContacts: MutableList<AllCeibroConnections.CeibroConnection>) {
        originalConnections = allContacts
    }

    fun updateRecentContacts(allContacts: MutableList<AllCeibroConnections.CeibroConnection>) {
        _recentAllConnections.postValue(allContacts as MutableList<AllCeibroConnections.CeibroConnection>?)
    }

    fun updateRecentOriginalContacts(allContacts: MutableList<AllCeibroConnections.CeibroConnection>) {
        recentOriginalConnections = allContacts
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

    fun filterRecentContacts(search: String) {
        if (search.isEmpty()) {
            if (recentOriginalConnections.isNotEmpty()) {
                _recentAllConnections.postValue(recentOriginalConnections as MutableList<AllCeibroConnections.CeibroConnection>?)
            }
            return
        }
        val filtered = recentOriginalConnections.filter {
            (!it.contactFullName.isNullOrEmpty() && it.contactFullName.lowercase()
                .contains(search, true)) ||
                    (!it.contactFirstName.isNullOrEmpty() && it.contactFirstName.lowercase()
                        .contains(search, true)) ||
                    (!it.contactSurName.isNullOrEmpty() && it.contactSurName.lowercase()
                        .contains(search, true))
        }
        if (filtered.isNotEmpty())
            _recentAllConnections.postValue(filtered as MutableList<AllCeibroConnections.CeibroConnection>?)
        else
            _recentAllConnections.postValue(mutableListOf())
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

    companion object {
        fun printCurrentTimeWithSeconds(line: Int) {
            val currentTime = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val dateString = sdf.format(Date(currentTime))
            Log.d("getAllConnectionsV2", "Line $line Time: $dateString")
        }
    }
}