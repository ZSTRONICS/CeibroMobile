package com.zstronics.ceibro.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zstronics.ceibro.data.database.converters.*
import com.zstronics.ceibro.data.database.converters.v2.*
import com.zstronics.ceibro.data.database.dao.*
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.database.models.inbox.CeibroInboxV2
import com.zstronics.ceibro.data.database.models.projects.CeibroDownloadDrawingV2
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.database.models.subtask.*
import com.zstronics.ceibro.data.database.models.tasks.AdvanceOptions
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.LocalTaskDetailFiles
import com.zstronics.ceibro.data.database.models.tasks.SubTaskStatusCount
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
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
        CeibroTaskV2::class,
        CeibroInboxV2::class,
        CeibroConnectionGroupV2::class,
        Events::class,
        TopicsV2DatabaseEntity::class,
        CeibroProjectV2::class,
        CeibroFloorV2::class,
        CeibroGroupsV2::class,
        CeibroDownloadDrawingV2::class,
        CeibroDrawingPins::class,
        AllCeibroConnections.CeibroConnection::class,
        NewTaskV2Entity::class,
        LocalTaskDetailFiles::class,
    ],
    version = 16,
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
    AssignedToStateListTypeConverter::class,
    AssignToStateDataTypeConverter::class,
    TaskMemberDetailTypeConverter::class,
    ProjectOfTaskTypeConverter::class,
    TagTypeConverter::class,
    TagsListTypeConverter::class,
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
    FloorsV2ListTypeConverter::class,
    GroupsV2ListTypeConverter::class,
    DrawingsV2ListTypeConverter::class,
    UserCeibroDataTypeConverter::class,
    AssignedToStateTypeConverter::class,
    LocalFilesToStoreTypeConverter::class,
    AttachmentTypesConverter::class,
    DrawingV2TypeConverter::class,
    AddLocationTaskTypeConverter::class,
    CeibroDrawingPinsTypeConverter::class,
    PinTaskDataTypeConverter::class,
    ActionFilesDataListTypeConverter::class,
    ActionProjectDataTypeConverter::class,
    ActionDataTaskTypeConverter::class,
    GroupContactV2ListTypeConverter::class,
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
    abstract fun getFloorsV2Dao(): FloorsV2Dao
    abstract fun getDownloadDrawingDao(): DownloadedDrawingV2Dao
    abstract fun getGroupsV2Dao(): GroupsV2Dao
    abstract fun getInboxV2Dao(): InboxV2Dao
    abstract fun getDrawingPinsV2Dao(): DrawingPinsV2Dao
    abstract fun getConnectionsV2Dao(): ConnectionsV2Dao
    abstract fun getTaskDetailFilesV2Dao(): TaskDetailFilesV2Dao
    abstract fun getConnectionGroupV2Dao(): ConnectionGroupV2Dao
    abstract fun getDraftNewTaskV2Dao(): DraftNewTaskV2Dao

    companion object {
        const val DB_NAME = "ceibro_database.db"
    }
}