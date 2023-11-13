package com.zstronics.ceibro.ui.projectv2

import android.content.Context
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProjectsV2VM @Inject constructor(
    override val viewState: ProjectsV2State,
) : HiltBaseViewModel<IProjectsV2.State>(), IProjectsV2.ViewModel {
    override fun getProjectName(context: Context) {

    }
}