package com.zstronics.ceibro.data.repos.chat.room

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Project(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String
) : BaseResponse(), Parcelable