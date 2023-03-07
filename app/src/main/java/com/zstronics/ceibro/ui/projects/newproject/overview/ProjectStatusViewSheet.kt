package com.zstronics.ceibro.ui.projects.newproject.overview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.databinding.FragmentStatusViewSheetBinding
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus

class ProjectStatusViewSheet constructor(private val projectStatuses: List<ProjectOverviewVM.ProjectStatus>) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentStatusViewSheetBinding
    var onEdit: ((position: Int, updated: ProjectOverviewVM.ProjectStatus) -> Unit)? = null
    var onDelete: ((position: Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_status_view_sheet,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ProjectStatusAdapter()
        adapter.setList(projectStatuses)
        adapter.simpleChildItemClickListener =
            { childView: View, position: Int, data: ProjectOverviewVM.ProjectStatus ->
                val popUpWindowObj = popUpMenu(position, childView, data)
                popUpWindowObj.showAsDropDown(
                    childView.findViewById(R.id.optionMenu),
                    0,
                    10
                )
            }
        binding.statusRV.adapter = adapter
        binding.closeBtn.setOnClickListener {
            dismiss()
        }
    }

    private fun popUpMenu(
        position: Int,
        v: View,
        data: ProjectOverviewVM.ProjectStatus
    ): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_edit_remove_menu, null)

        val edit = view.findViewById<View>(R.id.edit)
        val remove = view.findViewById<LinearLayoutCompat>(R.id.remove)

        edit.setOnClickListener {
            popupWindow.dismiss()
        }
        remove.setOnClickListener {
            onDelete?.invoke(position)
            popupWindow.dismiss()
        }

        popupWindow.isFocusable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 13f
        return popupWindow
    }
}