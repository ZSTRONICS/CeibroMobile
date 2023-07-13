package com.zstronics.ceibro.data.repos.task.models


import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNamesV2
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class TaskV2Response(
    @SerializedName("allTasks")
    val allTasks: AllTasks
) : BaseResponse(), Parcelable {
    @Parcelize
    @Keep
    data class AllTasks(
        @SerializedName("done")
        var done: List<CeibroTaskV2>,
        @SerializedName("new")
        var new: List<CeibroTaskV2>,
        @SerializedName("ongoing")
        var ongoing: List<CeibroTaskV2>,
        @SerializedName("unread")
        var unread: List<CeibroTaskV2>,
        @SerializedName("canceled")
        var canceled: List<CeibroTaskV2> = listOf()
    ) : BaseResponse(), Parcelable
}

@Entity(tableName = TableNamesV2.Tasks)
@Keep
data class TasksV2DatabaseEntity(
    @PrimaryKey
    @ColumnInfo("rootState")
    val rootState: String,
    @SerializedName("tasks")
    val allTasks: TaskV2Response.AllTasks
)