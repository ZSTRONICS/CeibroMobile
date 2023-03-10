package com.zstronics.ceibro.ui.tasks.subtaskrejections

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.models.SubTaskRejections
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubTaskRejectionVM @Inject constructor(
    override val viewState: SubTaskRejectionState,
    val sessionManager: SessionManager,
    private val taskRepository: ITaskRepository
) : HiltBaseViewModel<ISubTaskRejection.State>(), ISubTaskRejection.ViewModel {
    val user = sessionManager.getUser().value

    private val _subtaskRejections: MutableLiveData<List<SubTaskRejections>> = MutableLiveData(arrayListOf())
    val subtaskRejections: LiveData<List<SubTaskRejections>> = _subtaskRejections

    private val _subtask: MutableLiveData<AllSubtask?> = MutableLiveData()
    val subtask: LiveData<AllSubtask?> = _subtask

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        val subtaskParcel: AllSubtask? = bundle?.getParcelable("subtask")
        _subtask.value = subtaskParcel

        subtaskParcel?.id?.let { loadRejections(it) }
    }

    private fun loadRejections(subTaskId: String) {
        launch {
            loading(true)
            taskRepository.getSubtaskRejections(subTaskId) { isSuccess, error, subTaskRejections ->
                if (isSuccess) {
                    loading(false)
                    _subtaskRejections.postValue(subTaskRejections)

                    if (subTaskRejections.isEmpty()) {
                        loading(false,"No rejections found")
                    }

                } else loading(false, error)
            }
        }
    }

}