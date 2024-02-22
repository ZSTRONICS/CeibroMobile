package com.zstronics.ceibro.data.repos.task.models.v2

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class AllTasksV2NewResponse(
    @SerializedName("data")
    val newData: NewData
) : BaseResponse(), Parcelable {
    @Parcelize
    @Keep
    data class NewData(
        @SerializedName("allTasks")
        val allTasks: List<CeibroTaskV2>?,
        @SerializedName("allEvents")
        val allEvents: List<Events>?,
        @SerializedName("allPins")
        val allPins: List<CeibroDrawingPins>?,
        @SerializedName("latestUpdatedAt")
        val latestUpdatedAt: String
    ) : BaseResponse(), Parcelable
}