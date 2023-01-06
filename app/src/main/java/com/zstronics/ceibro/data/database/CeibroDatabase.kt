package com.zstronics.ceibro.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zstronics.ceibro.data.database.converters.AdvanceOptionsTypeConverter
import com.zstronics.ceibro.data.database.converters.ListConverters
import com.zstronics.ceibro.data.database.converters.ProjectSubTaskListTypeConverter
import com.zstronics.ceibro.data.database.dao.TaskDao
import com.zstronics.ceibro.data.database.models.tasks.AdvanceOptions
import com.zstronics.ceibro.data.database.models.tasks.ProjectTask

@Database(
    entities = [ProjectTask::class, AdvanceOptions::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    ListConverters::class, AdvanceOptionsTypeConverter::class,
    ProjectSubTaskListTypeConverter::class
)
abstract class CeibroDatabase : RoomDatabase() {
    abstract fun getTasksDao(): TaskDao

    companion object {
        const val DB_NAME = "ceibro_app.db"
    }
}