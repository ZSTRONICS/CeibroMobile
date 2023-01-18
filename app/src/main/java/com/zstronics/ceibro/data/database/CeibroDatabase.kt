package com.zstronics.ceibro.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zstronics.ceibro.data.database.converters.*
import com.zstronics.ceibro.data.database.dao.SubTaskDao
import com.zstronics.ceibro.data.database.dao.TaskDao
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.subtask.AssignedTo
import com.zstronics.ceibro.data.database.models.subtask.SubTaskAdvanceOptions
import com.zstronics.ceibro.data.database.models.subtask.Viewer
import com.zstronics.ceibro.data.database.models.tasks.AdvanceOptions
import com.zstronics.ceibro.data.database.models.tasks.ProjectSubTaskStatus
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember

@Database(
    entities = [CeibroTask::class, AdvanceOptions::class, ProjectSubTaskStatus::class, TaskMember::class, AllSubtask::class, AssignedTo::class,
        Viewer::class, SubTaskAdvanceOptions::class],
    version = 9,
    exportSchema = false
)
@TypeConverters(
    SubTaskAdvanceOptionsTypeConverter::class,
    AssignToListTypeConverter::class,
    ViewerListTypeConverter::class,
    AdvanceOptionsTypeConverter::class,
    ListConverters::class,
    ProjectSubTaskStatusListTypeConverter::class,
    TaskMemberListTypeConverter::class,
    TaskMemberTypeConverter::class,
    TaskProjectTypeConverter::class
)
abstract class CeibroDatabase : RoomDatabase() {
    abstract fun getTasksDao(): TaskDao
    abstract fun getSubTaskDao(): SubTaskDao

    companion object {
        const val DB_NAME = "ceibro_app.db"
    }
}