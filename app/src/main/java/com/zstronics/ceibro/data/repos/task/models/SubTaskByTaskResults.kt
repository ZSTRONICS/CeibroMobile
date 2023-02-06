package com.zstronics.ceibro.data.repos.task.models


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SubTaskByTaskResults(
    @SerializedName("subtasks")
    val subtasks: List<AllSubtask>,
    @SerializedName("task")
    val task: CeibroTask?
) : BaseResponse(), Parcelable