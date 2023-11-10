package com.zstronics.ceibro.ui.projectv2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.zstronics.ceibro.ui.projectv2.allprojectsv2.AllProjectsV2Fragment
import com.zstronics.ceibro.ui.projectv2.hiddenprojectv2.HiddenProjectsV2Fragment

class ProjectTabLayoutAdapter(fm: FragmentManager, val callback: (Int) -> Unit) :
    FragmentPagerAdapter(fm) {


    override fun getItem(position: Int): Fragment {

        val allProjectsV2Fragment = AllProjectsV2Fragment {
            callback.invoke(1)
        }

        val hiddenProjectsV2Fragment = HiddenProjectsV2Fragment {
            callback.invoke(1)
        }

        return when (position) {


            0 -> allProjectsV2Fragment
            1 -> hiddenProjectsV2Fragment
            else -> allProjectsV2Fragment
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
