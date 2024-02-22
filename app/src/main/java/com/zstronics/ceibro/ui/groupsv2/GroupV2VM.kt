package com.zstronics.ceibro.ui.groupsv2

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ConnectionGroupV2Dao
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.ConnectionGroupUpdateWithoutNameRequest
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.DeleteGroupInBulkRequest
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.NewConnectionGroupRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupV2VM @Inject constructor(
    override val viewState: GroupV2State,
    val sessionManager: SessionManager,
    val dashboardRepository: IDashboardRepository,
    val taskDao: TaskV2Dao,
    val connectionsV2Dao: ConnectionsV2Dao,
    val connectionGroupV2Dao: ConnectionGroupV2Dao
) : HiltBaseViewModel<IGroupV2.State>(), IGroupV2.ViewModel {


    private val _connectionGroups: MutableLiveData<MutableList<CeibroConnectionGroupV2>> =
        MutableLiveData()
    val connectionGroups: MutableLiveData<MutableList<CeibroConnectionGroupV2>> = _connectionGroups


    private val _filteredGroups: MutableLiveData<MutableList<CeibroConnectionGroupV2>> =
        MutableLiveData()
    val filteredGroups: MutableLiveData<MutableList<CeibroConnectionGroupV2>> = _filteredGroups


    var originalConnectionGroups: MutableList<CeibroConnectionGroupV2> = mutableListOf()

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        getAllConnectionGroups()
    }

    private fun getAllConnectionGroups() {
        launch {
            val groups = connectionGroupV2Dao.getAllConnectionGroup()
            _connectionGroups.postValue(groups.toMutableList())
            originalConnectionGroups = groups.toMutableList()
        }
    }

    fun createConnectionGroup(
        name: String,
        contacts: List<String>,
        callBack: (createdGroup: CeibroConnectionGroupV2) -> Unit
    ) {
        val requestBody = NewConnectionGroupRequest(
            name = name,
            contacts = contacts
        )
        loading(true)
        launch {
            when (val response = dashboardRepository.createConnectionGroup(requestBody)) {
                is ApiResponse.Success -> {
                    val createdGroup = response.data
                    connectionGroupV2Dao.insertConnectionGroup(createdGroup)
                    loading(false, "Group created")
                    callBack.invoke(createdGroup)
                }

                is ApiResponse.Error -> {
                    loading(false, "Error: ${response.error.message}")
                }
            }
        }
    }

    fun updateConnectionGroup(
        item: CeibroConnectionGroupV2,
        groupName: String,
        contacts: List<String>,
        groupNameChanged: Boolean,
        callBack: (createdGroup: CeibroConnectionGroupV2) -> Unit
    ) {
        loading(true)
        if (groupNameChanged) {
            val requestBody = NewConnectionGroupRequest(
                name = groupName,
                contacts = contacts
            )
            launch {
                when (val response =
                    dashboardRepository.updateConnectionGroup(item._id, requestBody)) {
                    is ApiResponse.Success -> {
                        val createdGroup = response.data
                        connectionGroupV2Dao.insertConnectionGroup(createdGroup)
                        loading(false, "Group updated")
                        callBack.invoke(createdGroup)
                    }

                    is ApiResponse.Error -> {
                        loading(false, "Error: ${response.error.message}")
                    }
                }
            }
        } else {
            val requestBody1 = ConnectionGroupUpdateWithoutNameRequest(
                contacts = contacts
            )
            launch {
                when (val response =
                    dashboardRepository.updateConnectionGroupWithoutName(item._id, requestBody1)) {
                    is ApiResponse.Success -> {
                        val createdGroup = response.data
                        connectionGroupV2Dao.insertConnectionGroup(createdGroup)
                        loading(false, "Group updated")
                        callBack.invoke(createdGroup)
                    }

                    is ApiResponse.Error -> {
                        loading(false, "Error: ${response.error.message}")
                    }
                }
            }
        }
    }

    fun deleteConnectionGroup(groupId: String, callBack: () -> Unit) {
        loading(true)
        launch {
            when (val response = dashboardRepository.deleteConnectionGroup(groupId)) {
                is ApiResponse.Success -> {
                    connectionGroupV2Dao.deleteConnectionGroupById(groupId)
                    loading(false, response.data.message)
                    callBack.invoke()
                }

                is ApiResponse.Error -> {
                    loading(false, "Error: ${response.error.message}")
                }
            }
        }
    }

    fun deleteConnectionGroupsInBulk(
        groups: ArrayList<CeibroConnectionGroupV2>,
        callBack: (List<String>) -> Unit
    ) {
        loading(true)
        launch {
            val list = groups.map { it._id }
            val delRequest = DeleteGroupInBulkRequest(list)
            when (val response = dashboardRepository.deleteConnectionGroupInBulk(delRequest)) {
                is ApiResponse.Success -> {
                    list.forEach {
                        connectionGroupV2Dao.deleteConnectionGroupById(it)
                    }
                    loading(false, response.data.message)
                    callBack.invoke(list)
                }

                is ApiResponse.Error -> {
                    loading(false, "Error: ${response.error.message}")
                }
            }
        }
    }

    fun filterGroups(search: String) {
        if (search.isEmpty()) {
            if (originalConnectionGroups.isNotEmpty()) {
                _filteredGroups.postValue(originalConnectionGroups)
            }
            return
        }
        val filtered = originalConnectionGroups.filter {
            (it.name.isNotEmpty() && it.name.lowercase().contains(search, true))
        }
        if (filtered.isNotEmpty())
            _filteredGroups.postValue(filtered.toMutableList())
        else
            _filteredGroups.postValue(mutableListOf())
    }

}