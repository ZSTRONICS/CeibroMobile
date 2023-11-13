package com.zstronics.ceibro.ui.projectv2.projectdetailv2.projectinfo

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentProjectInfoV2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProjectInfoV2Fragment :
    BaseNavViewModelFragment<FragmentProjectInfoV2Binding, IProjectInfoV2.State, ProjectInfoV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectInfoV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_project_info_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {

        }
    }
}