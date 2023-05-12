package com.zstronics.ceibro.data.repos.auth

import com.zstronics.ceibro.data.repos.auth.login.*
import com.zstronics.ceibro.data.repos.auth.refreshtoken.RefreshTokenRequest
import com.zstronics.ceibro.data.repos.auth.signup.*
import com.zstronics.ceibro.data.repos.editprofile.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AuthRepositoryService {
    @POST("v2/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @PATCH("v2/users/profile")     //For Profile updating
    suspend fun updateProfile(@Body editProfileRequest: EditProfileRequest): Response<EditProfileResponse>

    @POST("v2/users/change-password")
    suspend fun changePassword(@Body changePasswordRequest: ChangePasswordRequest): Response<GenericResponse>

    @POST("v2/users/change-number")
    suspend fun changePhoneNumber(@Body changeNumberRequest: ChangeNumberRequest): Response<GenericResponse>

    @POST("v2/users/verify/change-number")
    suspend fun changePhoneNumberVerifyOtp(@Body changeNumberVerifyOtpRequest: ChangeNumberVerifyOtpRequest): Response<GenericResponse>

    @POST("v1/users/getprofile")     //For getting user Profile
    suspend fun getUserProfile(): Response<LoginResponse>

    @POST("v2/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<GenericResponse>

    @POST("v2/auth/otp/verify")
    suspend fun registerVerifyOtp(@Body registerVerifyOtpRequest: RegisterVerifyOtpRequest): Response<GenericResponse>

    @POST("v2/users/{phoneNumber}/profile")
    suspend fun signup(
        @Path("phoneNumber") phoneNumber: String,
        @Body signUpRequest: SignUpRequest
    ): Response<LoginResponse>


    @POST("v2/auth/forget-password")
    suspend fun forgetPassword(@Body forgetPasswordRequest: ForgetPasswordRequest): Response<GenericResponse>

    @POST("v2/auth/otp/verify-nodel")
    suspend fun forgetPassVerifyOtp(@Body registerVerifyOtpRequest: RegisterVerifyOtpRequest): Response<GenericResponse>

    @POST("v2/auth/otp/resend")
    suspend fun resendOtp(@Body forgetPasswordRequest: ForgetPasswordRequest): Response<GenericResponse>

    @POST("v2/auth/reset-password")
    suspend fun resetPassword(@Body resetPasswordRequest: ResetPasswordRequest): Response<GenericResponse>

    @Multipart
    @PATCH("v1/users/profile/pic")
    suspend fun updateUserProfilePicture(@Part profilePicture: MultipartBody.Part): Response<UserProfilePicUpdateResponse>

    @Multipart
    @PATCH("v2/users/profile/pic")
    suspend fun uploadProfilePictureV2(@Part profilePicture: MultipartBody.Part): Response<UserProfilePicUpdateResponse>

    @POST("v1/auth/refresh-tokens")
    suspend fun refreshJWTToken(@Body body: RefreshTokenRequest): Response<Tokens>
}