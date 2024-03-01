package com.zstronics.ceibro.ui.tasks.v3

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailcomments.TaskDetailCommentsV2Fragment
import com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailfiles.TaskDetailFilesV2Fragment
import com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing.TaskV3OngoingFragment

private const val NUM_TABS = 4

class TasksParentV3TabLayoutAdapter(fragmentManager: FragmentActivity, private val tabIcons: Array<Int>) :
    FragmentStateAdapter(fragmentManager) {
    override fun getItemCount(): Int = NUM_TABS

    override fun createFragment(position: Int): Fragment {
        val ongoingFragment = TaskV3OngoingFragment()
        val taskDetailCommentsV2Fragment = TaskDetailCommentsV2Fragment()
        val taskDetailFilesV2Fragment = TaskDetailFilesV2Fragment()

        return when (position) {
            0 -> ongoingFragment
            1 -> ongoingFragment
            2 -> ongoingFragment
            3 -> ongoingFragment
            else -> ongoingFragment
        }
    }

    fun getTabIcon(position: Int): Int {
        return tabIcons[position]
    }
}