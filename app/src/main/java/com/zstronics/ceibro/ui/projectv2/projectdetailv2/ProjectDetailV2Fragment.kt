package com.zstronics.ceibro.ui.projectv2.projectdetailv2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.databinding.FragmentProjectDetailV2Binding
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class ProjectDetailV2Fragment :
    BaseNavViewModelFragment<FragmentProjectDetailV2Binding, IProjectDetailV2.State, ProjectDetailV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectDetailV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_project_detail_v2
    override fun toolBarVisibility(): Boolean = false
    private var tabTitles = listOf<String>()
    val drawingFileClickListener: ((view: View, data: DrawingV2, tag: String) -> Unit)? = null

    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                navigateBack()
            }
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabTitles = listOf(getString(R.string.detail), getString(R.string.drawing))
        val adapter = ProjectDetailTabLayoutAdapter(requireActivity()) { view, data, tag ->
            CookiesManager.drawingFileForLocation.value = data
            CookiesManager.cameToLocationViewFromProject = true
            CookiesManager.openingNewLocationFile = true
            navigateBack()
            EventBus.getDefault().postSticky(LocalEvents.LoadDrawingInLocation())
        }
        mViewDataBinding.viewPager.adapter = adapter

        TabLayoutMediator(
            mViewDataBinding.tabLayout,
            mViewDataBinding.viewPager
        ) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        mViewDataBinding.tvProjectName.text = CookiesManager.projectNameForDetails
    }

}