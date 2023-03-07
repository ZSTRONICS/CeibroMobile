package com.zstronics.ceibro.ui.projects.newproject.group

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentProjectGroupBinding
import com.zstronics.ceibro.databinding.FragmentProjectOverviewBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProjectGroupFragment :
    BaseNavViewModelFragment<FragmentProjectGroupBinding, IProjectGroup.State, ProjectGroupVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectGroupVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_project_group
    override fun toolBarVisibility(): Boolean = true
    override fun onClick(id: Int) {
        when (id) {
        }
    }
}