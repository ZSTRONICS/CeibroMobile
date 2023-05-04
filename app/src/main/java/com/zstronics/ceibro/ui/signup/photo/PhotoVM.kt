package com.zstronics.ceibro.ui.signup.photo

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PhotoVM @Inject constructor(
    override val viewState: PhotoState,
    private val repository: IAuthRepository,
    val sessionManager: SessionManager
) : HiltBaseViewModel<IPhoto.State>(), IPhoto.ViewModel {
    var selectedUri: Uri? = null
    fun updatePhoto(context: Context, callback: () -> Unit) {
        launch {
            loading(true)
            val fileUri = FileUtils.getFile(
                context,
                selectedUri.toString().toUri()
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
                    loading(false, "Profile pic updated")
                    callback.invoke()
                }
                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

}