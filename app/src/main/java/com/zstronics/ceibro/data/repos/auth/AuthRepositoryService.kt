package com.zstronics.ceibro.data.repos.auth

import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.auth.login.LoginRequest
import com.zstronics.ceibro.data.repos.auth.login.LoginResponse
import com.zstronics.ceibro.data.repos.auth.login.User
import com.zstronics.ceibro.data.repos.auth.signup.SignUpRequest
import com.zstronics.ceibro.data.repos.auth.signup.SignUpResponse
import com.zstronics.ceibro.data.repos.editprofile.EditProfileRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AuthRepositoryService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("users/profile")     //For Profile updating
    suspend fun updateProfile(@Body editProfileRequest: EditProfileRequest): Response<User>

    @POST("users/getprofile")     //For getting user Profile
    suspend fun getUserProfile(): Response<LoginResponse>

    @POST("auth/register")
    suspend fun signup(@Body signUpRequest: SignUpRequest): Response<SignUpResponse>

    @Multipart
    @PATCH("users/profile/pic")
    suspend fun updateUserProfilePicture(@Part profilePicture: MultipartBody.Part): Response<User>

}