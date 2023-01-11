package com.zstronics.ceibro.ui.tasks.newsubtask

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewSubTaskVM @Inject constructor(
    override val viewState: NewSubTaskState,
) : HiltBaseViewModel<INewSubTask.State>(), INewSubTask.ViewModel {
}