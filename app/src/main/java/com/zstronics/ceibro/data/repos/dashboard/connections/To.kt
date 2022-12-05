package com.zstronics.ceibro.data.repos.dashboard.connections


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class To(
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
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("isEmailVerified")
    val isEmailVerified: Boolean,
    @SerializedName("isOnline")
    val isOnline: Boolean,
    @SerializedName("lockedUntil")
    val lockedUntil: String,
    @SerializedName("mutedChat")
    val mutedChat: List<Any>,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("pinnedChat")
    val pinnedChat: List<String>,
    @SerializedName("pinnedMessages")
    val pinnedMessages: List<Any>,
    @SerializedName("role")
    val role: String,
    @SerializedName("socketId")
    val socketId: String,
    @SerializedName("surName")
    val surName: String,
    @SerializedName("profilePic")
    var profilePic: String,
    @SerializedName("workEmail")
    val workEmail: String
) : BaseResponse()