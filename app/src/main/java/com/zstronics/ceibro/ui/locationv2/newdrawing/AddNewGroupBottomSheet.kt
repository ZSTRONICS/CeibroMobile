package com.zstronics.ceibro.ui.locationv2.newdrawing


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.FloorCheckboxItemListBinding
import com.zstronics.ceibro.databinding.FragmentAddNewGroupSheetBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddNewGroupBottomSheet(val callback: (String) -> Unit) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddNewGroupSheetBinding
    private var selectedFloorList = ArrayList<String>();


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_new_group_sheet,
            container,
            false
        )
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvAddFloors.setOnClickListener {
            binding.clAddGroup.visibility = View.VISIBLE
            it.visibility = View.GONE
        }
        binding.addGroupBtn.setOnClickListener {
            binding.tvAddFloors.visibility = View.VISIBLE
            binding.clAddGroup.visibility = View.GONE
        }

        val item = ArrayList<String>()
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        item.add("Pipes")
        item.add("electricity")
        updateSelectionFloorsList(binding.llFloorsList, item)


    }

    private fun updateSelectionFloorsList(llFloorsList: LinearLayout, item: ArrayList<String>) {
        llFloorsList.removeAllViews()

        item.forEachIndexed { index, data ->

            val itemViewBinding: FloorCheckboxItemListBinding = DataBindingUtil.inflate(
                LayoutInflater.from(binding.root.context),
                R.layout.floor_checkbox_item_list,
                binding.llFloorsList,
                false
            )
            itemViewBinding.tvFloorName.text = data
            itemViewBinding.ivFloor.setOnClickListener {

                createPopupWindow(it)
            }
            llFloorsList.addView(itemViewBinding.root)

        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = true
        }
        return dialog

    }


    private fun createPopupWindow(v: View): PopupWindow {
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.group_update_dialog, null)
        val delete: TextView = view.findViewById(R.id.updateGroupBtn)
        delete.setOnClickListener {
            dismiss()
            updateGroup(it.context)

        }
        val renameGroupBtn: TextView = view.findViewById(R.id.renameGroupBtn)
        renameGroupBtn.setOnClickListener {
            dismiss()
        }

        val popupWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true

        val values = IntArray(2)
        v.getLocationInWindow(values)

        popupWindow.showAsDropDown(v, -110, -200)

        return popupWindow
    }


    private fun updateGroup(
        context: Context
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_custom_dialog, null)

        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(context).setView(view)
        val alertDialog = builder.create()

        val yesBtn = view.findViewById<Button>(R.id.yesBtn)
        val noBtn = view.findViewById<Button>(R.id.noBtn)
        val dialogText = view.findViewById<TextView>(R.id.dialog_text)
        dialogText.text =
            context.resources.getString(R.string.are_you_sure_you_want_to_delete_this_group)
        alertDialog.window?.setBackgroundDrawable(null)
        alertDialog.show()

        yesBtn.setOnClickListener {
            alertDialog.dismiss()

        }

        noBtn.setOnClickListener {
            alertDialog.dismiss()
        }
    }
}
