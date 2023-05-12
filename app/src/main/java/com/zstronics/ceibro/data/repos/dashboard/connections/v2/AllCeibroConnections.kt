package com.zstronics.ceibro.data.repos.dashboard.connections.v2


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class AllCeibroConnections(
    @SerializedName("contacts")
    val contacts: List<CeibroConnection>
) : BaseResponse(), Parcelable {
    @Parcelize
    data class CeibroConnection(
        @SerializedName("contactFullName")
        val contactFullName: String?,
        @SerializedName("contactFirstName")
        val contactFirstName: String?,
        @SerializedName("contactSurName")
        val contactSurName: String?,
        @SerializedName("countryCode")
        val countryCode: String,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("_id")
        val id: String,
        @SerializedName("isBlocked")
        var isBlocked: Boolean,
        @SerializedName("isCeiborUser")
        val isCeiborUser: Boolean,
        @SerializedName("isSilent")
        val isSilent: Boolean,
        @SerializedName("phoneNumber")
        val phoneNumber: String,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("userCeibroData")
        val userCeibroData: UserCeibroData?
    ) : BaseResponse(), Parcelable {
        @Parcelize
        data class UserCeibroData(
            @SerializedName("companyName")
            val companyName: String,
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
            val surName: String
        ) : BaseResponse(), Parcelable
    }
}