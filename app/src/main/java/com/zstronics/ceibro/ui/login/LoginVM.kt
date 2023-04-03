package com.zstronics.ceibro.ui.login

import com.onesignal.OneSignal
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.login.LoginRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginVM @Inject constructor(
    override val viewState: LoginState,
    override var validator: Validator?,
    private val repository: IAuthRepository,
    private val sessionManager: SessionManager
) : HiltBaseViewModel<ILogin.State>(), ILogin.ViewModel, IValidator {

//    val service = RetroNetwork().createService(AuthRepositoryService::class.java)
//    val authRepo = AuthRepository(service)

    override fun onLogin() {
        doLogin(viewState.email.value.toString(), viewState.password.value.toString())
    }

    override fun doLogin(email: String, password: String) {

        val request = LoginRequest(email = email, password = password)
        launch {
            loading(true)
            when (val response = repository.login(request)) {

                is ApiResponse.Success -> {
                    sessionManager.startUserSession(
                        response.data.user,
                        response.data.tokens,
                        viewState.password.value.toString()
                    )
                    OneSignal.setExternalUserId(response.data.user.id)
                    OneSignal.disablePush(false)        //Running setSubscription() operation inside this method (a hack)
                    OneSignal.pauseInAppMessages(false)
                    loading(false, "Login successful")
                    clickEvent?.postValue(100)
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }
}