package com.zstronics.ceibro.ui.profile.editprofile

import android.content.Context
import android.os.Handler
import androidx.core.net.toUri
import androidx.work.WorkManager
import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.dao.TopicsV2Dao
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.auth.signup.ForgetPasswordRequest
import com.zstronics.ceibro.data.repos.editprofile.ChangeNumberRequest
import com.zstronics.ceibro.data.repos.editprofile.ChangeNumberVerifyOtpRequest
import com.zstronics.ceibro.data.repos.editprofile.ChangePasswordRequest
import com.zstronics.ceibro.data.repos.editprofile.EditProfileRequest
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.contacts.ContactSyncWorker
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
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
    private val taskRepository: TaskRepository,
    private val taskDao: TaskV2Dao,
    private val topicsV2Dao: TopicsV2Dao,
    private val projectsV2Dao: ProjectsV2Dao,
    private val connectionsV2Dao: ConnectionsV2Dao,
    val sessionManager: SessionManager
) : HiltBaseViewModel<IEditProfile.State>(), IEditProfile.ViewModel, IValidator {
    val user = sessionManager.getUser().value

    init {
        EventBus.getDefault().register(this)
        sessionManager.setUser()
        with(viewState) {
            userFirstName.value = sessionManager.getUser().value?.firstName
            userSurname.value = sessionManager.getUser().value?.surName
            userEmail.value = sessionManager.getUser().value?.email
            userPhoneNumber.value = sessionManager.getUser().value?.phoneNumber
            userCompanyName.value = sessionManager.getUser().value?.companyName
            userJobTitle.value = sessionManager.getUser().value?.jobTitle
            userProfilePic.value = sessionManager.getUser().value?.profilePic
        }
    }


    override fun updateProfile(
        firstName: String,
        surname: String,
        email: String,
        phoneNumber: String,
        companyName: String,
        jobTitle: String,
        onProfileUpdated: () -> Unit
    ) {
        val request = EditProfileRequest(
            firstName = firstName,
            surName = surname,
            email = email,
            companyName = companyName,
            jobTitle = jobTitle
        )
        launch {
            loading(true)
            when (val response = repository.updateProfileCall(request)) {

                is ApiResponse.Success -> {
                    println("user data on upadation:${response.data}")
                    sessionManager.updateUser(response.data.user)
                    loading(false, "Profile Updated successfully")
                    onProfileUpdated.invoke()
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

    override fun changePassword(oldPassword: String, newPassword: String, onPasswordChanged: () -> Unit) {
        val request = ChangePasswordRequest(
            oldPassword = oldPassword,
            newPassword = newPassword
        )
        launch {
            loading(true)
            when (val response = repository.changePassword(request)) {

                is ApiResponse.Success -> {
                    loading(false, "Password updated successfully, please re-login")
                    onPasswordChanged.invoke()
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

    override fun changePhoneNumber(newNumber: String, countryCode: String, password: String, onNumberChanged: () -> Unit) {
        val request = ChangeNumberRequest(
            newNumber = newNumber,
            countryCode = countryCode,
            password = password
        )
        launch {
            loading(true)
            when (val response = repository.changePhoneNumber(request)) {

                is ApiResponse.Success -> {
                    loading(false, response.data.message)
                    onNumberChanged.invoke()
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

    override fun changePhoneNumberVerifyOtp(newNumber: String, otp: String, onNumberVerified: () -> Unit) {
        val request = ChangeNumberVerifyOtpRequest(
            newNumber = newNumber,
            otp = otp
        )
        launch {
            loading(true)
            when (val response = repository.changePhoneNumberVerifyOtp(request)) {

                is ApiResponse.Success -> {
                    loading(false, "Phone number changed, please re-login")
                    onNumberVerified.invoke()
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }


    override fun resendOtp(phoneNumber: String, onOtpResend: () -> Unit) {
        val request = ForgetPasswordRequest(phoneNumber = phoneNumber)
        launch {
            loading(true)
            when (val response = repository.resendOtp(request)) {

                is ApiResponse.Success -> {
                    loading(false, response.data.message)
                    onOtpResend.invoke()
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

    override fun updateProfilePhoto(file: String, context: Context) {
        launch {
            loading(true)
            val fileUri = FileUtils.getFile(
                context,
                file.toUri()
            )?.absolutePath.toString()
            when (val response = repository.uploadProfilePictureV2(fileUri)) {
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
                userPhoneNumber.value = userObj?.phoneNumber
                userCompanyName.value = userObj?.companyName
                userJobTitle.value = userObj?.jobTitle
                userProfilePic.value = userObj?.profilePic
            }
        }, 100)
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

    override fun endUserSession(context: Context) {
        launch {
            taskRepository.eraseTaskTable()
            taskRepository.eraseSubTaskTable()
            taskDao.deleteAllData()
            topicsV2Dao.deleteAllData()
            projectsV2Dao.deleteAll()
            connectionsV2Dao.deleteAll()
        }
        SocketHandler.sendLogout()
        sessionManager.endUserSession()
        // Cancel all periodic work with the tag "contactSync"
        WorkManager.getInstance(context)
            .cancelAllWorkByTag(ContactSyncWorker.CONTACT_SYNC_WORKER_TAG)
    }
}