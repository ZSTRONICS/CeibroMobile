package com.zstronics.ceibro.data.repos.task.models


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SubtaskStatusData(
    @SerializedName("_id")
    val id: String,
    @SerializedName("userId")
    val user: TaskMember,
    @SerializedName("userState")
    val userState: String
) : BaseResponse(), Parcelable