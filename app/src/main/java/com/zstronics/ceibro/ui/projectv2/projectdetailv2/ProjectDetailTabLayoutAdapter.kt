package com.zstronics.ceibro.ui.projectv2.projectdetailv2


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.projectinfo.ProjectInfoV2Fragment


private const val NUM_TABS = 1


class ProjectDetailTabLayoutAdapter(
    fragmentManager: FragmentActivity
) : FragmentStateAdapter(fragmentManager) {
    override fun getItemCount(): Int = NUM_TABS

    override fun createFragment(position: Int): Fragment {

        val projectInfoV2Fragment = ProjectInfoV2Fragment()

        return when (position) {
            0 -> projectInfoV2Fragment

            else -> projectInfoV2Fragment
        }
    }
}


