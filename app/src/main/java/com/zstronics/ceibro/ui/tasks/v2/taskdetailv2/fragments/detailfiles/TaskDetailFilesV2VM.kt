package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailfiles

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.dao.DrawingPinsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.dao.InboxV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.LocalTaskDetailFiles
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskDetailFilesV2VM @Inject constructor(
    override val viewState: TaskDetailFilesV2State,
    val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
    private val remoteTask: TaskRemoteDataSource,
    val dashboardRepository: IDashboardRepository,
    val taskDao: TaskV2Dao,
    val groupsV2Dao: GroupsV2Dao,
    private val inboxV2Dao: InboxV2Dao,
    val drawingPinsDao: DrawingPinsV2Dao,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao
) : HiltBaseViewModel<ITaskDetailFilesV2.State>(), ITaskDetailFilesV2.ViewModel {

    val _taskDetail: MutableLiveData<CeibroTaskV2> = MutableLiveData()
    val taskDetail: LiveData<CeibroTaskV2> = _taskDetail
    val originalTask: MutableLiveData<CeibroTaskV2> = MutableLiveData()

    val _allDetailFiles: MutableLiveData<MutableList<LocalTaskDetailFiles>> = MutableLiveData()
    val allDetailFiles: LiveData<MutableList<LocalTaskDetailFiles>> = _allDetailFiles
    val originalAllDetailFiles: MutableLiveData<MutableList<LocalTaskDetailFiles>> = MutableLiveData()

    val _photoFiles: MutableLiveData<MutableList<LocalTaskDetailFiles>> = MutableLiveData()
    val photoFiles: LiveData<MutableList<LocalTaskDetailFiles>> = _photoFiles
    val originalPhotoFiles: MutableLiveData<MutableList<LocalTaskDetailFiles>> = MutableLiveData()

    val _documentFiles: MutableLiveData<MutableList<LocalTaskDetailFiles>> = MutableLiveData()
    val documentFiles: LiveData<MutableList<LocalTaskDetailFiles>> = _documentFiles
    val originalDocumentFiles: MutableLiveData<MutableList<LocalTaskDetailFiles>> = MutableLiveData()


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        launch {
            val taskData: CeibroTaskV2? = CeibroApplication.CookiesManager.taskDataForDetails
            val taskDataFromNotification: CeibroTaskV2? =
                CeibroApplication.CookiesManager.taskDataForDetailsFromNotification
            val allFiles: List<LocalTaskDetailFiles>? = CeibroApplication.CookiesManager.taskDetailFiles.value

            taskData?.let { task ->
                originalTask.postValue(task)
                _taskDetail.postValue(task)
            } ?: kotlin.run {
                taskDataFromNotification?.let { taskNotification ->
                    originalTask.postValue(taskNotification)
                    _taskDetail.postValue(taskNotification)
                }
            }

            allFiles?.let { files ->
                val sortedFiles = files.sortedByDescending { it.createdAt }.toMutableList()

                originalAllDetailFiles.postValue(sortedFiles)
                _allDetailFiles.postValue(sortedFiles)

                val allPhotosList: ArrayList<LocalTaskDetailFiles> = arrayListOf()
                val allFilesList: ArrayList<LocalTaskDetailFiles> = arrayListOf()

                for (item in sortedFiles) {
                    when (item.fileTag) {
                        AttachmentTags.Image.tagValue -> {
                            allPhotosList.add(item)
                        }

                        AttachmentTags.Drawing.tagValue -> {
                            allPhotosList.add(item)
                        }

                        AttachmentTags.ImageWithComment.tagValue -> {
                            allPhotosList.add(item)
                        }

                        AttachmentTags.File.tagValue -> {
                            allFilesList.add(item)
                        }
                    }
                }

                originalPhotoFiles.postValue(allPhotosList.toMutableList())
                _photoFiles.postValue(allPhotosList.toMutableList())

                originalDocumentFiles.postValue(allFilesList.toMutableList())
                _documentFiles.postValue(allFilesList.toMutableList())

            }
        }
    }
}