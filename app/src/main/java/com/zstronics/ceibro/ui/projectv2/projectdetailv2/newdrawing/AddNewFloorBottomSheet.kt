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
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.FloorCheckboxItemListBinding
import com.zstronics.ceibro.databinding.FloorCheckboxItemListingBinding
import com.zstronics.ceibro.databinding.FragmentAddFloorBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddNewFloorBottomSheet(val callback: (String) -> Unit) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddFloorBinding
    private var selectedFloorList = ArrayList<String>();


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_floor,
            container,
            false
        )
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvAddFloors.setOnClickListener {
            getFloorsList(it.context, selectedFloorList) { list ->
                selectedFloorList = list
                binding.floorText.text =
                    Editable.Factory.getInstance().newEditable(list.size.toString())

                callback.invoke(list.size.toString())
                updateSelectionFloorsList(binding.llFloorsList, list)
            }
        }

        val item = ArrayList<String>()
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
            itemViewBinding.ivMenuBtn.setOnClickListener {

                createPopupWindow(it, index.toString()) {

                    selectedFloorList.remove(itemViewBinding.tvFloorName.text.toString())
                    binding.floorText.text = Editable.Factory.getInstance()
                        .newEditable(selectedFloorList.size.toString())

                    callback.invoke(selectedFloorList.size.toString())

                    llFloorsList.removeView(itemViewBinding.root)
                    llFloorsList.invalidate()
                }
            }
            itemViewBinding.tvFloorName.text = data
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


    private fun createPopupWindow(v: View, index: String, callback: (String) -> Unit): PopupWindow {

        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.floor_menu_dialog, null)

        val popupWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true


        val deleteFloorBtn: TextView = view.findViewById(R.id.deleteFloorBtn)
        deleteFloorBtn.setOnClickListener {
            callback.invoke(index)
            popupWindow.dismiss()
        }


        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        if (positionOfIcon > height) {
            popupWindow.showAsDropDown(v, -70, -100)
        } else {
            popupWindow.showAsDropDown(v, 0, 5)
        }

//        popupWindow.showAsDropDown(v, -110, -200)

        return popupWindow
    }


    private fun getFloorsList(
        context: Context,
        selectedFloorList: ArrayList<String>,
        callback: (ArrayList<String>) -> Unit
    ) {

        val floorsList = ArrayList<String>()
        floorsList.clear()
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.floor_list_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val llFloorsList = dialog.findViewById<View>(R.id.llFloorsList) as LinearLayout

        llFloorsList.removeAllViews()
        val item = ArrayList<String>()
        item.add("B3 Floor")
        item.add("B2 Floor")
        item.add("B1 Floor")
        item.add("G Floor")
        item.add("1 Floor")
        item.add("2 Floor")
        item.add("3 Floor")
        item.add("4 Floor")
        item.add("5 Floor")
        item.add("6 Floor")
        item.add("7 Floor")
        item.add("8 Floor")
        item.add("9 Floor")
        item.add("10 Floor")
        item.add("11 Floor")
        item.add("12 Floor")
        item.add("13 Floor")
        item.add("14 Floor")
        item.add("15 Floor")
        item.add("16 Floor")
        item.add("17 Floor")
        item.add("18 Floor")
        item.add("19 Floor")
        item.add("20 Floor")


        item.forEachIndexed { index, data ->

            val itemViewBinding: FloorCheckboxItemListingBinding = DataBindingUtil.inflate(
                LayoutInflater.from(binding.root.context),
                R.layout.floor_checkbox_item_listing,
                binding.llFloorsList,
                false
            )
            itemViewBinding.cbFloorName.text = data

            if (selectedFloorList.isNotEmpty()) {
                if (selectedFloorList.contains(data)) {
                    itemViewBinding.cbFloorName.isChecked = true
                    floorsList.add(data)
                } else {
                    itemViewBinding.cbFloorName.isChecked = false
                }
            }
            itemViewBinding.cbFloorName.setOnClickListener {
                if (itemViewBinding.cbFloorName.isChecked) {
                    floorsList.add(data)
                } else {
                    floorsList.remove(data)
                }
                callback.invoke(floorsList)
            }
            llFloorsList.addView(itemViewBinding.root)

        }
        dialog.show()
    }

}
