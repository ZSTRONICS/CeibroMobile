package com.zstronics.ceibro.data.database.models.projects

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNamesV2
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import kotlinx.parcelize.Parcelize


@Keep
@Parcelize
@Entity(tableName = TableNamesV2.Floors, primaryKeys = ["_id"])
data class CeibroFloorV2(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("creator")
    val creator: String?,
    @SerializedName("deleted")
    val deleted: Boolean,
    @SerializedName("drawings")
    val drawings: List<String> = listOf(),
    @SerializedName("floorName")
    val floorName: String,
    @SerializedName("projectId")
    val projectId: String,
    @SerializedName("updatedAt")
    val updatedAt: String

) : BaseResponse(), Parcelable