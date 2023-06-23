package com.zstronics.ceibro.data.repos.projects.projectsmain

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNamesV2
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

@Entity(tableName = TableNamesV2.Projects)
data class ProjectsV2DatabaseEntity(
    @PrimaryKey
    val id: Int = 0,
    @ColumnInfo("projects")
    val projects: List<AllProjectsResponseV2.ProjectsV2>?
)