package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailfiles

import android.os.Bundle
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskDetailFilesV2VM @Inject constructor(
    override val viewState: TaskDetailFilesV2State,
) : HiltBaseViewModel<ITaskDetailFilesV2.State>(), ITaskDetailFilesV2.ViewModel {

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

    }
}