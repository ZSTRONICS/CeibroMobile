package com.zstronics.ceibro.ui.projects.newproject

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentCreateProjectMainBinding
import com.zstronics.ceibro.ui.projects.newproject.group.ProjectGroupFragment
import com.zstronics.ceibro.ui.projects.newproject.overview.ProjectOverviewFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CreateProjectMainFragment :
    BaseNavViewModelFragment<FragmentCreateProjectMainBinding, ICreateProjectMain.State, CreateProjectMainVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: CreateProjectMainVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_create_project_main
    override fun toolBarVisibility(): Boolean = true
    override fun getToolBarTitle() = getString(R.string.new_projects_title)
    val projectOverview = ProjectOverviewFragment()
    val projectGroup = ProjectGroupFragment()
    override fun onClick(id: Int) {
        when (id) {
            R.id.Overview -> {
                viewState.selectedTabId.value = CreateProjectTabs.Overview.name
                childFragmentManager.beginTransaction()
                    .replace(R.id.project_fragment_container, projectOverview).commit()

            }
            R.id.Group -> {
                viewState.selectedTabId.value = CreateProjectTabs.Group.name
                childFragmentManager.beginTransaction()
                    .replace(R.id.project_fragment_container, projectGroup).commit()
            }
            R.id.Role -> {
                viewState.selectedTabId.value = CreateProjectTabs.Role.name
                childFragmentManager.beginTransaction()
                    .replace(R.id.project_fragment_container, projectOverview).commit()
            }
            R.id.Member -> {
                viewState.selectedTabId.value = CreateProjectTabs.Member.name
                childFragmentManager.beginTransaction()
                    .replace(R.id.project_fragment_container, projectOverview).commit()
            }
            R.id.Document -> {
                viewState.selectedTabId.value = CreateProjectTabs.Document.name
                childFragmentManager.beginTransaction()
                    .replace(R.id.project_fragment_container, projectOverview).commit()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.beginTransaction()
            .replace(R.id.project_fragment_container, projectOverview).commit()
    }

    enum class CreateProjectTabs {
        Overview, Group, Role, Member, Document
    }
}