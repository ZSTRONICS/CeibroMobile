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
import androidx.lifecycle.LiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.FragmentStatusViewSheetBinding

class ProjectStatusViewSheet constructor(private val projectStatuses: LiveData<ArrayList<ProjectOverviewVM.ProjectStatus>>) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentStatusViewSheetBinding
    var onEdit: ((position: Int, updated: ProjectOverviewVM.ProjectStatus) -> Unit)? = null
    var onDelete: ((position: Int) -> Unit)? = null
    var onAddNew: (() -> Unit)? = null
    var onSelect: ((status: String) -> Unit)? = null

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
        val view = binding.root
        val windowHeight = resources.displayMetrics.heightPixels
        val halfWindowHeight = windowHeight / 2
        view.layoutParams?.height = halfWindowHeight

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ProjectStatusAdapter()
        adapter.simpleChildItemClickListener =
            { childView: View, position: Int, data: ProjectOverviewVM.ProjectStatus ->
                val popUpWindowObj = popUpMenu(position, childView, data)
                popUpWindowObj.showAsDropDown(
                    childView.findViewById(R.id.optionMenu),
                    0,
                    10
                )
            }

        adapter.itemClickListener =
            { childView: View, position: Int, data: ProjectOverviewVM.ProjectStatus ->
                onSelect?.invoke(data.status)
                dismiss()
            }
        binding.statusRV.adapter = adapter
        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        binding.addNewStatusTV.setOnClickListener {
            onAddNew?.invoke()
        }
        projectStatuses.observe(viewLifecycleOwner) {
            adapter.setList(it)
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