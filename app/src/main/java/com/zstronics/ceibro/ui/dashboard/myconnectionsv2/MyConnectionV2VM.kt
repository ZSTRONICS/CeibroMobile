package com.zstronics.ceibro.ui.dashboard.myconnectionsv2

import android.os.Build
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.extensions.getLocalContacts
import com.zstronics.ceibro.resourses.IResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class MyConnectionV2VM @Inject constructor(
    override val viewState: MyConnectionV2State,
    val sessionManager: SessionManager,
    val dashboardRepository: IDashboardRepository,
    val resProvider: IResourceProvider,
    private val connectionsV2Dao: ConnectionsV2Dao
) : HiltBaseViewModel<IMyConnectionV2.State>(), IMyConnectionV2.ViewModel {
    var user = sessionManager.getUser().value
    private var _allConnections: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>?> =
        MutableLiveData()
    val allConnections: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>?> =
        _allConnections
    var originalConnections = listOf<AllCeibroConnections.CeibroConnection>()

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        viewState.isAutoSyncEnabled.postValue(user?.autoContactSync)

        val deviceInfo = StringBuilder()
        val manufacturer = Build.MANUFACTURER
        deviceInfo.append("$manufacturer ")
        val model = Build.MODEL
        deviceInfo.append("$model")
        viewState.deviceInfo = deviceInfo.toString()
    }

    fun getAllConnectionsV2(callBack: () -> Unit) {
        val userId = user?.id
        println("PhoneNumber-MyConnectionV2VM- getConnections for: $user")
        launch {
            val connectionsData = connectionsV2Dao.getAll()
            viewState.dbContactsSize = connectionsData.size
            val contacts = connectionsData.groupDataByFirstLetter().toMutableList()
            originalConnections = contacts
            _allConnections.postValue(contacts)
            callBack.invoke()
        }
    }

    fun syncContactsEnabled(
        onSuccess: () -> Unit
    ) {
        val phone = sessionManager.getUser().value?.phoneNumber
//        val phone = "+923120619435"
        launch {
            // Handle the API response
            println("PhoneNumber-MyConnectionV2VM-SyncEnable: true, for: $phone")
            loading(true)
            when (val response =
                dashboardRepository.syncContactsEnabled(phone ?: "", enabled = true)) {
                is ApiResponse.Success -> {
                    val userObj = sessionManager.getUserObj()
                    userObj?.autoContactSync = true
                    userObj?.let { sessionManager.updateUser(userObj = it) }

                    loading(false, "")
                    onSuccess.invoke()
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

    fun syncContactsDisable(
        onSuccess: () -> Unit
    ) {
        val phone = sessionManager.getUser().value?.phoneNumber
        launch {
            println("PhoneNumber-MyConnectionV2VM-SyncEnable: false, for: $phone")
            loading(true)
            when (val response =
                dashboardRepository.syncContactsEnabled(phone ?: "", enabled = false)) {
                is ApiResponse.Success -> {
                    val userObj = sessionManager.getUserObj()
                    userObj?.autoContactSync = false
                    userObj?.let { sessionManager.updateUser(userObj = it) }

                    loading(false, "")
                    onSuccess.invoke()
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
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

    data class CeibroConnectionGroup(
        val sectionLetter: Char,
        val items: List<AllCeibroConnections.CeibroConnection>
    )
}