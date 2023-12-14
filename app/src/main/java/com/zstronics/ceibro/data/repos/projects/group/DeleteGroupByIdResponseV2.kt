package com.zstronics.ceibro.data.repos.projects.group

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class DeleteGroupByIdResponseV2(
    @SerializedName("message")
    var message: String? = null
) : BaseResponse(), Parcelable