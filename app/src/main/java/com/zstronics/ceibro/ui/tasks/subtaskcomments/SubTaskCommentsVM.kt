package com.zstronics.ceibro.ui.tasks.subtaskcomments

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubTaskCommentsVM @Inject constructor(
    override val viewState: SubTaskCommentsState,
) : HiltBaseViewModel<ISubTaskComments.State>(), ISubTaskComments.ViewModel {
}