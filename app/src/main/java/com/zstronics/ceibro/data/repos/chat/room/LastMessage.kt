package com.zstronics.ceibro.data.repos.chat.room


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class LastMessage(
    @SerializedName("id")
    val id: String,
    @SerializedName("message")
    val message: String
): BaseResponse(), Parcelable