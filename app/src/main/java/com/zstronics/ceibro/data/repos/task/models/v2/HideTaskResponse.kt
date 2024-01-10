package com.zstronics.ceibro.data.repos.task.models.v2


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class HideTaskResponse(
    @SerializedName("creator")
    val creator: String,
    @SerializedName("hiddenBy")
    val hiddenBy: List<String>,
    @SerializedName("taskId")
    val taskId: String,
    @SerializedName("userSubState")
    val userSubState: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("taskUpdatedAt")
    val taskUpdatedAt: String,
    @SerializedName("fromMeState")
    val fromMeState: String,
    @SerializedName("toMeState")
    val toMeState: String,
    @SerializedName("hiddenState")
    val hiddenState: String,
    @SerializedName("pinData")
    val pinData: CeibroDrawingPins?
) : BaseResponse(), Parcelable