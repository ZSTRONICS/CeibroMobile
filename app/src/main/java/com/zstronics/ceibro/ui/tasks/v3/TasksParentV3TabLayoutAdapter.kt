package com.zstronics.ceibro.ui.tasks.v3

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailcomments.TaskDetailCommentsV2Fragment
import com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailfiles.TaskDetailFilesV2Fragment
import com.zstronics.ceibro.ui.works.WorksFragment

private const val NUM_TABS = 4

class TasksParentV3TabLayoutAdapter(fragmentManager: FragmentActivity, private val tabIcons: Array<Int>) :
    FragmentStateAdapter(fragmentManager) {
    override fun getItemCount(): Int = NUM_TABS

    override fun createFragment(position: Int): Fragment {
        val taskDetailParentV2Fragment = WorksFragment()
        val taskDetailCommentsV2Fragment = TaskDetailCommentsV2Fragment()
        val taskDetailFilesV2Fragment = TaskDetailFilesV2Fragment()

        return when (position) {
            0 -> taskDetailParentV2Fragment
            1 -> taskDetailParentV2Fragment
            2 -> taskDetailParentV2Fragment
            3 -> taskDetailParentV2Fragment
            else -> taskDetailParentV2Fragment
        }
    }

    fun getTabIcon(position: Int): Int {
        return tabIcons[position]
    }
}