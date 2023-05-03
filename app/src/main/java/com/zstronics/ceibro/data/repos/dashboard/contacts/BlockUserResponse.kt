package com.zstronics.ceibro.data.repos.dashboard.contacts


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class BlockUserResponse(
    @SerializedName("contacts")
    val contacts: Contacts
) : BaseResponse() {
    data class Contacts(
        @SerializedName("contactFirstName")
        val contactFirstName: String,
        @SerializedName("contactSurName")
        val contactSurName: String,
        @SerializedName("countryCode")
        val countryCode: String,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("_id")
        val id: String,
        @SerializedName("isBlocked")
        val isBlocked: Boolean,
        @SerializedName("isCeiborUser")
        val isCeiborUser: Boolean,
        @SerializedName("isSilent")
        val isSilent: Boolean,
        @SerializedName("phoneNumber")
        val phoneNumber: String,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("userCeibroData")
        val userCeibroData: String
    ) : BaseResponse()
}