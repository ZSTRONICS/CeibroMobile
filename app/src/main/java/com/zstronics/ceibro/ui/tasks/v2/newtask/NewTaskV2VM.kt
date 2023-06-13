package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Request
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

@HiltViewModel
class NewTaskV2VM @Inject constructor(
    override val viewState: NewTaskV2State,
    sessionManager: SessionManager,
    private val taskRepository: ITaskRepository,
) : HiltBaseViewModel<INewTaskV2.State>(), INewTaskV2.ViewModel {
    val user = sessionManager.getUser().value
    val listOfImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val onlyImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val imagesWithComments: MutableLiveData<ArrayList<PickedImages>> =
        MutableLiveData(arrayListOf())
    val documents: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())

    var taskId = ""

    fun createNewTask(
        context: Context,
        doneImageRequired: Boolean,
        doneCommentsRequired: Boolean,
        success: (taskId: String) -> Unit,
    ) {

        if (viewState.taskTitle.value.toString() == "") {
            alert("Topic is required")
        } else if (viewState.assignToText.value.toString() == "") {
            alert("Assign to required")
        } else {
            val assignedToCeibroUsers =
                viewState.selectedContacts.value?.filter { it.isCeiborUser }
                    ?.map {
                        NewTaskV2Request.AssignedToState(
                            it.phoneNumber, it.userCeibroData?.id.toString()
                        )
                    } ?: listOf()
            val invitedNumbers = viewState.selectedContacts.value?.filter { !it.isCeiborUser }
                ?.map { it.phoneNumber } ?: listOf()
            val projectId = viewState.selectedProject.value?.id ?: ""
            val newTaskRequest = NewTaskV2Request(
                topic = viewState.selectedTopic.value?.id.toString(),
                project = projectId,
                assignedToState = assignedToCeibroUsers,
                dueDate = viewState.dueDate.value.toString(),
                creator = user?.id.toString(),
                description = viewState.description.value.toString(),
                invitedNumbers = invitedNumbers,
                doneImageRequired = doneImageRequired,
                doneCommentsRequired = doneCommentsRequired
            )

            launch {
                loading(true)
                taskRepository.newTaskV2(newTaskRequest) { isSuccess, taskId ->
                    if (isSuccess) {
                        loading(false, "")
                    } else {
                        loading(false, taskId)
                    }
                }
            }
        }
    }
}