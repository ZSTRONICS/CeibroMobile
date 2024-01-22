package com.zstronics.ceibro.data.repos.auth

import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.repos.auth.login.AuthTokenResponse
import com.zstronics.ceibro.data.repos.auth.login.LoginRequest
import com.zstronics.ceibro.data.repos.auth.login.LoginResponse
import com.zstronics.ceibro.data.repos.auth.login.Tokens
import com.zstronics.ceibro.data.repos.auth.login.UserProfilePicUpdateResponse
import com.zstronics.ceibro.data.repos.auth.refreshtoken.RefreshTokenRequest
import com.zstronics.ceibro.data.repos.auth.signup.ForgetPasswordRequest
import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.auth.signup.RegisterRequest
import com.zstronics.ceibro.data.repos.auth.signup.RegisterVerifyOtpRequest
import com.zstronics.ceibro.data.repos.auth.signup.ResetPasswordRequest
import com.zstronics.ceibro.data.repos.auth.signup.SignUpRequest
import com.zstronics.ceibro.data.repos.editprofile.ChangeNumberRequest
import com.zstronics.ceibro.data.repos.editprofile.ChangeNumberVerifyOtpRequest
import com.zstronics.ceibro.data.repos.editprofile.ChangePasswordRequest
import com.zstronics.ceibro.data.repos.editprofile.EditProfileRequest
import com.zstronics.ceibro.data.repos.editprofile.EditProfileResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.greenrobot.eventbus.EventBus
import java.io.File
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val service: AuthRepositoryService,
    val sessionManager: SessionManager
) : IAuthRepository, BaseNetworkRepository() {

    override suspend fun login(loginRequest: LoginRequest): ApiResponse<LoginResponse> {
        val response = executeSafely(
            call =
            {
                service.login(loginRequest)
            }
        )
        when (response) {
            is ApiResponse.Success -> {
                CeibroApplication.CookiesManager.jwtToken = response.data.tokens.access.token
                CeibroApplication.CookiesManager.isLoggedIn = true
            }

            is ApiResponse.Error -> {

            }
        }
        return response
    }

    override suspend fun updateProfileCall(editProfileRequest: EditProfileRequest): ApiResponse<EditProfileResponse> {
        return executeSafely(
            call =
            {
                service.updateProfile(editProfileRequest)
            }
        )
    }

    override suspend fun changePassword(changePasswordRequest: ChangePasswordRequest): ApiResponse<GenericResponse> {
        return executeSafely(
            call =
            {
                service.changePassword(changePasswordRequest)
            }
        )
    }

    override suspend fun changePhoneNumber(changeNumberRequest: ChangeNumberRequest): ApiResponse<GenericResponse> {
        return executeSafely(
            call =
            {
                service.changePhoneNumber(changeNumberRequest)
            }
        )
    }

    override suspend fun changePhoneNumberVerifyOtp(changeNumberVerifyOtpRequest: ChangeNumberVerifyOtpRequest): ApiResponse<GenericResponse> {
        return executeSafely(
            call =
            {
                service.changePhoneNumberVerifyOtp(changeNumberVerifyOtpRequest)
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

    override suspend fun register(
        registerRequest: RegisterRequest,
        token: String
    ): ApiResponse<GenericResponse> {
        return executeSafely(
            call =
            {
                service.register(registerRequest, token)
            }
        )
    }

    override suspend fun getAuthToken(clientId: String): ApiResponse<AuthTokenResponse> {
        return executeSafely(
            call =
            {
                service.getAuthToken(clientId)
            }
        )
    }

    override suspend fun registerVerifyOtp(registerVerifyOtpRequest: RegisterVerifyOtpRequest): ApiResponse<GenericResponse> {
        return executeSafely(
            call =
            {
                service.registerVerifyOtp(registerVerifyOtpRequest)
            }
        )
    }

    override suspend fun signup(
        phoneNumber: String,
        signUpRequest: SignUpRequest
    ): ApiResponse<LoginResponse> {
        return executeSafely(
            call =
            {
                service.signup(phoneNumber, signUpRequest)
            }
        )
    }


    override suspend fun forgetPassword(
        forgetPasswordRequest: ForgetPasswordRequest,
        token: String
    ): ApiResponse<GenericResponse> {
        return executeSafely(
            call =
            {
                service.forgetPassword(forgetPasswordRequest, token)
            }
        )
    }

    override suspend fun forgetPassVerifyOtp(registerVerifyOtpRequest: RegisterVerifyOtpRequest): ApiResponse<GenericResponse> {
        return executeSafely(
            call =
            {
                service.forgetPassVerifyOtp(registerVerifyOtpRequest)
            }
        )
    }

    override suspend fun resendOtp(forgetPasswordRequest: ForgetPasswordRequest): ApiResponse<GenericResponse> {
        return executeSafely(
            call =
            {
                service.resendOtp(forgetPasswordRequest)
            }
        )
    }

    override suspend fun resendOtpBeforeLogin(
        forgetPasswordRequest: ForgetPasswordRequest,
        token: String
    ): ApiResponse<GenericResponse> {
        return executeSafely(
            call =
            {
                service.resendOtpBeforeLogin(forgetPasswordRequest, token)
            }
        )
    }

    override suspend fun resetPassword(resetPasswordRequest: ResetPasswordRequest): ApiResponse<GenericResponse> {
        return executeSafely(
            call =
            {
                service.resetPassword(resetPasswordRequest)
            }
        )
    }

    override suspend fun uploadProfilePicture(fileUri: String): ApiResponse<UserProfilePicUpdateResponse> {
        val file = File(fileUri)
        val reqFile = file.asRequestBody(("image/" + file.extension).toMediaTypeOrNull())
        val multiPartImageFile: MultipartBody.Part =
            MultipartBody.Part.createFormData("profilePic", file.name, reqFile)
        return executeSafely(call = {
            service.updateUserProfilePicture(multiPartImageFile)
        })
    }

    override suspend fun refreshJWTToken(body: RefreshTokenRequest): ApiResponse<Tokens> {
        val response = executeSafely(
            call =
            {
                service.refreshJWTToken(body)
            }
        )
        when (response) {
            is ApiResponse.Success -> {
                CeibroApplication.CookiesManager.isLoggedIn = true
                CeibroApplication.CookiesManager.tokens = response.data
                CeibroApplication.CookiesManager.jwtToken = response.data.access.token
                sessionManager.refreshToken(response.data)
                SocketHandler.reconnectSocket()
            }

            is ApiResponse.Error -> {
                if (response.error.statusCode == 406) {
                    EventBus.getDefault().post(LocalEvents.LogoutUserEvent())
                }
            }
        }
        return response
    }

    override suspend fun uploadProfilePictureV2(fileUri: String): ApiResponse<UserProfilePicUpdateResponse> {
        val file = File(fileUri)
        val reqFile = file.asRequestBody(("image/" + file.extension).toMediaTypeOrNull())
        val multiPartImageFile: MultipartBody.Part =
            MultipartBody.Part.createFormData("profilePic", file.name, reqFile)
        return executeSafely(call = {
            service.uploadProfilePictureV2(multiPartImageFile)
        })
    }
}