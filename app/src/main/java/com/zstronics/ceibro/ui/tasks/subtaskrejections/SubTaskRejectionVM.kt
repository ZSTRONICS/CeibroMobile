package com.zstronics.ceibro.ui.tasks.subtaskrejections

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubTaskRejectionVM @Inject constructor(
    override val viewState: SubTaskRejectionState,
) : HiltBaseViewModel<ISubTaskRejection.State>(), ISubTaskRejection.ViewModel {
}