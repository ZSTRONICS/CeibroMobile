package com.zstronics.ceibro.ui.signup

import android.os.Build
import android.os.Bundle
import com.onesignal.OneSignal
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.signup.SignUpRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SignUpVM @Inject constructor(
    override val viewState: SignUpState,
    override var validator: Validator?,
    private val repository: IAuthRepository,
    val sessionManager: SessionManager
) : HiltBaseViewModel<ISignUp.State>(), ISignUp.ViewModel, IValidator {


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        with(viewState) {
            phoneNumber.value = bundle?.getString("phoneNumber")
            phoneCode.value = bundle?.getString("phoneCode")
        }
//        doSignUp(viewState.firstName.value.toString(), viewState.surname.value.toString(), viewState.email.value.toString(),
//            viewState.password.value.toString(), viewState.confirmPassword.value.toString())
    }

    override fun doSignUp(
        firstName: String,
        surname: String,
        email: String,
        companyName: String,
        jobTitle: String,
        password: String,
        onSignedUp: () -> Unit
    ) {
        val deviceInfo = StringBuilder()
        val manufacturer = Build.MANUFACTURER
        deviceInfo.append("$manufacturer ")
        val model = Build.MODEL
        deviceInfo.append("$model")

        val request = SignUpRequest(
            firstName = firstName,
            surName = surname,
            email = email,
            companyName = companyName,
            jobTitle = jobTitle,
            password = password
        )
        launch {
            loading(true)
            when (val response =
                repository.signup(viewState.phoneNumber.value.toString(), request)) {

                is ApiResponse.Success -> {
                    val secureUUID = UUID.randomUUID()
                    CookiesManager.deviceType = deviceInfo.toString()
                    CookiesManager.secureUUID = secureUUID.toString()
                    sessionManager.startUserSession(
                        response.data.user,
                        response.data.tokens,
                        "",
                        true,
                        secureUUID.toString(),
                        deviceInfo.toString()
                    )
                    OneSignal.setExternalUserId(response.data.user.id)
                    OneSignal.disablePush(false)        //Running setSubscription() operation inside this method (a hack)
                    OneSignal.pauseInAppMessages(false)
                    if (sessionManager.getUpdatedAtTimeStamp().isEmpty()) {
                        sessionManager.saveUpdatedAtTimeStamp(response.data.user.createdAt)
                    }
                    loading(false, "Profile setup complete")
                    onSignedUp.invoke()
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }
}