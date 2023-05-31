package com.zstronics.ceibro.ui.tasks.v2.newtask.assignee

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AssigneeVM @Inject constructor(
    override val viewState: AssigneeState,
) : HiltBaseViewModel<IAssignee.State>(), IAssignee.ViewModel {
}