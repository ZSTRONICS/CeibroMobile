package com.zstronics.ceibro.ui.profile.editprofile

import android.content.Context
import android.os.Handler
import androidx.core.net.toUri
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.editprofile.EditProfileRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@HiltViewModel
class EditProfileVM @Inject constructor(
    override val viewState: EditProfileState,
    override var validator: Validator?,
    private val repository: IAuthRepository,
    val sessionManager: SessionManager
) : HiltBaseViewModel<IEditProfile.State>(), IEditProfile.ViewModel, IValidator {

    init {
        EventBus.getDefault().register(this)
        sessionManager.setUser()
        with(viewState) {
            userFirstName.value = sessionManager.getUser().value?.firstName
            userSurname.value = sessionManager.getUser().value?.surName
            userEmail.value = sessionManager.getUser().value?.email
            userContactNumber.value = sessionManager.getUser().value?.phone
            userPassword.value = sessionManager.getPass()
            userConfirmPassword.value = sessionManager.getPass()
            userCompanyName.value = sessionManager.getUser().value?.companyName
            userCompanyVAT.value = sessionManager.getUser().value?.companyVat
            userCompanyLocation.value = sessionManager.getUser().value?.companyLocation
            userCompanyContactNo.value = sessionManager.getUser().value?.companyPhone
            userCompanyWorkEmail.value = sessionManager.getUser().value?.workEmail
            currentlyRepresenting.value = sessionManager.getUser().value?.currentlyRepresenting
            userProfilePic.value = sessionManager.getUser().value?.profilePic
        }
    }

    override fun onUpdate() {
        updateProfile(
            viewState.userFirstName.value.toString(),
            viewState.userSurname.value.toString(),
            viewState.userEmail.value.toString(),
            viewState.userContactNumber.value.toString(),
            viewState.userPassword.value.toString(),
            viewState.userConfirmPassword.value.toString(),
            viewState.userCompanyName.value.toString(),
            viewState.userCompanyVAT.value.toString(),
            viewState.userCompanyLocation.value.toString(),
            viewState.userCompanyContactNo.value.toString(),
            viewState.userCompanyWorkEmail.value.toString(),
            viewState.currentlyRepresenting.value
        )
    }

    override fun updateProfile(
        firstName: String,
        surname: String,
        email: String,
        contactNo: String,
        password: String,
        confirmPassword: String,
        companyName: String,
        companyVAT: String,
        companyLocation: String,
        companyContactNo: String,
        companyWorkEmail: String,
        currentlyRepresenting: Boolean?
    ) {
        val currentlyRepresenting1: Boolean = currentlyRepresenting ?: false

        val request = EditProfileRequest(
            firstName = firstName,
            surName = surname,
            phone = contactNo,
            password = confirmPassword,
            companyName = companyName,
            companyVat = companyVAT,
            companyLocation = companyLocation,
            companyPhone = companyContactNo,
            workEmail = companyWorkEmail,
            currentlyRepresenting = currentlyRepresenting1
        )
        launch {
            loading(true)
            when (val response = repository.updateProfileCall(request)) {

                is ApiResponse.Success -> {
                    sessionManager.updateUser(response.data.userObj)
                    loading(false, "Profile Updated successfully")
                    clickEvent?.postValue(111)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }

    }

    fun updateProfilePhoto(file: String, context: Context) {
        launch {
            loading(true)
            val fileUri = FileUtils.getFile(
                context,
                file.toUri()
            )?.absolutePath.toString()
            when (val response = repository.uploadProfilePicture(fileUri)) {
                is ApiResponse.Success -> {
                    val userObj = sessionManager.getUserObj()
                    if (response.data.profilePic != "") {
                        userObj?.profilePic = response.data.profilePic
                    }
                    else {
                        userObj?.profilePic = fileUri
                    }
                    userObj?.let { sessionManager.updateUser(userObj = it) }
                    viewState.userProfilePic.value = userObj?.profilePic
                    loading(false, "Profile pic updated")
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserDataUpdated(event: LocalEvents.UserDataUpdated?) {
        val handler = Handler()
        handler.postDelayed(Runnable {
            sessionManager.setUser()
            val userObj = sessionManager.getUser().value
            with(viewState) {
                userFirstName.value = userObj?.firstName
                userSurname.value = userObj?.surName
                userEmail.value = userObj?.email
                userContactNumber.value = userObj?.phone
                userPassword.value = sessionManager.getPass()
                userConfirmPassword.value = sessionManager.getPass()
                userCompanyName.value = userObj?.companyName
                userCompanyVAT.value = userObj?.companyVat
                userCompanyLocation.value = userObj?.companyLocation
                userCompanyContactNo.value = userObj?.companyPhone
                userCompanyWorkEmail.value = userObj?.workEmail
                currentlyRepresenting.value = userObj?.currentlyRepresenting
                userProfilePic.value = userObj?.profilePic
            }
        }, 100)
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

}