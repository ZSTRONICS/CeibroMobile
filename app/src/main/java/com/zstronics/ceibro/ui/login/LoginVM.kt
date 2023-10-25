package com.zstronics.ceibro.ui.login

import android.os.Build
import com.onesignal.OneSignal
import com.zstronics.ceibro.base.KEY_User_Last_Login_Time
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.login.LoginRequest
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.resourses.IResourceProvider
import com.zstronics.ceibro.ui.contacts.toLightContacts
import com.zstronics.ceibro.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LoginVM @Inject constructor(
    override val viewState: LoginState,
    override var validator: Validator?,
    private val repository: IAuthRepository,
    private val taskRepository: ITaskRepository,
    private val sessionManager: SessionManager,
    private val resourceProvider: IResourceProvider,
    private val connectionsV2Dao: ConnectionsV2Dao,
    val dashboardRepository: IDashboardRepository
) : HiltBaseViewModel<ILogin.State>(), ILogin.ViewModel, IValidator {

//    val service = RetroNetwork().createService(AuthRepositoryService::class.java)
//    val authRepo = AuthRepository(service)


    override fun doLogin(
        phoneNumber: String,
        password: String,
        rememberMe: Boolean,
        onLoggedIn: () -> Unit
    ) {
        val deviceInfo = StringBuilder()
        val manufacturer = Build.MANUFACTURER
        deviceInfo.append("$manufacturer ")
        val model = Build.MODEL
        deviceInfo.append("$model")
        println("DeviceInfo: ${deviceInfo.toString()}")

        val request = LoginRequest(phoneNumber = phoneNumber, password = password)
        launch {
            loading(true)
            when (val response = repository.login(request)) {

                is ApiResponse.Success -> {
                    val secureUUID = UUID.randomUUID()
                    CookiesManager.deviceType = deviceInfo.toString()
                    CookiesManager.secureUUID = secureUUID.toString()
                    sessionManager.startUserSession(
                        response.data.user,
                        response.data.tokens,
                        "",
                        rememberMe,
                        secureUUID.toString(),
                        deviceInfo.toString()
                    )
                    OneSignal.setExternalUserId(response.data.user.id)
                    OneSignal.disablePush(false)        //Running setSubscription() operation inside this method (a hack)
                    OneSignal.pauseInAppMessages(false)

                    val currentUTCTime =
                        DateUtils.getCurrentUTCDateTime(DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC)
                    sessionManager.saveStringValue(KEY_User_Last_Login_Time, currentUTCTime)
                    if (sessionManager.getUpdatedAtTimeStamp().isEmpty()) {
                        sessionManager.saveUpdatedAtTimeStamp(response.data.user.createdAt)
                    }
                    if (response.data.user.autoContactSync) {
                        onLoggedIn.invoke()
                    } else {
                        getSavedContactsToStoreInSharePreference(onLoggedIn)
                    }
                    loading(false, "Login successful")
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }


//    override fun getTaskWithUpdatedTimeStamp(
//        timeStamp: String,
//        onLoggedIn: () -> Unit
//    ) {
//
//        launch {
//            loading(true)
//
//            taskRepository.getTaskWithUpdatedTimeStamp(timeStamp) { isSuccess, taskEvents, message ->
//                if (isSuccess) {
//
//                } else {
//
//                }
//            }
//        }
//    }

    private fun getSavedContactsToStoreInSharePreference(callBack: () -> Unit) {
        val userId = sessionManager.getUser().value?.id
        launch {
            when (val response = dashboardRepository.getAllConnectionsV2()) {

                is ApiResponse.Success -> {
                    val contacts = response.data.contacts
                    sessionManager.saveSyncedContacts(contacts.toLightContacts())
                    connectionsV2Dao.insertAll(response.data.contacts)
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