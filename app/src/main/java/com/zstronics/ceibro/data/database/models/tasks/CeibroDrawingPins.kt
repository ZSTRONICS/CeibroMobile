package com.zstronics.ceibro.data.database.models.tasks

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.database.TableNamesV2
import kotlinx.parcelize.Parcelize

@Entity(tableName = TableNamesV2.DrawingPins, primaryKeys = ["_id"], indices = [Index(value = ["_id"]), Index(value = ["drawingId"])])
@Parcelize
@Keep
data class CeibroDrawingPins(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("creator")
    val creator: String,
    @SerializedName("drawingId")
    val drawingId: String,
    @SerializedName("page_height")
    val page_height: Int,
    @SerializedName("page_width")
    val page_width: Int,
    @SerializedName("pinPhotoUrl")
    val pinPhotoUrl: String,
    @SerializedName("pinUID")
    val pinUID: Int,
    @SerializedName("taskData")
    val taskData: PinTaskData,
    @SerializedName("thumbnail")
    val thumbnail: String,
    @SerializedName("thumbnailId")
    val thumbnailId: String?,
    @SerializedName("type")
    val type: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("x_coord")
    val x_coord: Double,
    @SerializedName("y_coord")
    val y_coord: Double
) : Parcelable {

    @Parcelize
    @Keep
    data class PinTaskData(
        @SerializedName("_id")
        val _id: String,
        @SerializedName("creatorState")
        val creatorState: String,
        @SerializedName("fromMeState")
        val fromMeState: String,
        @SerializedName("hiddenState")
        val hiddenState: String,
        @SerializedName("isAssignedToMe")
        val isAssignedToMe: Boolean,
        @SerializedName("isCreator")
        val isCreator: Boolean,
        @SerializedName("isHiddenByMe")
        val isHiddenByMe: Boolean,
        @SerializedName("isSeenByMe")
        val isSeenByMe: Boolean,
        @SerializedName("rootState")
        val rootState: String,
        @SerializedName("taskUID")
        val taskUID: String,
        @SerializedName("toMeState")
        val toMeState: String,
        @SerializedName("userSubState")
        val userSubState: String
    ) : Parcelable
}