package com.zstronics.ceibro.ui.projectv2.projectdetailv2.projectinfo

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProjectInfoV2VM @Inject constructor(
    override val viewState: ProjectInfoV2State,
) : HiltBaseViewModel<IProjectInfoV2.State>(), IProjectInfoV2.ViewModel {
}