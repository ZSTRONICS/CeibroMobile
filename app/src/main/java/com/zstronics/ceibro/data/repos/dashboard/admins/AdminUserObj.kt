package com.zstronics.ceibro.data.repos.dashboard.admins

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
data class AdminUserObj(
    @SerializedName("_id")
    val id: String,
    @SerializedName("_id")
    val companyName: String? = "",
    @SerializedName("_id")
    val companyPhone: String? = "",
    @SerializedName("_id")
    val createdAt: String,
    @SerializedName("_id")
    val email: String? = "",
    @SerializedName("_id")
    val firstName: String,
    @SerializedName("_id")
    val phone: String? = "",
    @SerializedName("_id")
    val profilePic: String? = "",
    @SerializedName("_id")
    val surName: String,
    @SerializedName("_id")
    val workEmail: String? = ""
) : BaseResponse()