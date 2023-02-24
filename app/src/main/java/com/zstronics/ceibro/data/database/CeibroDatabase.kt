package com.zstronics.ceibro.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zstronics.ceibro.data.database.converters.*
import com.zstronics.ceibro.data.database.dao.FileAttachmentsDao
import com.zstronics.ceibro.data.database.dao.SubTaskDao
import com.zstronics.ceibro.data.database.dao.TaskDao
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.database.models.subtask.*
import com.zstronics.ceibro.data.database.models.tasks.AdvanceOptions
import com.zstronics.ceibro.data.database.models.tasks.SubTaskStatusCount
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember

@Database(
    entities = [CeibroTask::class, AdvanceOptions::class, SubTaskStatusCount::class, TaskMember::class, AllSubtask::class, AssignedTo::class,
        Viewer::class, SubTaskAdvanceOptions::class, SubTaskStateItem::class, SubTaskComments::class, TaskDataOfSubTask::class, SubTaskProject::class, FilesAttachments::class, RejectionComment::class],
    version = 44,
    exportSchema = false
)
@TypeConverters(
    SubTaskAdvanceOptionsTypeConverter::class,
    AssignToListTypeConverter::class,
    ViewerListTypeConverter::class,
    AdvanceOptionsTypeConverter::class,
    ListConverters::class,
    TaskMemberListTypeConverter::class,
    TaskMemberTypeConverter::class,
    TaskProjectTypeConverter::class,
    SubTaskStatusCountTypeConverter::class,
    SubTaskStateListTypeConverter::class,
    SubTaskCommentsTypeConverter::class,
    SubTaskCommentsListTypeConverter::class,
    TaskDataOfSubTaskTypeConverter::class,
    SubTaskProjectTypeConverter::class,
    SubTaskRejectionCommentsListTypeConverter::class,
)
abstract class CeibroDatabase : RoomDatabase() {
    abstract fun getTasksDao(): TaskDao
    abstract fun getSubTaskDao(): SubTaskDao
    abstract fun getFileAttachmentsDao(): FileAttachmentsDao

    companion object {
        const val DB_NAME = "ceibro_app.db"
    }
}