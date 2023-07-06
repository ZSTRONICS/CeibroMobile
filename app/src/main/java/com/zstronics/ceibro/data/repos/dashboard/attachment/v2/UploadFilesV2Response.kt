package com.zstronics.ceibro.data.repos.dashboard.attachment.v2


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.Files

data class UploadFilesV2Response(
    @SerializedName("data")
    val uploadData: List<Files>
) : BaseResponse()