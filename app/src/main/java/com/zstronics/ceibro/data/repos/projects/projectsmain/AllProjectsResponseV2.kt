package com.zstronics.ceibro.data.repos.projects.projectsmain

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class AllProjectsResponseV2(
    @SerializedName("results")
    val projects: List<ProjectsV2>?
) : BaseResponse(), Parcelable {
    @Keep
    @Parcelize
    data class ProjectsV2(
        @SerializedName("access")
        val access: List<String>,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("creator")
        val creator: OwnerV2?,
        @SerializedName("docsCount")
        val docsCount: Int,
        @SerializedName("_id")
        val id: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("updatedAt")
        val updatedAt: String
    ) : BaseResponse(), Parcelable {
        @Keep
        @Parcelize
        data class OwnerV2(
            @SerializedName("firstName")
            val firstName: String,
            @SerializedName("_id")
            val id: String,
            @SerializedName("surName")
            val surName: String
        ) : BaseResponse(), Parcelable
    }
}