package com.zstronics.ceibro.data.repos.auth

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.auth.login.LoginRequest
import com.zstronics.ceibro.data.repos.auth.login.LoginResponse
import com.zstronics.ceibro.data.repos.auth.login.User
import com.zstronics.ceibro.data.repos.auth.signup.SignUpRequest
import com.zstronics.ceibro.data.repos.auth.signup.SignUpResponse
import com.zstronics.ceibro.data.repos.editprofile.EditProfileRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val service: AuthRepositoryService
) : IAuthRepository, BaseNetworkRepository() {

    override suspend fun login(loginRequest: LoginRequest): ApiResponse<LoginResponse> {
        return executeSafely(
            call =
            {
                service.login(loginRequest)
            }
        )
    }

    override suspend fun updateProfileCall(editProfileRequest: EditProfileRequest): ApiResponse<User> {
        return executeSafely(
            call =
            {
                service.updateProfile(editProfileRequest)
            }
        )
    }

    override suspend fun getUserProfile(): ApiResponse<LoginResponse> {
        return executeSafely(
            call =
            {
                service.getUserProfile()
            }
        )
    }

    override suspend fun signup(signUpRequest: SignUpRequest): ApiResponse<SignUpResponse> {
        return executeSafely(
            call =
            {
                service.signup(signUpRequest)
            }
        )
    }
    override suspend fun uploadProfilePicture(fileUri: String): ApiResponse<User> {
        val file = File(fileUri)
        val reqFile = file.asRequestBody(("image/" + file.extension).toMediaTypeOrNull())
        val multiPartImageFile: MultipartBody.Part =
            MultipartBody.Part.createFormData("profilePic", file.name, reqFile)
        return executeSafely(call = {
            service.updateUserProfilePicture(multiPartImageFile)
        })
    }

}