package com.zstronics.ceibro.ui.projectv2.projectdetailv2


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.projectinfo.ProjectInfoV2Fragment


private const val NUM_TABS = 1

class ProjectDetailTabLayoutAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        val allProjectsV2Fragment = ProjectInfoV2Fragment()
        return when (position) {
            0 -> allProjectsV2Fragment
            else -> allProjectsV2Fragment
        }
    }
}



