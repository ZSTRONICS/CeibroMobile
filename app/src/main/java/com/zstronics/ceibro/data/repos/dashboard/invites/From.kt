package com.zstronics.ceibro.data.repos.dashboard.invites


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class From(
    @SerializedName("companyLocation")
    val companyLocation: String,
    @SerializedName("companyName")
    val companyName: String,
    @SerializedName("companyPhone")
    val companyPhone: String,
    @SerializedName("companyVat")
    val companyVat: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("currentlyRepresenting")
    val currentlyRepresenting: Boolean,
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("_id")
    val id: String,
    @SerializedName("isOnline")
    val isOnline: Boolean,
    @SerializedName("mutedChat")
    val mutedChat: List<Any>,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("pinnedChat")
    val pinnedChat: List<Any>,
    @SerializedName("pinnedMessages")
    val pinnedMessages: List<Any>,
    @SerializedName("profilePic")
    val profilePic: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("surName")
    val surName: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("workEmail")
    val workEmail: String
) : BaseResponse()