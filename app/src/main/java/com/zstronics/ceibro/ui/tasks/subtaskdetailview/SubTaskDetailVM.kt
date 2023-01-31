package com.zstronics.ceibro.ui.tasks.subtaskdetailview

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubTaskDetailVM @Inject constructor(
    override val viewState: SubTaskDetailState,
) : HiltBaseViewModel<ISubTaskDetail.State>(), ISubTaskDetail.ViewModel {
    private val _subtask: MutableLiveData<AllSubtask> = MutableLiveData()
    val subtask: LiveData<AllSubtask> = _subtask

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val subtaskParcel: AllSubtask? = bundle?.getParcelable("subtask")
        _subtask.value = subtaskParcel

//        subtaskParcel?._id?.let { getSubTasks(it) }
    }

}