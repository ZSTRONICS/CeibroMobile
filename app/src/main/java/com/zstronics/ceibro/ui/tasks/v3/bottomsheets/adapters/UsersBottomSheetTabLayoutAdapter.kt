package com.zstronics.ceibro.ui.tasks.v3.bottomsheets.adapters


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.ui.tasks.v3.TasksParentTabV3VM
import com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing.groups.SelectGroupFiltersV2Fragment
import com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing.users.UsersFiltersFragment


private const val NUM_TABS = 2

class UsersBottomSheetTabLayoutAdapter(
    fragmentManager: FragmentActivity,
    val viewModel: TasksParentTabV3VM,
    private val userConnectionAndRoleCallBack: (Pair<ArrayList<AllCeibroConnections.CeibroConnection>, ArrayList<String>>)->Unit,
    private val userGroupCallBack: (ArrayList<CeibroConnectionGroupV2>) -> Unit
) :
    FragmentStateAdapter(fragmentManager) {
    override fun getItemCount(): Int = NUM_TABS

    override fun createFragment(position: Int): Fragment {
        val usersFiltersFragment = UsersFiltersFragment(viewModel.userConnectionAndRoleList)
        usersFiltersFragment.setConnectionCallBack {
            userConnectionAndRoleCallBack.invoke(it)
        }


        val list = ArrayList<CeibroConnectionGroupV2>()
        viewModel.selectedGroups.forEach {
        list.add(it.copy())

        }
        val selectGroupV2Fragment = SelectGroupFiltersV2Fragment(list)

        selectGroupV2Fragment.setGroupsCallBack {
            userGroupCallBack.invoke(it)
        }

        return when (position) {
            0 -> usersFiltersFragment
            1 -> selectGroupV2Fragment
            else -> usersFiltersFragment
        }
    }
}