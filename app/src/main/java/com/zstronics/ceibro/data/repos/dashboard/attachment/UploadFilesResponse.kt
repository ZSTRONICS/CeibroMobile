package com.zstronics.ceibro.data.repos.dashboard.attachment


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import androidx.room.Entity
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNames
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments

@Keep
data class UploadFilesResponse(
    @SerializedName("results") val results: Results
) : BaseResponse() {
    @Keep
    data class Results(
        @SerializedName("files") val files: List<FilesAttachments>,
        @SerializedName("moduleId") val moduleId: String,
        @SerializedName("moduleName") val moduleName: String
    ) : BaseResponse()
}