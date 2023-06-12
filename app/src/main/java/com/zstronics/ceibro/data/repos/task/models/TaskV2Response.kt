package com.zstronics.ceibro.data.repos.task.models


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class TaskV2Response(
    @SerializedName("allTasks")
    val allTasks: AllTasks
) : BaseResponse(), Parcelable {
    @Parcelize
    @Keep
    data class AllTasks(
        @SerializedName("done")
        val done: List<CeibroTaskV2>,
        @SerializedName("new")
        val new: List<CeibroTaskV2>,
        @SerializedName("ongoing")
        val ongoing: List<CeibroTaskV2>,
        @SerializedName("unread")
        val unread: List<CeibroTaskV2>
    ) : BaseResponse(), Parcelable
}