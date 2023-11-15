package com.zstronics.ceibro.data.database.models.projects

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNamesV2
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = TableNamesV2.Projects, primaryKeys = ["_id"], indices = [Index(value = ["isRecentlyUsedByMe"]), Index(value = ["isHiddenByMe"])])
data class CeibroProjectV2(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("creator")
    val creator: TaskMemberDetail,
    @SerializedName("description")
    val description: String,
    @SerializedName("docsCount")
    val docsCount: Int,
    @SerializedName("isFavoriteByMe")
    val isFavoriteByMe: Boolean,
    @SerializedName("isHiddenByMe")
    val isHiddenByMe: Boolean,
    @SerializedName("isRecentlyUsedByMe")
    val isRecentlyUsedByMe: Boolean,
    @SerializedName("projectPic")
    val projectPic: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("updatedAt")
    val updatedAt: String
) : BaseResponse(), Parcelable
