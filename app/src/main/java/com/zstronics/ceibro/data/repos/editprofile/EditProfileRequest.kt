package com.zstronics.ceibro.data.repos.editprofile


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EditProfileRequest(
    @SerializedName("companyName")
    val companyName: String,
    @SerializedName("jobTitle")
    val jobTitle: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("surName")
    val surName: String,
    @SerializedName("email")
    val email: String
)