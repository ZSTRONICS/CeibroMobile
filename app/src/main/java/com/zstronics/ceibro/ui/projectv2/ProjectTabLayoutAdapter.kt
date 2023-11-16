package com.zstronics.ceibro.ui.projectv2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zstronics.ceibro.ui.projectv2.allprojectsv2.AllProjectsV2Fragment
import com.zstronics.ceibro.ui.projectv2.hiddenprojectv2.HiddenProjectsV2Fragment


private const val NUM_TABS = 2


class ProjectTabLayoutAdapter(fragmentManager: FragmentActivity) :
    FragmentStateAdapter(fragmentManager) {
    override fun getItemCount(): Int = NUM_TABS

    override fun createFragment(position: Int): Fragment {
        val allProjectsV2Fragment = AllProjectsV2Fragment()

        val hiddenProjectsV2Fragment = HiddenProjectsV2Fragment {
           // allProjectsV2Fragment.reloadData()
        }
        return when (position) {
            0 -> allProjectsV2Fragment
            1 -> hiddenProjectsV2Fragment
            else -> allProjectsV2Fragment
        }
    }
}