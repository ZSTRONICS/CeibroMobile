package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
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
import com.zstronics.ceibro.data.repos.projects.group.GroupResponseV2
import com.zstronics.ceibro.databinding.FloorCheckboxItemListBinding
import com.zstronics.ceibro.databinding.FragmentAddNewGroupSheetBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddNewGroupBottomSheet(val model: NewDrawingV2VM, val callback: (GroupResponseV2) -> Unit) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddNewGroupSheetBinding
    val items = ArrayList<String>()
    var onAddGroup: ((groupName: String) -> Unit)? = null
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
            val text = binding.tvNewGroup.text.toString()
            if (text.isNotEmpty()) {
                if (!items.contains(binding.tvNewGroup.text.toString().toLowerCase())) {
                    onAddGroup?.invoke(text)
                 //   items.add(text)
                    //addFloorToList(binding.llFloorsList, text)
                    binding.tvNewGroup.text = Editable.Factory.getInstance().newEditable("")

                }
            }
            binding.tvAddFloors.visibility = View.VISIBLE
            binding.clAddGroup.visibility = View.GONE
        }



        updateSelectionFloorsList(binding.llFloorsList, model)


    }

    private fun addFloorToList(llFloorsList: LinearLayout, item: String) {


        val itemViewBinding: FloorCheckboxItemListBinding = DataBindingUtil.inflate(
            LayoutInflater.from(binding.root.context),
            R.layout.floor_checkbox_item_list,
            binding.llFloorsList,
            false
        )
        itemViewBinding.tvFloorName.text = item
        itemViewBinding.tvFloorName.setOnClickListener {
           // callback.invoke(item)
            dismiss()
        }
        itemViewBinding.ivMenuBtn.setOnClickListener {

            createPopupWindow(it) {

            }
        }
        llFloorsList.addView(itemViewBinding.root)


    }

    private fun updateSelectionFloorsList(llFloorsList: LinearLayout, model: NewDrawingV2VM) {
        llFloorsList.removeAllViews()

        model.groupList.observe(viewLifecycleOwner) { items ->
            items.forEachIndexed { index, data ->

                val itemViewBinding: FloorCheckboxItemListBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(binding.root.context),
                    R.layout.floor_checkbox_item_list,
                    binding.llFloorsList,
                    false
                )
                itemViewBinding.tvFloorName.text = data.groupName
                itemViewBinding.tvFloorName.setOnClickListener {
                    callback.invoke(data)
                    dismiss()
                }
                itemViewBinding.ivMenuBtn.setOnClickListener {

                    createPopupWindow(it) {}
                }
                llFloorsList.addView(itemViewBinding.root)

            }
        }


    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = true
        }
        return dialog

    }


    private fun createPopupWindow(v: View, callback: (String) -> Unit): PopupWindow {
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.group_menu_dialog, null)

        val popupWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true


        val delete: TextView = view.findViewById(R.id.deleteGroupBtn)
        val renameGroupBtn: TextView = view.findViewById(R.id.renameGroupBtn)

        delete.setOnClickListener {
            callback.invoke("")
            updateGroup(it.context)
            popupWindow.dismiss()
        }
        renameGroupBtn.setOnClickListener {
            popupWindow.dismiss()
        }

        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        if (positionOfIcon > height) {
            popupWindow.showAsDropDown(v, -70, -200)
        } else {
            popupWindow.showAsDropDown(v, 0, 5)
        }

//        popupWindow.showAsDropDown(v, -110, -200)

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
