package com.zstronics.ceibro.ui.tasks.subtaskeditmembers

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubTaskEditMembersVM @Inject constructor(
    override val viewState: SubTaskEditMembersState,
) : HiltBaseViewModel<ISubTaskEditMembers.State>(), ISubTaskEditMembers.ViewModel {
}