package com.zstronics.ceibro.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zstronics.ceibro.data.database.converters.*
import com.zstronics.ceibro.data.database.dao.TaskDao
import com.zstronics.ceibro.data.database.models.tasks.AdvanceOptions
import com.zstronics.ceibro.data.database.models.tasks.ProjectSubTaskStatus
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember

@Database(
    entities = [CeibroTask::class, AdvanceOptions::class, ProjectSubTaskStatus::class, TaskMember::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(
    AdvanceOptionsTypeConverter::class,
    ListConverters::class,
    ProjectSubTaskListTypeConverter::class,
    TaskMemberListTypeConverter::class,
    TaskMemberTypeConverter::class,
    TaskProjectTypeConverter::class
)
abstract class CeibroDatabase : RoomDatabase() {
    abstract fun getTasksDao(): TaskDao

    companion object {
        const val DB_NAME = "ceibro_app.db"
    }
}