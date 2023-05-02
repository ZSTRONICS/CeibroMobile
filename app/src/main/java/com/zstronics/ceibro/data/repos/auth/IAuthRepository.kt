package com.zstronics.ceibro.data.repos.auth

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.login.LoginRequest
import com.zstronics.ceibro.data.repos.auth.login.LoginResponse
import com.zstronics.ceibro.data.repos.auth.login.Tokens
import com.zstronics.ceibro.data.repos.auth.login.UserProfilePicUpdateResponse
import com.zstronics.ceibro.data.repos.auth.refreshtoken.RefreshTokenRequest
import com.zstronics.ceibro.data.repos.auth.signup.*
import com.zstronics.ceibro.data.repos.editprofile.EditProfileRequest
import com.zstronics.ceibro.data.repos.editprofile.EditProfileResponse

interface IAuthRepository {
    suspend fun login(loginRequest: LoginRequest): ApiResponse<LoginResponse>
    suspend fun updateProfileCall(editProfileRequest: EditProfileRequest): ApiResponse<EditProfileResponse>
    suspend fun getUserProfile(): ApiResponse<LoginResponse>
    suspend fun register(registerRequest: RegisterRequest): ApiResponse<GenericResponse>
    suspend fun registerVerifyOtp(registerVerifyOtpRequest: RegisterVerifyOtpRequest): ApiResponse<GenericResponse>
    suspend fun signup(signUpRequest: SignUpRequest): ApiResponse<GenericResponse>
    suspend fun forgetPassword(forgetPasswordRequest: ForgetPasswordRequest): ApiResponse<GenericResponse>
    suspend fun forgetPassVerifyOtp(registerVerifyOtpRequest: RegisterVerifyOtpRequest): ApiResponse<GenericResponse>
    suspend fun resendOtp(forgetPasswordRequest: ForgetPasswordRequest): ApiResponse<GenericResponse>
    suspend fun uploadProfilePicture(fileUri: String): ApiResponse<UserProfilePicUpdateResponse>
    suspend fun refreshJWTToken(body: RefreshTokenRequest): ApiResponse<Tokens>

}