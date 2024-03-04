package com.zstronics.ceibro.ui.tasks.v3.bottomsheets.adapters


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing.groups.SelectGroupV2Fragment
import com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing.users.UsersFragment


private const val NUM_TABS = 2

class UsersBottomSheetTabLayoutAdapter(fragmentManager: FragmentActivity) :
    FragmentStateAdapter(fragmentManager) {
    override fun getItemCount(): Int = NUM_TABS

    override fun createFragment(position: Int): Fragment {
        val taskDetailParentV2Fragment = UsersFragment()
        val SelectGroupV2Fragment = SelectGroupV2Fragment()
        val taskDetailFilesV2Fragment = UsersFragment()

        return when (position) {
            0 -> taskDetailParentV2Fragment
            1 -> SelectGroupV2Fragment
            else -> taskDetailParentV2Fragment
        }
    }
}