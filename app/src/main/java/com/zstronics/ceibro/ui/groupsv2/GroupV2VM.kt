package com.zstronics.ceibro.ui.groupsv2

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ConnectionGroupV2Dao
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.AssignedToState
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AssignedToStateNewEntity
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.ConnectionGroupUpdateWithoutNameRequest
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CreateGroupRequest
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.DeleteGroupInBulkRequest
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
    val user = sessionManager.getUser().value

    private val _connectionGroups: MutableLiveData<MutableList<CeibroConnectionGroupV2>> =
        MutableLiveData()
    val connectionGroups: MutableLiveData<MutableList<CeibroConnectionGroupV2>> = _connectionGroups

    private val _connections: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        MutableLiveData()
    val connections: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        _connections


    private val _filteredGroups: MutableLiveData<MutableList<CeibroConnectionGroupV2>> =
        MutableLiveData()
    val filteredGroups: MutableLiveData<MutableList<CeibroConnectionGroupV2>> = _filteredGroups


    var originalConnectionGroups: MutableList<CeibroConnectionGroupV2> = mutableListOf()


    var adminSelfAssigned: MutableLiveData<Boolean> = MutableLiveData(false)
    var adminSelectedContacts: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        MutableLiveData()
    var adminAssignToText: MutableLiveData<String> = MutableLiveData()

    var assigneeSelfAssigned: MutableLiveData<Boolean> = MutableLiveData(false)
    var assigneeSelectedContacts: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        MutableLiveData()
    var assigneeAssignToText: MutableLiveData<String> = MutableLiveData()

    var confirmerSelfAssigned: MutableLiveData<Boolean> = MutableLiveData(false)
    var confirmerSelectedContacts: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        MutableLiveData()
    var confirmerAssignToText: MutableLiveData<String> = MutableLiveData()

    var viewerSelfAssigned: MutableLiveData<Boolean> = MutableLiveData(false)
    var viewerSelectedContacts: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        MutableLiveData()
    var viewerAssignToText: MutableLiveData<String> = MutableLiveData()

    var shareSelfAssigned: MutableLiveData<Boolean> = MutableLiveData(false)
    var shareSelectedContacts: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        MutableLiveData()
    var shareAssignToText: MutableLiveData<String> = MutableLiveData()

    var selectedContacts: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> =
        MutableLiveData()

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        getAllConnectionGroups()
    }

    private fun getAllConnectionGroups() {
        launch {
            val connections = connectionsV2Dao.getAll()
            _connections.postValue(connections.toMutableList())
            val groups = connectionGroupV2Dao.getAllConnectionGroup()
            _connectionGroups.postValue(groups.toMutableList())
            originalConnectionGroups = groups.toMutableList()
        }
    }

    fun createConnectionGroup(
        name: String,
        callBack: (createdGroup: CeibroConnectionGroupV2) -> Unit
    ) {


        val groupName = name
        val contacts = ArrayList<String>()
        val viewerlist = ArrayList<String>()
        val confirmer = ArrayList<String>()
        val assignToState = ArrayList<AssignedToStateNewEntity>()
        val groupAdmins = ArrayList<String>()
        val sharedWith = ArrayList<String>()

        if (viewerSelfAssigned.value == true) {
            user?.id?.let {
                viewerlist.add(it)
            }
            user?.let { contacts.add(it.id) }
        }
        viewerSelectedContacts.value?.forEach {
            it.userCeibroData?.let { user ->
                viewerlist.add(user.id)
                contacts.add(user.id)
            }
        }
        if (confirmerSelfAssigned.value == true) {
            user?.id?.let {
                confirmer.add(it)
            }
            user?.let { contacts.add(it.id) }
        }
        confirmerSelectedContacts.value?.forEach {
            it.userCeibroData?.let { user ->
                confirmer.add(user.id)
                contacts.add(user.id)
            }

        }
        if (adminSelfAssigned.value == true) {
            user?.id?.let {
                groupAdmins.add(it)
            }
        }
        adminSelectedContacts.value?.forEach {
            it.userCeibroData?.id?.let { userId -> groupAdmins.add(userId) }
        }
        if (assigneeSelfAssigned.value == true) {
            user?.let {
                assignToState.add(
                    AssignedToStateNewEntity(
                        phoneNumber = it.phoneNumber,
                        userId = it.id
                    )
                )
                contacts.add(it.id)
            }
        }
        assigneeSelectedContacts.value?.forEach {

            it.userCeibroData?.let { user ->
                assignToState.add(
                    AssignedToStateNewEntity(
                        phoneNumber = user.phoneNumber,
                        userId = user.id,
                    )
                )
            }
            it.userCeibroData?.let { user -> contacts.add(user.id) }
        }

        if (shareSelfAssigned.value == true) {
            user?.id?.let {
                sharedWith.add(it)
            }
        }
        shareSelectedContacts.value?.forEach {
            it.userCeibroData?.let { user -> sharedWith.add(user.id) }
        }

        val requestBody = CreateGroupRequest(
            name = groupName,
            contacts = contacts.distinct(),
            viewer = viewerlist.distinct(),
            confirmer = confirmer.distinct(),
            assignedToState = assignToState.distinct(),
            groupAdmins = groupAdmins.distinct(),
            sharedWith = sharedWith.distinct(),
            isPublic = false,
            assosiatedProjects = emptyList()
        )
        loading(true)
        launch {
            when (val response = dashboardRepository.createConnectionGroup(requestBody)) {
                is ApiResponse.Success -> {
                    val createdGroup = response.data
                    connectionGroupV2Dao.insertConnectionGroup(createdGroup)
                    loading(false, "Group created")
                    callBack.invoke(createdGroup)
                    clearGroupData()
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
        isGroupNameSame: Boolean,
        callBack: (createdGroup: CeibroConnectionGroupV2) -> Unit
    ) {

        val groupName = groupName
        val contacts = ArrayList<String>()
        val viewerlist = ArrayList<String>()
        val confirmer = ArrayList<String>()
        val assignToState = ArrayList<AssignedToStateNewEntity>()
        val groupAdmins = ArrayList<String>()
        val sharedWith = ArrayList<String>()

        if (viewerSelfAssigned.value == true) {
            user?.id?.let {
                viewerlist.add(it)
            }
            user?.let { contacts.add(it.id) }
        }
        viewerSelectedContacts.value?.forEach {
            it.userCeibroData?.let { user ->
                viewerlist.add(user.id)
                contacts.add(user.id)
            }
        }
        if (confirmerSelfAssigned.value == true) {
            user?.id?.let {
                confirmer.add(it)
            }
            user?.let { contacts.add(it.id) }
        }
        confirmerSelectedContacts.value?.forEach {
            it.userCeibroData?.let { user ->
                confirmer.add(user.id)
                contacts.add(user.id)
            }

        }
        if (adminSelfAssigned.value == true) {
            user?.id?.let {
                groupAdmins.add(it)
            }
        }
        adminSelectedContacts.value?.forEach {
            it.userCeibroData?.id?.let { userId -> groupAdmins.add(userId) }
        }
        if (assigneeSelfAssigned.value == true) {
            user?.let {
                assignToState.add(
                    AssignedToStateNewEntity(
                        phoneNumber = it.phoneNumber,
                        userId = it.id
                    )
                )
                contacts.add(it.id)
            }
        }
        assigneeSelectedContacts.value?.forEach {

            it.userCeibroData?.let { user ->
                assignToState.add(
                    AssignedToStateNewEntity(
                        phoneNumber = user.phoneNumber,
                        userId = user.id,
                    )
                )
            }
            it.userCeibroData?.let { user -> contacts.add(user.id) }
        }

        if (shareSelfAssigned.value == true) {
            user?.id?.let {
                sharedWith.add(it)
            }
        }
        shareSelectedContacts.value?.forEach {
            it.userCeibroData?.let { user -> sharedWith.add(user.id) }
        }

        val requestBody = CreateGroupRequest(
            name = groupName,
            contacts = contacts.distinct(),
            viewer = viewerlist.distinct(),
            confirmer = confirmer.distinct(),
            assignedToState = assignToState.distinct(),
            groupAdmins = groupAdmins.distinct(),
            sharedWith = sharedWith.distinct(),
            isPublic = false,
            assosiatedProjects = emptyList()
        )

        val requestBody1 = ConnectionGroupUpdateWithoutNameRequest(
            contacts = contacts.distinct(),
            viewer = viewerlist.distinct(),
            confirmer = confirmer.distinct(),
            assignedToState = assignToState.distinct(),
            groupAdmins = groupAdmins.distinct(),
            sharedWith = sharedWith.distinct(),
            isPublic = false,
            assosiatedProjects = emptyList()
        )




        loading(true)
        if (!isGroupNameSame) {

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

    fun updateGroupData(data: CeibroConnectionGroupV2) {
        updateAdmin(data.groupAdmins)
        updateAssignTo(data.assignedToState)
        updateConfirmer(data.confirmer)
        updateViewer(data.viewer)
        updateShareWith(data.sharedWith)
    }

    private fun clearGroupData() {
        adminSelfAssigned.value = false
        adminSelectedContacts.value = mutableListOf()
        adminAssignToText.value = ""

        assigneeSelfAssigned.value = false
        assigneeSelectedContacts.value = mutableListOf()
        assigneeAssignToText.value = ""

        confirmerSelfAssigned.value = false
        confirmerSelectedContacts.value = mutableListOf()
        confirmerAssignToText.value = ""

        viewerSelfAssigned.value = false
        viewerSelectedContacts.value = mutableListOf()
        viewerAssignToText.value = ""

        shareSelfAssigned.value = false
        shareSelectedContacts.value = mutableListOf()
        shareAssignToText.value = ""

    }

    private fun updateAdmin(admin: List<TaskMemberDetail>) {
        val groupAdmins = ArrayList<AllCeibroConnections.CeibroConnection>()
        admin.let { groupAdminConnectionList ->
            if (groupAdminConnectionList.isNotEmpty()) {

                if (groupAdminConnectionList.find { it.id == user?.id } != null) {
                    adminSelfAssigned.value = true
                }

                connections.value?.forEach { connectionFromList ->
                    connectionFromList.userCeibroData?.let { userCeibroData ->
                        if (groupAdminConnectionList.find { it.id == userCeibroData.id } != null) {
                            groupAdmins.add(connectionFromList)
                        }
                    }
                }
            }
            var index = 0
            var assigneeMembers = ""
            if (adminSelfAssigned.value == true) {
                assigneeMembers += if (groupAdmins.isEmpty()) {
                    "Me"
                } else {
                    "Me; "
                }
            }

            for (item in groupAdmins) {
                assigneeMembers += if (index == groupAdmins.size - 1) {
                    "${item.contactFirstName} ${item.contactSurName}"
                } else {
                    "${item.contactFirstName} ${item.contactSurName}; "
                }
                index++
            }
            adminSelectedContacts.value = groupAdmins
            adminAssignToText.value = assigneeMembers

        }
    }

    private fun updateAssignTo(assignee: List<AssignedToState>) {
        val assigneeGroup = ArrayList<AllCeibroConnections.CeibroConnection>()
        assignee.let { groupassigneeConnectionList ->
            if (groupassigneeConnectionList.isNotEmpty()) {
                if (groupassigneeConnectionList.find { it.id == user?.id } != null) {
                    assigneeSelfAssigned.value = true
                }
                connections.value?.forEach { connectionFromList ->
                    connectionFromList.userCeibroData?.let { userCeibroData ->
                        if (groupassigneeConnectionList.find { it.id == userCeibroData.id } != null) {
                            assigneeGroup.add(connectionFromList)
                        }
                    }
                }
            }
            var index = 0
            var assigneeMembers = ""
            if (assigneeSelfAssigned.value == true) {
                assigneeMembers += if (assigneeGroup.isEmpty()) {
                    "Me"
                } else {
                    "Me; "
                }
            }

            for (item in assigneeGroup) {
                assigneeMembers += if (index == assigneeGroup.size - 1) {
                    "${item.contactFirstName} ${item.contactSurName}"
                } else {
                    "${item.contactFirstName} ${item.contactSurName}; "
                }
                index++
            }
            assigneeSelectedContacts.value = assigneeGroup
            assigneeAssignToText.value = assigneeMembers
        }
    }

    private fun updateConfirmer(confrmer: List<TaskMemberDetail>) {
        val confirmerGroup = ArrayList<AllCeibroConnections.CeibroConnection>()
        confrmer.let { groupassigneeConnectionList ->
            if (groupassigneeConnectionList.isNotEmpty()) {
                if (groupassigneeConnectionList.find { it.id == user?.id } != null) {
                    confirmerSelfAssigned.value = true
                }

                connections.value?.forEach { connectionFromList ->
                    connectionFromList.userCeibroData?.let { userCeibroData ->
                        if (groupassigneeConnectionList.find { it.id == userCeibroData.id } != null) {

                            confirmerGroup.add(connectionFromList)
                        }
                    }
                }
            }
            var index = 0
            var assigneeMembers = ""
            if (confirmerSelfAssigned.value == true) {
                assigneeMembers += if (confirmerGroup.isEmpty()) {
                    "Me"
                } else {
                    "Me; "
                }
            }

            for (item in confirmerGroup) {
                assigneeMembers += if (index == confirmerGroup.size - 1) {
                    "${item.contactFirstName} ${item.contactSurName}"
                } else {
                    "${item.contactFirstName} ${item.contactSurName}; "
                }
                index++
            }
            confirmerSelectedContacts.value = confirmerGroup
            confirmerAssignToText.value = assigneeMembers
        }
    }

    private fun updateViewer(viewer: List<TaskMemberDetail>) {
        val viewerGroup = ArrayList<AllCeibroConnections.CeibroConnection>()
        viewer.let { groupassigneeConnectionList ->
            if (groupassigneeConnectionList.isNotEmpty()) {

                if (groupassigneeConnectionList.find { it.id == user?.id } != null) {
                    viewerSelfAssigned.value = true
                }

                connections.value?.forEach { connectionFromList ->
                    connectionFromList.userCeibroData?.let { userCeibroData ->
                        if (groupassigneeConnectionList.find { it.id == userCeibroData.id } != null) {
                            viewerGroup.add(connectionFromList)
                        }
                    }
                }
            }
            var index = 0
            var assigneeMembers = ""
            if (viewerSelfAssigned.value == true) {
                assigneeMembers += if (viewerGroup.isEmpty()) {
                    "Me"
                } else {
                    "Me; "
                }
            }

            for (item in viewerGroup) {
                assigneeMembers += if (index == viewerGroup.size - 1) {
                    "${item.contactFirstName} ${item.contactSurName}"
                } else {
                    "${item.contactFirstName} ${item.contactSurName}; "
                }
                index++
            }
            viewerSelectedContacts.value = viewerGroup
            viewerAssignToText.value = assigneeMembers
        }
    }

    private fun updateShareWith(share: List<TaskMemberDetail>) {
        val shareGroup = ArrayList<AllCeibroConnections.CeibroConnection>()
        share.let { groupassigneeConnectionList ->
            if (groupassigneeConnectionList.isNotEmpty()) {
                if (groupassigneeConnectionList.find { it.id == user?.id } != null) {
                    shareSelfAssigned.value = true
                }

                connections.value?.forEach { connectionFromList ->
                    connectionFromList.userCeibroData?.let { userCeibroData ->
                        if (groupassigneeConnectionList.find { it.id == userCeibroData.id } != null) {
                            shareGroup.add(connectionFromList)
                        }
                    }
                }
            }
            var index = 0
            var assigneeMembers = ""
            if (shareSelfAssigned.value == true) {
                assigneeMembers += if (shareGroup.isEmpty()) {
                    "Me"
                } else {
                    "Me; "
                }
            }

            for (item in shareGroup) {
                assigneeMembers += if (index == shareGroup.size - 1) {
                    "${item.contactFirstName} ${item.contactSurName}"
                } else {
                    "${item.contactFirstName} ${item.contactSurName}; "
                }
                index++
            }
            shareSelectedContacts.value = shareGroup
            shareAssignToText.value = assigneeMembers
        }
    }
}