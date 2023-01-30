package com.zstronics.ceibro.data.repos.editprofile


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EditProfileRequest(
    @SerializedName("companyLocation")
    val companyLocation: String,
    @SerializedName("companyName")
    val companyName: String,
    @SerializedName("companyPhone")
    val companyPhone: String,
    @SerializedName("companyVat")
    val companyVat: String,
    @SerializedName("currentlyRepresenting")
    val currentlyRepresenting: Boolean,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("surName")
    val surName: String,
    @SerializedName("workEmail")
    val workEmail: String
)