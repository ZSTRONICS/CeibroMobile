package com.zstronics.ceibro.ui.works

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WorksVM @Inject constructor(
    override val viewState: WorksState,
) : HiltBaseViewModel<IWorks.State>(), IWorks.ViewModel {
}