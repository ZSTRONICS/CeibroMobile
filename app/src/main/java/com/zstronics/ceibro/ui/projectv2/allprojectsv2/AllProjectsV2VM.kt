package com.zstronics.ceibro.ui.projectv2.allprojectsv2

import android.content.Context
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AllProjectsV2VM @Inject constructor(
    override val viewState: AllProjectsV2State,
) : HiltBaseViewModel<IAllProjectV2.State>(), IAllProjectV2.ViewModel {
    override fun getProjectName(context: Context) {

    }
}