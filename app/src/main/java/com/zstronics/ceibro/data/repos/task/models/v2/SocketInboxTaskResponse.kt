package com.zstronics.ceibro.data.repos.task.models.v2

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.inbox.CeibroInboxV2
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SocketInboxTaskResponse(
    @SerializedName("eventType")
    val eventType: String,
    @SerializedName("data")
    val data: CeibroInboxV2?
) : BaseResponse(), Parcelable
