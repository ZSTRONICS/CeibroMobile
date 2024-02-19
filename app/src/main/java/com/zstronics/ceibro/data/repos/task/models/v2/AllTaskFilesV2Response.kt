package com.zstronics.ceibro.data.repos.task.models.v2


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.LocalTaskDetailFiles
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class AllTaskFilesV2Response(
    @SerializedName("allTaskFiles")
    val allTaskFiles: List<LocalTaskDetailFiles>
) : BaseResponse(), Parcelable