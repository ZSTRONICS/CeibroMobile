package com.zstronics.ceibro.data.repos.dashboard.attachment


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments

@Keep
data class GetAllFilesResponse(
    @SerializedName("result") val results: List<FilesAttachments>?
) : BaseResponse()