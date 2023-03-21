package com.zstronics.ceibro.ui.socket

import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.data.repos.projects.member.GetProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment

object LocalEvents {
    class TaskCreatedEvent
    data class SubTaskCreatedEvent(val taskId: String)
    data class NewSubTaskComment(val newComment: SubTaskComments, val commentId: String)
    data class UploadFilesToServer(
        val request: AttachmentUploadRequest,
        val fileUriList: ArrayList<SubtaskAttachment?>
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


    data class ProjectCreatedEvent(val newProject: AllProjectsResponse.Projects)
    class ProjectRefreshEvent


    data class RoleCreatedEvent(val newRole: ProjectRolesResponse.ProjectRole)
    data class RoleRefreshEvent(val projectId: String)


    data class GroupCreatedEvent(val newGroup: ProjectGroup)
    data class GroupRefreshEvent(val projectId: String)


    data class ProjectMemberAddedEvent(val newMember: List<GetProjectMemberResponse.ProjectMember>)
    data class ProjectMemberUpdatedEvent(val updatedMember: GetProjectMemberResponse.ProjectMember)
    data class ProjectMemberRefreshEvent(val projectId: String)

}