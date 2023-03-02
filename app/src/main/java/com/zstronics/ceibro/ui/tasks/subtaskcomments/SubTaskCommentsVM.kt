package com.zstronics.ceibro.ui.tasks.subtaskcomments

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.AllCommentsResponse
import com.zstronics.ceibro.data.repos.task.models.SubTaskRejections
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubTaskCommentsVM @Inject constructor(
    override val viewState: SubTaskCommentsState,
    private val taskRepository: ITaskRepository
) : HiltBaseViewModel<ISubTaskComments.State>(), ISubTaskComments.ViewModel {
    private val _allComment: MutableLiveData<ArrayList<SubTaskComments>> = MutableLiveData()
    val allComment: LiveData<ArrayList<SubTaskComments>> = _allComment

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        val subtaskParcel: AllSubtask? = bundle?.getParcelable("subtask")

        subtaskParcel?.id?.let { loadComments(it) }
    }

    private fun loadComments(subTaskId: String) {
        launch {
            taskRepository.getAllCommentsOfSubtask(subTaskId) { isSuccess, error, allComment ->
                if (isSuccess) {
                    if (allComment.isEmpty()) {
                        alert("No Comments on this subtask")
                    }
                    _allComment.value = allComment
                } else alert(error)
            }
        }
    }
}