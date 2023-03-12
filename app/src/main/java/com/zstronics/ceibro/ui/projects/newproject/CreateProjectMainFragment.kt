package com.zstronics.ceibro.ui.projects.newproject

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.databinding.FragmentCreateProjectMainBinding
import com.zstronics.ceibro.ui.projects.newproject.group.ProjectGroupFragment
import com.zstronics.ceibro.ui.projects.newproject.members.ProjectMembersFragment
import com.zstronics.ceibro.ui.projects.newproject.overview.ProjectOverviewFragment
import com.zstronics.ceibro.ui.projects.newproject.overview.ownersheet.ProjectStateHandler
import com.zstronics.ceibro.ui.projects.newproject.role.ProjectRoleFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CreateProjectMainFragment :
    BaseNavViewModelFragment<FragmentCreateProjectMainBinding, ICreateProjectMain.State, CreateProjectMainVM>(),
    ProjectStateHandler {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: CreateProjectMainVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_create_project_main
    override fun toolBarVisibility(): Boolean = true
    override fun getToolBarTitle() =
        viewState.project.value?.title ?: getString(R.string.new_projects_title)

    override fun onClick(id: Int) {
        when (id) {
            R.id.Overview -> {
                viewState.selectedTabId.value = CreateProjectTabs.Overview.name
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.project_fragment_container,
                        ProjectOverviewFragment(this, viewState.project, viewModel.allConnections)
                    ).commit()

            }
            R.id.Group -> {
                viewState.selectedTabId.value = CreateProjectTabs.Group.name
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.project_fragment_container,
                        ProjectGroupFragment(viewState.project)
                    ).commit()
            }
            R.id.Role -> {
                viewState.selectedTabId.value = CreateProjectTabs.Role.name
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.project_fragment_container,
                        ProjectRoleFragment(viewState.project, viewModel.allConnections)
                    ).commit()
            }
            R.id.Member -> {
                viewState.selectedTabId.value = CreateProjectTabs.Member.name
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.project_fragment_container,
                        ProjectMembersFragment(viewState.project, viewModel.allConnections)
                    ).commit()
            }
            R.id.Document -> {
                viewState.selectedTabId.value = CreateProjectTabs.Document.name
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.project_fragment_container,
                        ProjectOverviewFragment(this, viewState.project, viewModel.allConnections)
                    ).commit()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.beginTransaction()
            .replace(
                R.id.project_fragment_container,
                ProjectOverviewFragment(this, viewState.project, viewModel.allConnections)
            ).commit()
    }

    enum class CreateProjectTabs {
        Overview, Group, Role, Member, Document
    }

    override fun onProjectCreated(project: AllProjectsResponse.Projects?) {
        viewModel.onProjectCreated(project)
    }
}