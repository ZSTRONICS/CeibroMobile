package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailcomments.TaskDetailCommentsV2Fragment
import com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailfiles.TaskDetailFilesV2Fragment
import com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailparent.TaskDetailParentV2Fragment


private const val NUM_TABS = 3

class TaskDetailV2TabLayout(fragmentManager: FragmentActivity) :
    FragmentStateAdapter(fragmentManager) {
    override fun getItemCount(): Int = NUM_TABS

    val taskDetailParentV2Fragment = TaskDetailParentV2Fragment()
    val taskDetailCommentsV2Fragment = TaskDetailCommentsV2Fragment()
    val taskDetailFilesV2Fragment = TaskDetailFilesV2Fragment()

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> taskDetailParentV2Fragment
            1 -> taskDetailCommentsV2Fragment
            2 -> taskDetailFilesV2Fragment
            else -> taskDetailParentV2Fragment
        }
    }


    fun onParentDestroyed() {
        taskDetailParentV2Fragment.onDestroy()
        taskDetailCommentsV2Fragment.onDestroy()
        taskDetailFilesV2Fragment.onDestroy()
    }
}