package com.zstronics.ceibro.data.repos.auth.login


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class User(
    @SerializedName("companyLocation")
    val companyLocation: String?,
    @SerializedName("companyName")
    val companyName: String?,
    @SerializedName("companyPhone")
    val companyPhone: String?,
    @SerializedName("companyVat")
    val companyVat: String?,
    @SerializedName("currentlyRepresenting")
    val currentlyRepresenting: Boolean?,
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("_id")
    val id: String,
    @SerializedName("isEmailVerified")
    val isEmailVerified: Boolean?,
    @SerializedName("isOnline")
    val isOnline: Boolean?,
    @SerializedName("lockedUntil")
    val lockedUntil: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("profilePic")
    var profilePic: String,
    @SerializedName("role")
    val role: String?,
    @SerializedName("surName")
    val surName: String,
    @SerializedName("jobTitle")
    val jobTitle: String,
    @SerializedName("workEmail")
    val workEmail: String?,
    @SerializedName("autoContactSync")
    var autoContactSync: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
) : BaseResponse()