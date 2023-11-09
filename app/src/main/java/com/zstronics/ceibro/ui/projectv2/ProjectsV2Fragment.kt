package com.zstronics.ceibro.ui.projectv2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentProjectsV2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProjectsV2Fragment :
    BaseNavViewModelFragment<FragmentProjectsV2Binding, IProjectsV2.State, ProjectsV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_projects_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {

        when (id) {

            R.id.cl_AddNewProject -> {
                navigate(R.id.newProjectV2Fragment)
            }

            R.id.tvNewProject -> {
                navigate(R.id.newProjectV2Fragment)
            }

            R.id.cl_new -> {
                navigate(R.id.newProjectV2Fragment)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentManager: FragmentManager = childFragmentManager
        val adapter = ProjectTabLayoutAdapter(fragmentManager)
        mViewDataBinding.viewPager.adapter = adapter

        mViewDataBinding.tabLayout.setupWithViewPager(mViewDataBinding.viewPager)
    }
}