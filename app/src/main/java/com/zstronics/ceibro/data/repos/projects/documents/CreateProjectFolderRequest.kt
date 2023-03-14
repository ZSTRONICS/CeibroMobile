package com.zstronics.ceibro.data.repos.projects.documents


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CreateProjectFolderRequest(
    @SerializedName("name")
    val folderName: String
)