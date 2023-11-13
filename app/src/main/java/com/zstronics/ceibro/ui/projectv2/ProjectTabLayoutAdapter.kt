package com.zstronics.ceibro.ui.projectv2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zstronics.ceibro.ui.projectv2.allprojectsv2.AllProjectsV2Fragment
import com.zstronics.ceibro.ui.projectv2.hiddenprojectv2.HiddenProjectsV2Fragment


private const val NUM_TABS = 2

class ProjectTabLayoutAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    val callback: (Int) -> Unit
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
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
}
//class ProjectTabLayoutAdapter(fm: FragmentManager, val callback: (Int) -> Unit) :
//    FragmentPagerAdapter(fm) {
//
//
//    override fun getItem(position: Int): Fragment {
//
//        val allProjectsV2Fragment = AllProjectsV2Fragment {
//            callback.invoke(1)
//        }
//
//        val hiddenProjectsV2Fragment = HiddenProjectsV2Fragment {
//            callback.invoke(1)
//        }
//
//        return when (position) {
//
//
//            0 -> allProjectsV2Fragment
//            1 -> hiddenProjectsV2Fragment
//            else -> allProjectsV2Fragment
//        }
//    }
//
//    override fun getCount(): Int {
//        return 2
//    }
//
//    override fun getPageTitle(position: Int): CharSequence? {
//        return when (position) {
//            0 -> "All Projects"
//            1 -> "Hidden Projects"
//            else -> null
//        }
//    }
//}
