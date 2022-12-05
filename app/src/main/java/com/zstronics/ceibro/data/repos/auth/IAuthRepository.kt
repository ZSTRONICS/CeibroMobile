package com.zstronics.ceibro.data.repos.auth

import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.auth.login.LoginRequest
import com.zstronics.ceibro.data.repos.auth.login.LoginResponse
import com.zstronics.ceibro.data.repos.auth.login.User
import com.zstronics.ceibro.data.repos.auth.signup.SignUpRequest
import com.zstronics.ceibro.data.repos.auth.signup.SignUpResponse
import com.zstronics.ceibro.data.repos.editprofile.EditProfileRequest
import retrofit2.http.Body

interface IAuthRepository {
    suspend fun login(loginRequest: LoginRequest): ApiResponse<LoginResponse>
    suspend fun updateProfileCall(editProfileRequest: EditProfileRequest): ApiResponse<User>
    suspend fun getUserProfile(): ApiResponse<LoginResponse>
    suspend fun signup(signUpRequest: SignUpRequest): ApiResponse<SignUpResponse>
    suspend fun uploadProfilePicture(fileUri: String): ApiResponse<User>

}