package com.zstronics.ceibro.ui.tasks.v3

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.zstronics.ceibro.ui.inbox.InboxFragment
import com.zstronics.ceibro.ui.tasks.v3.fragments.approval.TaskV3ApprovalFragment
import com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing.TaskV3OngoingFragment

private const val NUM_TABS = 4

class TasksParentV3TabLayoutAdapter(
    fragmentManager: FragmentActivity,
    private val tabIcons: Array<Int>,
    private val parentViewModel: TasksParentTabV3VM
) :
    FragmentStateAdapter(fragmentManager) {
    override fun getItemCount(): Int = NUM_TABS

    override fun createFragment(position: Int): Fragment {
        val inboxFragment = InboxFragment()
        val ongoingFragment = TaskV3OngoingFragment.newInstance(parentViewModel)
        val approvalFragment = TaskV3ApprovalFragment.newInstance(parentViewModel)

        return when (position) {
            0 -> inboxFragment
            1 -> ongoingFragment
            2 -> approvalFragment
            3 -> approvalFragment
            else -> ongoingFragment
        }
    }

    fun getTabIcon(position: Int): Int {
        return tabIcons[position]
    }

    fun setDefaultTab(viewPager: ViewPager2) {
        // Set the default tab to index 1 (ongoingFragment2)
        viewPager.setCurrentItem(1, false)
    }
}