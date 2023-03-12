package com.zstronics.ceibro.ui.projects.newproject.group

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.databinding.FragmentProjectGroupBinding
import com.zstronics.ceibro.ui.projects.newproject.group.adapter.ProjectGroupsAdapter
import com.zstronics.ceibro.ui.projects.newproject.group.addnewgroup.AddNewGroupSheet
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ProjectGroupFragment(private val projectLive: MutableLiveData<AllProjectsResponse.Projects>) :
    BaseNavViewModelFragment<FragmentProjectGroupBinding, IProjectGroup.State, ProjectGroupVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectGroupVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_project_group
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
                viewModel.addGroup(projectLive.value?.id, groupText)
            }
            sheet.show(childFragmentManager, "AddNewStatusSheet")
        }
    }

    @Inject
    lateinit var adapter: ProjectGroupsAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getGroups(projectLive.value?.id)
        mViewDataBinding.groupsRV.adapter = adapter
        viewModel.groups.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.setList(it)
            }
        }

        adapter.simpleChildItemClickListener =
            { childView: View, position: Int, data: ProjectGroup ->
                val popUpWindowObj =
                    popUpMenu(
                        position = position,
                        v = childView,
                        groupId = data.id,
                        groupName = data.name,
                        projectId = data.project
                    )
                popUpWindowObj.showAsDropDown(
                    childView.findViewById(R.id.optionMenu),
                    0,
                    1
                )
            }
    }

    private fun popUpMenu(
        position: Int,
        v: View,
        groupId: String,
        groupName: String,
        projectId: String
    ): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_edit_remove_menu, null)

        val edit = view.findViewById<View>(R.id.edit)
        val remove = view.findViewById<LinearLayoutCompat>(R.id.remove)

        edit.setOnClick {
            val sheet = AddNewGroupSheet(groupName)
            sheet.onGroupEdited = { groupText ->
                viewModel.updateGroup(projectId, groupId, groupText)
            }
            sheet.show(childFragmentManager, "AddNewStatusSheet")
            popupWindow.dismiss()
        }
        remove.setOnClick {
            viewModel.deleteGroup(projectId, position, groupId)
            popupWindow.dismiss()
        }

        popupWindow.isFocusable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 13f
        return popupWindow
    }
}