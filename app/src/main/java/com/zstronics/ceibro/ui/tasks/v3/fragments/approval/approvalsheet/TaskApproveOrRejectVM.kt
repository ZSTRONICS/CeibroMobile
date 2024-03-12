package com.zstronics.ceibro.ui.tasks.v3.fragments.approval.approvalsheet

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
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


}