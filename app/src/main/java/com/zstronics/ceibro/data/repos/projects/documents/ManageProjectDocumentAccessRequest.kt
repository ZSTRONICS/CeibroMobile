package com.zstronics.ceibro.data.repos.projects.documents


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ManageProjectDocumentAccessRequest(
    @SerializedName("access")
    val access: List<String>,
    @SerializedName("group")
    val group: List<String>,
    @SerializedName("type")
    val accessType: String,
    @SerializedName("id")
    val fileOrFolderId: String,
)