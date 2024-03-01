package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskV3OngoingVM @Inject constructor(
    override val viewState: TaskV3OngoingState,
) : HiltBaseViewModel<ITaskV3Ongoing.State>(), ITaskV3Ongoing.ViewModel {
}