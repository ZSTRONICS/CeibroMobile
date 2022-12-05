package com.zstronics.ceibro.ui.signup

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

    override fun onSignUp() {
        doSignUp(viewState.firstName.value.toString(), viewState.surname.value.toString(), viewState.email.value.toString(),
            viewState.password.value.toString(), viewState.confirmPassword.value.toString())
    }

    override fun doSignUp(
        firstName: String,
        surname: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        val request = SignUpRequest(firstName = firstName, surName = surname, email = email, password = confirmPassword)
        launch {
            loading(true)
            when (val response = repository.signup(request)) {

                is ApiResponse.Success -> {
                    loading(false, response.data.message)
                    clickEvent?.postValue(112)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }
}