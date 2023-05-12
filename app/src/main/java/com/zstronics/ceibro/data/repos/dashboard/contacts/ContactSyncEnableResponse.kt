package com.zstronics.ceibro.data.repos.dashboard.contacts


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class ContactSyncEnableResponse(
    @SerializedName("user")
    val user: User
) : BaseResponse() {
    data class User(
        @SerializedName("autoContactSync")
        val autoContactSync: Boolean,
        @SerializedName("companyName")
        val companyName: String,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("email")
        val email: String,
        @SerializedName("firstName")
        val firstName: String,
        @SerializedName("_id")
        val id: String,
        @SerializedName("jobTitle")
        val jobTitle: String,
        @SerializedName("phoneNumber")
        val phoneNumber: String,
        @SerializedName("profilePic")
        val profilePic: String,
        @SerializedName("surName")
        val surName: String,
        @SerializedName("updatedAt")
        val updatedAt: String
    ) : BaseResponse()
}