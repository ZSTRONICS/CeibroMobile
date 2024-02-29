package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OngoingVM @Inject constructor(
    override val viewState: OngoingState,
) : HiltBaseViewModel<IOngoing.State>(), IOngoing.ViewModel {
}