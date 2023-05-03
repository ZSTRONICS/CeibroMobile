package com.zstronics.ceibro.ui.login

import com.onesignal.OneSignal
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.login.LoginRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.resourses.IResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginVM @Inject constructor(
    override val viewState: LoginState,
    override var validator: Validator?,
    private val repository: IAuthRepository,
    private val sessionManager: SessionManager,
    private val resourceProvider: IResourceProvider
) : HiltBaseViewModel<ILogin.State>(), ILogin.ViewModel, IValidator {

//    val service = RetroNetwork().createService(AuthRepositoryService::class.java)
//    val authRepo = AuthRepository(service)


    override fun doLogin(phoneNumber: String, password: String, rememberMe: Boolean, onLoggedIn: () -> Unit) {

        val request = LoginRequest(phoneNumber = phoneNumber, password = password)
        launch {
            loading(true)
            when (val response = repository.login(request)) {

                is ApiResponse.Success -> {
                    sessionManager.startUserSession(
                        response.data.user,
                        response.data.tokens,
                        "",
                        rememberMe
                    )
                    OneSignal.setExternalUserId(response.data.user.id)
                    OneSignal.disablePush(false)        //Running setSubscription() operation inside this method (a hack)
                    OneSignal.pauseInAppMessages(false)
                    loading(false, "Login successful")
                    onLoggedIn.invoke()
                    if (response.data.user.autoContactSync) {
                        startContactSyncWorker(resourceProvider.context)
                    }
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }
}