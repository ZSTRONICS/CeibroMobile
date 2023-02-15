package com.zstronics.ceibro.data.repos.task.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments

@Keep

data class FileUploadedEventResponse(
    @SerializedName("eventType")
    val eventType: String,
    @SerializedName("data")
    val data: FilesAttachments?
) : BaseResponse()
