package com.zstronics.ceibro.ui.tasks.v3.hidden

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.zstronics.ceibro.ui.tasks.v3.hidden.fragment.closed.TaskV3HiddenClosedFragment
import com.zstronics.ceibro.ui.tasks.v3.hidden.fragment.ongoing.TaskV3HiddenOngoingFragment

class TasksHiddenParentV3TabLayoutAdapter(
    fragmentManager: FragmentActivity,
    private val tabIcons: Array<Int>,
    private val parentViewModel: TasksHiddenParentTabV3VM
) :
    FragmentStateAdapter(fragmentManager) {
    private val NUM_TABS = 2
    override fun getItemCount(): Int = NUM_TABS

    override fun createFragment(position: Int): Fragment {

        val ongoingFragment = TaskV3HiddenOngoingFragment.newInstance(parentViewModel)
        val closedFragment = TaskV3HiddenClosedFragment.newInstance(parentViewModel)

        return when (position) {
            0 -> ongoingFragment
            1 -> closedFragment
            else -> ongoingFragment
        }
    }

    fun getTabIcon(position: Int): Int {
        return tabIcons[position]
    }

    fun setDefaultTab(viewPager: ViewPager2) {
        // Set the default tab to index 1 (ongoingFragment2)
        viewPager.setCurrentItem(0, false)
    }
}