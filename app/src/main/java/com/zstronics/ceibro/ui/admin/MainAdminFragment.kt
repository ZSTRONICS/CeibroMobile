package com.zstronics.ceibro.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentMainAdminBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import com.zstronics.ceibro.ui.admin.admins.AdminsFragment
import com.zstronics.ceibro.ui.admin.users.AllUsersFragment
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskFragment
import com.zstronics.ceibro.ui.tasks.task.TasksFragment
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class MainAdminFragment :
    BaseNavViewModelFragment<FragmentMainAdminBinding, IMainAdmin.State, MainAdminVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: MainAdminVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_main_admin
    override fun toolBarVisibility(): Boolean = false
    var selectedFragment = "AdminsFragment"
    override fun onClick(id: Int) {
        when (id) {
            R.id.adminsHeading -> {
                if (selectedFragment == "AllUsersFragment") {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.admin_fragment_container, AdminsFragment())
                        .commit()
                    selectedFragment = "AdminsFragment"
                    mViewDataBinding.adminsHeading.setTextColor(resources.getColor(R.color.appYellow))
                    mViewDataBinding.usersHeading.setTextColor(resources.getColor(R.color.grey))
                }
            }
            R.id.usersHeading -> {
                if (selectedFragment == "AdminsFragment") {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.admin_fragment_container, AllUsersFragment())
                        .commit()
                    selectedFragment = "AllUsersFragment"
                    mViewDataBinding.adminsHeading.setTextColor(resources.getColor(R.color.grey))
                    mViewDataBinding.usersHeading.setTextColor(resources.getColor(R.color.appYellow))
                }
            }
            R.id.backBtn -> navigateBack()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.beginTransaction()
            .replace(R.id.admin_fragment_container, AdminsFragment())
            .commit()
        selectedFragment = "AdminsFragment"
        mViewDataBinding.adminsHeading.setTextColor(resources.getColor(R.color.appYellow))


        mViewDataBinding.adminSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (selectedFragment == "AdminsFragment") {
                    EventBus.getDefault().post(LocalEvents.ApplySearchOnAdmins(query))
                }
                else {
                    EventBus.getDefault().post(LocalEvents.ApplySearchOnAllUsers(query))
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (selectedFragment == "AdminsFragment") {
                    EventBus.getDefault().post(LocalEvents.ApplySearchOnAdmins(newText))
                }
                else {
                    EventBus.getDefault().post(LocalEvents.ApplySearchOnAllUsers(newText))
                }
                return true
            }
        })

    }
}