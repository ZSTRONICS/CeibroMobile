package com.zstronics.ceibro.data.database.models.subtask

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.database.TableNames
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import kotlinx.parcelize.Parcelize

@Entity(tableName = TableNames.SubTaskAdvanceOptions)
@Parcelize
@Keep
data class SubTaskAdvanceOptions(
    @PrimaryKey
    val id: Int,
    @SerializedName("categories") val categories: List<String>,
    @SerializedName("checkList") val checkList: List<String>,
    @SerializedName("confirmNeeded") val confirmNeeded: List<TaskMember>,
    @SerializedName("isAdditionalWork") val isAdditionalWork: Boolean,
    @SerializedName("location") val location: String,
    @SerializedName("manPower") val manPower: Int,
    @SerializedName("priority") val priority: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("timeLog") val timeLog: Boolean,
    @SerializedName("viewer") val viewer: List<TaskMember>
) : Parcelable
