package com.zstronics.ceibro.data.repos.dashboard.invites


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class From(
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
    @SerializedName("mutedChat")
    val mutedChat: List<String>,
    @SerializedName("pinnedChat")
    val pinnedChat: List<String>,
    @SerializedName("pinnedMessages")
    val pinnedMessages: List<String>,
    @SerializedName("role")
    val role: String,
    @SerializedName("surName")
    val surName: String,
    @SerializedName("companyName")
    val companyName: String,
    @SerializedName("companyLocation")
    val companyLocation: String,
    @SerializedName("companyPhone")
    val companyPhone: String,
    @SerializedName("workEmail")
    val workEmail: String,
    @SerializedName("profilePic")
    var profilePic: String,
) : BaseResponse()