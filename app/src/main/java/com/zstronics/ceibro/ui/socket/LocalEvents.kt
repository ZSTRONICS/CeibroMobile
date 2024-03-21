package com.zstronics.ceibro.ui.socket

import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.inbox.CeibroInboxV2
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import com.zstronics.ceibro.data.database.models.tasks.CeibroDrawingPins
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.data.repos.dashboard.attachment.v2.AttachmentUploadV2Request
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.data.repos.projects.member.GetProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment

object LocalEvents {
    data class SubTaskCreatedEvent(val taskId: String)
    data class NewSubTaskComment(val newComment: SubTaskComments, val commentId: String)
    data class UploadFilesToServer(
        val request: AttachmentUploadRequest,
        val fileUriList: ArrayList<SubtaskAttachment?>
    )

    data class UploadFilesToV2Server(
        val request: AttachmentUploadV2Request
    )

    object AllFilesUploaded
    data class CreateNotification(
        val moduleId: String,
        val moduleName: String,
        val notificationTitle: String,
        val isOngoing: Boolean,
        val indeterminate: Boolean,
        val notificationIcon: Int = R.drawable.icon_upload
    )

    data class CreateSimpleNotification(
        val moduleId: String,
        val moduleName: String,
        val notificationTitle: String,
        val notificationDescription: String,
        val isOngoing: Boolean,
        val indeterminate: Boolean,
        val notificationIcon: Int = R.drawable.app_logo,
        val isTaskCreated: Boolean,
    )

    data class ApplyFilterOnTask(
        val projectId: String,
        val selectedStatus: String,
        val selectedDueDate: String,
        val assigneeToMembers: List<TaskMember>?
    )

    data class ApplyFilterOnSubTask(
        val projectId: String,
        val selectedStatus: String,
        val selectedDueDate: String,
        val assigneeToMembers: List<TaskMember>?
    )

    object ClearTaskFilters
    object ClearSubtaskFilters


    data class ApplySearchOnTask(
        val query: String?
    )

    data class ApplySearchOnSubTask(
        val query: String?
    )

    data class ApplySearchOnAdmins(
        val query: String?
    )

    data class ApplySearchOnAllUsers(
        val query: String?
    )


    data class ProjectCreatedEvent(val newProject: AllProjectsResponse.Projects)
    class ProjectRefreshEvent


    data class RoleCreatedEvent(val newRole: ProjectRolesResponse.ProjectRole)
    data class RoleRefreshEvent(val projectId: String)


    data class GroupCreatedEvent(val newGroup: ProjectGroup)
    data class GroupRefreshEvent(val projectId: String)


    data class ProjectMemberAddedEvent(val newMember: List<GetProjectMemberResponse.ProjectMember>)
    data class ProjectMemberUpdatedEvent(val updatedMember: GetProjectMemberResponse.ProjectMember)
    data class ProjectMemberRefreshEvent(val projectId: String)


    data class RefreshRootDocumentEvent(val projectId: String)
    data class RefreshFolderEvent(val projectId: String, val folderId: String)


    class UserDataUpdated
    class ConnectionRefreshEvent
    class InvitationRefreshEvent

    class LogoutUserEvent
    object UpdateConnections
    class RefreshTasksData
    class RefreshInboxData
    data class UpdateInboxItemSeen(val inboxTask: CeibroInboxV2)
    data class RefreshInboxSingleEvent(val inboxTask: CeibroInboxV2)
    class RefreshProjectsData
    data class RefreshFloorsData(val projectId: String)
    data class RefreshGroupsData(val projectId: String)
    data class RefreshDeletedGroupData(val groupId: String)
    class RefreshAllEvents
    class RefreshTaskFiles
    class TaskFailedToDone
    data class UpdateTaskInDetails(val task: CeibroTaskV2?)
    data class TaskSeenEvent(val task: CeibroTaskV2?)
    data class TaskDoneEvent(val task: CeibroTaskV2?, val taskEvent: Events)
    data class TaskEvent(val events: Events)
    data class TaskEventUpdate(val events: Events?)

    class InitSocketEventCallBack
    class LoadDrawingInLocation
    class LoadLocationProjectFragmentInLocation
    class LoadDrawingFragmentInLocation
    class LoadViewDrawingFragmentInLocation
    class ReloadLocationFragmentInstance
    data class UpdateGroupDrawings(val projectID: String)

    data class RefreshDrawingPins(val pinData: CeibroDrawingPins?)
    data class UpdateDrawingPins(val pinData: CeibroDrawingPins?)
    class UpdateFileDownloadProgress
}