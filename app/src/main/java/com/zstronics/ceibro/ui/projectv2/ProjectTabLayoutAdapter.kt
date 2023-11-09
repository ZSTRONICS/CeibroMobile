package com.zstronics.ceibro.ui.projectv2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.zstronics.ceibro.ui.projectv2.allprojectsv2.AllProjectsV2Fragment

class ProjectTabLayoutAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // Return the fragment based on the position
        return when (position) {
            0 -> AllProjectsV2Fragment()
            1 -> AllProjectsV2Fragment()
            else -> AllProjectsV2Fragment()
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "All Projects"
            1 -> "Hidden Projects"
            else -> null
        }
    }
}
