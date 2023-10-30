package com.zstronics.ceibro.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zstronics.ceibro.data.database.converters.*
import com.zstronics.ceibro.data.database.converters.v2.*
import com.zstronics.ceibro.data.database.dao.*
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.database.models.subtask.*
import com.zstronics.ceibro.data.database.models.tasks.AdvanceOptions
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.SubTaskStatusCount
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.models.TasksV2DatabaseEntitySingle
import com.zstronics.ceibro.data.repos.task.models.TopicsV2DatabaseEntity
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity

@Database(
    entities = [
        CeibroTask::class,
        AdvanceOptions::class,
        SubTaskStatusCount::class,
        TaskMember::class,
        AllSubtask::class,
        AssignedTo::class,
        Viewer::class,
        SubTaskAdvanceOptions::class,
        SubTaskStateItem::class,
        SubTaskComments::class,
        TaskDataOfSubTask::class,
        SubTaskProject::class,
        FilesAttachments::class,
        RejectionComment::class,
        TasksV2DatabaseEntity::class,
        TasksV2DatabaseEntitySingle::class,
        CeibroTaskV2::class,
        Events::class,
        TopicsV2DatabaseEntity::class,
        ProjectsV2DatabaseEntity::class,
        AllCeibroConnections.CeibroConnection::class,
        NewTaskV2Entity::class
    ],
    version = 73,
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
    FilesAttachmentsListTypeConverter::class,
    FilesAttachmentsTypeConverter::class,
    /// v2
    CeibroTaskV2TypeConverter::class,
    CeibroTaskAndEventsV2ResponseTypeConverter::class,
    CeibroTaskAndEventsV2TypeConverter::class,
    CeibroSingleTaskEntityV2TypeConverter::class,
    AssignedToStateListTypeConverter::class,
    AssignToStateDataTypeConverter::class,
    TaskMemberDetailTypeConverter::class,
    ProjectOfTaskTypeConverter::class,
    TopicTypeConverter::class,
    TaskFilesListTypeConverter::class,
    EventFilesListTypeConverter::class,
    EventsListTypeConverter::class,
    CommentDataTypeConverter::class,
    EventsDataListTypeConverter::class,
    ForwardDataTypeConverter::class,
    EventsDataTypeConverter::class,
    InvitedNumbersListTypeConverter::class,
    TopicsResponseTypeConverter::class,
    TopicDataListTypeConverter::class,
    ProjectsV2ListTypeConverter::class,
    OwnerV2TypeConverter::class,
    UserCeibroDataTypeConverter::class,
    AssignedToStateTypeConverter::class,
    LocalFilesToStoreTypeConverter::class,
    AttachmentTypesConverter::class,
)
abstract class CeibroDatabase : RoomDatabase() {
    @Deprecated("This dao is deprecated we are using v2 from now")
    abstract fun getTasksDao(): TaskDao

    @Deprecated("This dao is deprecated we are using v2 from now")
    abstract fun getSubTaskDao(): SubTaskDao

    @Deprecated("This dao is deprecated we are using v2 from now")
    abstract fun getFileAttachmentsDao(): FileAttachmentsDao
    abstract fun getTaskV2sDao(): TaskV2Dao
    abstract fun getTopicsV2Dao(): TopicsV2Dao
    abstract fun getProjectsV2Dao(): ProjectsV2Dao
    abstract fun getConnectionsV2Dao(): ConnectionsV2Dao
    abstract fun getDraftNewTaskV2Dao(): DraftNewTaskV2Dao

    companion object {
        const val DB_NAME = "ceibro_app.db"
    }
}