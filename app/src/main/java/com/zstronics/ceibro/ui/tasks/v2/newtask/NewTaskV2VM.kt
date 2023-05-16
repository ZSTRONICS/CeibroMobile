package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.NewTaskRequestNoAdvanceOptions
import com.zstronics.ceibro.data.repos.task.models.UpdateDraftTaskRequestNoAdvanceOptions
import com.zstronics.ceibro.data.repos.task.models.UpdateTaskRequestNoAdvanceOptions
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewTaskV2VM @Inject constructor(
    override val viewState: NewTaskV2State,
    private val sessionManager: SessionManager,
) : HiltBaseViewModel<INewTaskV2.State>(), INewTaskV2.ViewModel {
    val user = sessionManager.getUser().value


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

    }

}