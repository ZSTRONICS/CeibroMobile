package com.zstronics.ceibro.ui.projects.newproject.members

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
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.data.repos.projects.member.GetProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.databinding.FragmentProjectMembersBinding
import com.zstronics.ceibro.ui.projects.newproject.members.adapter.ProjectMembersAdapter
import com.zstronics.ceibro.ui.projects.newproject.members.memberprofile.ProjectAddNewMemberSheet
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ProjectMembersFragment(
    private val projectLive: MutableLiveData<AllProjectsResponse.Projects>,
    private val allConnections: LiveData<ArrayList<MyConnection>>
) :
    BaseNavViewModelFragment<FragmentProjectMembersBinding, IProjectMembers.State, ProjectMembersVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectMembersVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_project_members
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
            val sheet = allConnections.value?.let { allConnections ->
                viewModel.groups.value?.let { groups ->
                    viewModel.roles.value?.let { roles ->
                        ProjectAddNewMemberSheet(
                            projectId = projectLive.value?.id,
                            connections = allConnections,
                            groups = groups,
                            roles = roles
                        )
                    }
                }
            }
            sheet?.onMemberAdd = { body ->
                viewModel.createMember(projectLive.value?.id, body) {
                    sheet?.dismiss()
                }
            }
            sheet?.isCancelable = false
            sheet?.show(childFragmentManager, "ProjectAddNewMemberSheet")
        }
    }

    @Inject
    lateinit var adapter: ProjectMembersAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getMembers(projectLive.value?.id)
        viewModel.getGroups(projectLive.value?.id)
        viewModel.getRoles(projectLive.value?.id)

        mViewDataBinding.membersRV.adapter = adapter
        viewModel.groupMembers.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.setList(it)
            }
        }

        adapter.simpleChildItemClickListener =
            { childView: View, position: Int, data: GetProjectMemberResponse.ProjectMember ->
                val popUpWindowObj =
                    popUpMenu(
                        position = position,
                        v = childView,
                        member = data
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
        member: GetProjectMemberResponse.ProjectMember,
    ): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_edit_remove_menu, null)

        val edit = view.findViewById<View>(R.id.edit)
        val remove = view.findViewById<LinearLayoutCompat>(R.id.remove)

        edit.setOnClick {

        }
        remove.setOnClick {
            viewModel.deleteMember(position)
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