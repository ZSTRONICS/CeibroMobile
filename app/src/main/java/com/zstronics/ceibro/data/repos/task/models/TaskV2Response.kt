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
        var done: MutableList<CeibroTaskV2> = mutableListOf(),
        @SerializedName("new")
        var new: MutableList<CeibroTaskV2> = mutableListOf(),
        @SerializedName("ongoing")
        var ongoing: MutableList<CeibroTaskV2> = mutableListOf(),
        @SerializedName("unread")
        var unread: MutableList<CeibroTaskV2> = mutableListOf(),
        @SerializedName("canceled")
        var canceled: MutableList<CeibroTaskV2> = mutableListOf()
    ) : BaseResponse(), Parcelable
}

@Entity(tableName = TableNamesV2.TasksInternal)
@Keep
data class TasksV2DatabaseEntity(
    @PrimaryKey
    @ColumnInfo("rootState")
    val rootState: String,
    @SerializedName("tasks")
    val allTasks: TaskV2Response.AllTasks
)

@Entity(tableName = TableNamesV2.Tasks,primaryKeys = ["rootState", "subState"])
@Keep
data class TasksV2DatabaseEntitySingle(
    @ColumnInfo("rootState")
    val rootState: String,
    @ColumnInfo("subState")
    val subState: String,
    @SerializedName("task")
    var task: SingleTaskEntity
)


@Parcelize
@Keep
data class SingleTaskEntity(
    @SerializedName("data")
    var data: MutableList<CeibroTaskV2> = mutableListOf()
) : BaseResponse(), Parcelable