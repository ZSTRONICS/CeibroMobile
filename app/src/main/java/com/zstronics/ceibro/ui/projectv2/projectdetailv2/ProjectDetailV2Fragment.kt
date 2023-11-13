package com.zstronics.ceibro.ui.projectv2.projectdetailv2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentProjectDetailV2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProjectDetailV2Fragment :
    BaseNavViewModelFragment<FragmentProjectDetailV2Binding, IProjectDetailV2.State, ProjectDetailV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectDetailV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_project_detail_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                navigateBack()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentManager: FragmentManager = childFragmentManager
        val adapter = ProjectDetailTabLayoutAdapter(fragmentManager, lifecycle)
        mViewDataBinding.viewPager.adapter = adapter
        // mViewDataBinding.viewPager.isUserInputEnabled = false

        TabLayoutMediator(mViewDataBinding.tabLayout, mViewDataBinding.viewPager) { tab, position ->
            tab.text = "Detail"
        }.attach()
    }

}