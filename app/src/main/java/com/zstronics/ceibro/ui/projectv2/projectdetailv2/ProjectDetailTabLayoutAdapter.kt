package com.zstronics.ceibro.ui.projectv2.projectdetailv2


import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawing.DrawingsV2Fragment
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.projectinfo.ProjectInfoV2Fragment


private const val NUM_TABS = 2


class ProjectDetailTabLayoutAdapter(
    fragmentManager: FragmentActivity,
    val drawingFileClickListener: ((view: View, data: DrawingV2, tag: String) -> Unit)?=null
) : FragmentStateAdapter(fragmentManager) {
    override fun getItemCount(): Int = NUM_TABS

    override fun createFragment(position: Int): Fragment {

        val projectInfoV2Fragment = ProjectInfoV2Fragment()

        val drawingsV2Fragment = DrawingsV2Fragment()

        drawingsV2Fragment.drawingFileClickListener = { view, data, tag ->
            drawingFileClickListener?.invoke(view, data, tag)
        }


        return when (position) {
            0 -> projectInfoV2Fragment

            1 -> drawingsV2Fragment

            else -> projectInfoV2Fragment
        }
    }
}


