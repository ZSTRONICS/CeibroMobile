package com.zstronics.ceibro.ui.groupsv2

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ConnectionGroupV2Dao
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.InboxV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.inbox.CeibroInboxV2
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.NewConnectionGroupRequest
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.v2.TaskSeenResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.contacts.toLightContacts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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


    fun createConnectionGroup(name: String, contacts: List<String>, callBack: () -> Unit) {
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
                    callBack.invoke()
                }

                is ApiResponse.Error -> {
                    loading(false, "Error: ${response.error.message}")
                }
            }
        }
    }

}