package com.zstronics.ceibro.data.repos.dashboard.admins


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
data class AdminUsersResponse(
    @SerializedName("result")
    val result: List<AdminUserData>
) : BaseResponse() {
    @Keep
    @Parcelize
    data class AdminUserData(
        @SerializedName("companyName")
        val companyName: String?,
        @SerializedName("companyPhone")
        val companyPhone: String?,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("email")
        val email: String?,
        @SerializedName("firstName")
        val firstName: String,
        @SerializedName("_id")
        val id: String,
        @SerializedName("phone")
        val phone: String?,
        @SerializedName("profilePic")
        val profilePic: String?,
        @SerializedName("surName")
        val surName: String,
        @SerializedName("workEmail")
        val workEmail: String?
    ) : BaseResponse(), Parcelable
}