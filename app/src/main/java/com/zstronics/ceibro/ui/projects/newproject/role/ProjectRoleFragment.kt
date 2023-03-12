package com.zstronics.ceibro.ui.projects.newproject.role

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.databinding.FragmentProjectRoleBinding
import com.zstronics.ceibro.ui.projects.newproject.group.addnewgroup.AddNewGroupSheet
import com.zstronics.ceibro.ui.projects.newproject.role.adapter.ProjectRolesAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ProjectRoleFragment(private val projectLive: MutableLiveData<AllProjectsResponse.Projects>) :
    BaseNavViewModelFragment<FragmentProjectRoleBinding, IProjectRole.State, ProjectRoleVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectRoleVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_project_role
    override fun getToolBarTitle() =
        projectLive.value?.title ?: getString(R.string.new_projects_title)

    override fun toolBarVisibility(): Boolean = true
    override fun hasOptionMenu(): Boolean = true
    override fun onClick(id: Int) {
        when (id) {
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {

        val menuItem = menu.findItem(R.id.itemAdd)
        val addMenuLayout = menuItem.actionView as ConstraintLayout
        val addMenuBtn = addMenuLayout.findViewById<AppCompatButton>(R.id.addMenuBtn)

        addMenuBtn.setOnClick {
            val sheet = AddNewGroupSheet("")
            sheet.onGroupAdd = { groupText ->
//                viewModel.addGroup(projectLive.value?.id, groupText)
            }
            sheet.show(childFragmentManager, "AddNewStatusSheet")
        }
    }

    @Inject
    lateinit var adapter: ProjectRolesAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getRoles("63b42df8adc12f7c2980d7d9")
//        viewModel.getRoles(projectLive.value?.id)
        mViewDataBinding.rolesRV.adapter = adapter
        viewModel.roles.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }
    }
}