package com.zstronics.ceibro.data.repos.projects.documents

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class RefreshFolderResponse(
    @SerializedName("projectId")
    val projectId: String,
    @SerializedName("folderId")
    val folderId: String
) : BaseResponse()
