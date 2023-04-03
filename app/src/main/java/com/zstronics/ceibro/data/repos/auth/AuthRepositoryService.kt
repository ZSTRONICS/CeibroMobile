package com.zstronics.ceibro.data.repos.auth

import com.zstronics.ceibro.data.repos.auth.login.*
import com.zstronics.ceibro.data.repos.auth.refreshtoken.RefreshTokenRequest
import com.zstronics.ceibro.data.repos.auth.signup.GenericResponse
import com.zstronics.ceibro.data.repos.auth.signup.SignUpRequest
import com.zstronics.ceibro.data.repos.editprofile.EditProfileRequest
import com.zstronics.ceibro.data.repos.editprofile.EditProfileResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AuthRepositoryService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("users/profile")     //For Profile updating
    suspend fun updateProfile(@Body editProfileRequest: EditProfileRequest): Response<EditProfileResponse>

    @POST("users/getprofile")     //For getting user Profile
    suspend fun getUserProfile(): Response<LoginResponse>

    @POST("auth/register")
    suspend fun signup(@Body signUpRequest: SignUpRequest): Response<GenericResponse>

    @Multipart
    @PATCH("users/profile/pic")
    suspend fun updateUserProfilePicture(@Part profilePicture: MultipartBody.Part): Response<UserProfilePicUpdateResponse>

    @POST("auth/refresh-tokens")
    suspend fun refreshJWTToken(@Body body: RefreshTokenRequest): Response<Tokens>
}