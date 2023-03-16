package com.zstronics.ceibro.ui.projects.newproject.role

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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.databinding.FragmentProjectRoleBinding
import com.zstronics.ceibro.ui.projects.newproject.overview.ownersheet.ProjectStateHandler
import com.zstronics.ceibro.ui.projects.newproject.role.adapter.ProjectRolesAdapter
import com.zstronics.ceibro.ui.projects.newproject.role.addrole.AddNewRoleSheet
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ProjectRoleFragment(
    private val projectStateHandler: ProjectStateHandler,
    private val projectLive: MutableLiveData<AllProjectsResponse.Projects>,
    private val availableMembers: LiveData<List<Member>>
) :
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
    }

    override fun onPrepareOptionsMenu(menu: Menu) {

        val menuItem = menu.findItem(R.id.itemAdd)
        val addMenuLayout = menuItem.actionView as ConstraintLayout
        val addMenuBtn = addMenuLayout.findViewById<AppCompatButton>(R.id.addMenuBtn)

        addMenuBtn.setOnClick {
            val sheet =
                availableMembers.value?.let { availableMembers ->
                    AddNewRoleSheet(
                        projectLive.value?.id,
                        availableMembers as ArrayList<Member>,
                        null
                    )
                }
            sheet?.onAdd = { roleData ->
                viewModel.createRoleAPI(roleData) {
                    projectStateHandler.onMemberAdd()
                    sheet?.hideSheet()
                }
            }
            sheet?.isCancelable = false
            sheet?.show(childFragmentManager, "AddNewRoleSheet")
        }
    }

    @Inject
    lateinit var adapter: ProjectRolesAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getRoles(projectLive.value?.id)
        mViewDataBinding.rolesRV.adapter = adapter
        viewModel.roles.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }

        adapter.simpleChildItemClickListener =
            { childView: View, position: Int, data: ProjectRolesResponse.ProjectRole ->
                val popUpWindowObj = popUpMenu(position, childView, data)
                popUpWindowObj.showAsDropDown(
                    childView.findViewById(R.id.optionMenu),
                    0,
                    1
                )
            }
    }

    private fun editRole(
        position: Int,
        roleData: ProjectRolesResponse.ProjectRole
    ) {
        val sheet =
            availableMembers.value?.let { availableMembers ->
                AddNewRoleSheet(
                    projectLive.value?.id,
                    availableMembers as ArrayList<Member>,
                    roleData
                )
            }
        sheet?.onUpdate = { updatedRole ->
            viewModel.updateRoleAPI(roleData.id, updatedRole) {
                projectStateHandler.onMemberAdd()
                sheet?.hideSheet()
            }
        }
        sheet?.isCancelable = false
        sheet?.show(childFragmentManager, "AddNewRoleSheet")
    }

    private fun popUpMenu(
        position: Int,
        v: View,
        data: ProjectRolesResponse.ProjectRole
    ): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_edit_remove_menu, null)

        val edit = view.findViewById<View>(R.id.edit)
        val remove = view.findViewById<LinearLayoutCompat>(R.id.remove)

        edit.setOnClick {
            editRole(position, data)
            popupWindow.dismiss()
        }
        remove.setOnClick {
            viewModel.deleteRole(position, data)
            projectStateHandler.onMemberAdd()
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