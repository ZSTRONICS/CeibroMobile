package com.zstronics.ceibro.ui.projectv2.projectdetailv2

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProjectDetailV2VM @Inject constructor(
    override val viewState: ProjectDetailV2State,
) : HiltBaseViewModel<IProjectDetailV2.State>(), IProjectDetailV2.ViewModel {
}