package com.zstronics.ceibro.data.repos.chat.room


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class RemovedAcces(
    @SerializedName("companyName")
    val companyName: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("profilePic")
    val profilePic: String,
    @SerializedName("surName")
    val surName: String
) : BaseResponse(), Parcelable