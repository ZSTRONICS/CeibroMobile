package com.zstronics.ceibro.data.repos.dashboard.admins

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class AdminUsersResponse(
    @SerializedName("result")
    val result: List<AdminUserObj>
) : BaseResponse(), Parcelable