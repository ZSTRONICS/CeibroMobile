package com.zstronics.ceibro.data.base

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.repos.NotificationTaskData
import com.zstronics.ceibro.data.repos.auth.login.Tokens

object CookiesManager {
    var navigationGraphStartDestination: Int = 0
    var jwtToken: String? = null
    var isLoggedIn: Boolean = false
    var tokens: Tokens? = null
    var secureUUID: String? = null
    var deviceType: String? = null
    var androidId: String? = null
    var toMeNewTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    var toMeOngoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    var toMeDoneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    var fromMeUnreadTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    var fromMeOngoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    var fromMeDoneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    var hiddenCanceledTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    var hiddenOngoingTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    var hiddenDoneTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    var taskDataForDetails: CeibroTaskV2? = null
    var taskDetailEvents: List<Events>? = null
    var taskDetailRootState: String? = null
    var taskDetailSelectedSubState: String? = null
    var taskIdInDetails: String = ""
    val appFirstStartForSocket: MutableLiveData<Boolean> = MutableLiveData(true)
    val socketOnceConnected: MutableLiveData<Boolean> = MutableLiveData(false)
    var projectDataForDetails: CeibroProjectV2? = null
    var projectNameForDetails: String = ""
    var notificationDataContent: MutableList<Pair<NotificationTaskData, Int>> = mutableListOf()
}