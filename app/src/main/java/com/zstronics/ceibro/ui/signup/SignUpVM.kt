package com.zstronics.ceibro.ui.signup

import android.os.Bundle
import com.onesignal.OneSignal
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.signup.SignUpRequest
import com.zstronics.ceibro.data.repos.editprofile.EditProfileRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
        val request = SignUpRequest(firstName = firstName, surName = surname, email = email, companyName = companyName, jobTitle = jobTitle, password = password)
        launch {
            loading(true)
            when (val response = repository.signup(viewState.phoneNumber.value.toString(), request)) {

                is ApiResponse.Success -> {
                    sessionManager.startUserSession(
                        response.data.user,
                        response.data.tokens,
                        "",
                        false
                    )
                    OneSignal.setExternalUserId(response.data.user.id)
                    OneSignal.disablePush(false)        //Running setSubscription() operation inside this method (a hack)
                    OneSignal.pauseInAppMessages(false)
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