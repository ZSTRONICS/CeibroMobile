package com.zstronics.ceibro.ui.projectv2.hiddenprojectv2

import android.content.Context
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HiddenProjectsV2VM @Inject constructor(
    override val viewState: HiddenProjectsV2State,
) : HiltBaseViewModel<IHiddenProjectV2.State>(), IHiddenProjectV2.ViewModel {
    override fun getProjectName(context: Context) {

    }
}