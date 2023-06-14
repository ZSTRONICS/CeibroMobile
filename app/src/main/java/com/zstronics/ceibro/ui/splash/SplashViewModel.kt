package com.zstronics.ceibro.ui.splash

import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    override val viewState: SplashState,
    override var validator: Validator?,
    private val repository: IAuthRepository,
    private val taskRepository: TaskRepository,
    val sessionManager: SessionManager
) : HiltBaseViewModel<ISplash.State>(), ISplash.ViewModel, IValidator {

    init {
        if (sessionManager.isUserLoggedIn()) {
//            getProfile()
        }
    }

    override fun getProfile() {
        launch {
            when (val response = repository.getUserProfile()) {

                is ApiResponse.Success -> {
                    sessionManager.updateUser(response.data.user)
                }

                is ApiResponse.Error -> {
                }
            }
        }
    }

    fun endUserSession() {
        launch {
            taskRepository.eraseTaskTable()
            taskRepository.eraseSubTaskTable()
        }
        sessionManager.endUserSession()
    }

}