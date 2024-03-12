package com.zstronics.ceibro.ui.tasks.v3.fragments.approval.approvalsheet

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

@HiltViewModel
class TaskApproveOrRejectVM @Inject constructor(
    override val viewState: TaskApproveOrRejectState,
    val sessionManager: SessionManager,
    val dashboardRepository: IDashboardRepository,
    private val taskDao: TaskV2Dao,
) : HiltBaseViewModel<ITaskApproveOrReject.State>(), ITaskApproveOrReject.ViewModel {
    val user = sessionManager.getUser().value

    val listOfImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val onlyImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val imagesWithComments: MutableLiveData<ArrayList<PickedImages>> =
        MutableLiveData(arrayListOf())
    val documents: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())


    private val _taskType: MutableLiveData<String> = MutableLiveData()
    val taskType: LiveData<String> = _taskType


    private val _task: MutableLiveData<CeibroTaskV2> = MutableLiveData()
    val task: LiveData<CeibroTaskV2> = _task


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val taskType = bundle?.getString("updateTaskType")
        val task: CeibroTaskV2? = bundle?.getParcelable("CeibroTaskV2")
        task?.let {
            this._task.value = it
        }
        taskType?.let {
            this._taskType.value = it
        }
    }

    fun filesCounter(): Int {
        return ((listOfImages.value?.size ?: 0) + (documents.value?.size ?: 0))
    }

}