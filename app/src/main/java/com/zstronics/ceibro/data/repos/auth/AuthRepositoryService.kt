package com.zstronics.ceibro.data.repos.auth

import com.zstronics.ceibro.data.repos.auth.login.*
import com.zstronics.ceibro.data.repos.auth.refreshtoken.RefreshTokenRequest
import com.zstronics.ceibro.data.repos.auth.signup.*
import com.zstronics.ceibro.data.repos.editprofile.EditProfileRequest
import com.zstronics.ceibro.data.repos.editprofile.EditProfileResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AuthRepositoryService {
    @POST("v2/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("v1/users/profile")     //For Profile updating
    suspend fun updateProfile(@Body editProfileRequest: EditProfileRequest): Response<EditProfileResponse>

    @POST("v1/users/getprofile")     //For getting user Profile
    suspend fun getUserProfile(): Response<LoginResponse>

    @POST("v2/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<GenericResponse>

    @POST("v2/auth/otp/verify")
    suspend fun registerVerifyOtp(@Body registerVerifyOtpRequest: RegisterVerifyOtpRequest): Response<GenericResponse>

    @POST("v1/auth/register")
    suspend fun signup(@Body signUpRequest: SignUpRequest): Response<GenericResponse>


    @POST("v2/auth/forget-password")
    suspend fun forgetPassword(@Body forgetPasswordRequest: ForgetPasswordRequest): Response<GenericResponse>

    @POST("v2/auth/otp/verify-nodel")
    suspend fun forgetPassVerifyOtp(@Body registerVerifyOtpRequest: RegisterVerifyOtpRequest): Response<GenericResponse>

    @Multipart
    @PATCH("v1/users/profile/pic")
    suspend fun updateUserProfilePicture(@Part profilePicture: MultipartBody.Part): Response<UserProfilePicUpdateResponse>

    @POST("v1/auth/refresh-tokens")
    suspend fun refreshJWTToken(@Body body: RefreshTokenRequest): Response<Tokens>
}