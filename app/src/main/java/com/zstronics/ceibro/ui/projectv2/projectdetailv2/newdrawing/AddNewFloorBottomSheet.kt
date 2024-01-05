package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing


import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2
import com.zstronics.ceibro.databinding.FloorCheckboxItemListingBinding
import com.zstronics.ceibro.databinding.FragmentAddFloorBinding
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing.adpter.NewDrawingFloorAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AddNewFloorBottomSheet(
    val model: NewDrawingV2VM,
    val callback: (data: CeibroFloorV2) -> Unit
) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddFloorBinding
    private var selectedFloorList: MutableList<CeibroFloorV2> = mutableListOf()
    var deleteClickListener: ((String) -> Unit)? = null
    var selectItemClickListener: ((data: CeibroFloorV2, list: List<CeibroFloorV2>) -> Unit)? = null
    private val enumOrder = listOf(
        "B3",
        "B2",
        "B1",
        "G",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "17",
        "18",
        "19",
        "20",
        "21",
        "22",
        "23",
        "24",
        "25"
    )

    @Inject
    lateinit var floorAdapter: NewDrawingFloorAdapter
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


        model.floorList.observe(viewLifecycleOwner) {
//            println("floorList.observer called")
            val sortedList =
                it.sortedWith(compareBy { enumOrder.indexOf(it.floorName) }).toMutableList()


            floorAdapter.setList(sortedList)
            selectedFloorList = sortedList
        }


        binding.rvFloorList.adapter = floorAdapter


        floorAdapter.itemClickListener = { data, list ->

            selectItemClickListener?.invoke(data, list)
        }

        floorAdapter.deleteClickListener = { data ->
            if (data._id.isEmpty()) {
                floorAdapter.deleteItem(data.floorName)
            } else {
                deleteClickListener?.invoke(data._id)
            }
        }

        binding.tvAddFloors.setOnClickListener {
            getFloorsList(it.context, floorAdapter.listItems) { list ->
                floorAdapter.setList(list)
                selectedFloorList = list
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

    private fun getFloorsList(
        context: Context,
        selectedFloorList: MutableList<CeibroFloorV2>,
        callback: (MutableList<CeibroFloorV2>) -> Unit
    ) {

        val enumOrder = listOf(
            "B3", "B2", "B1", "G", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25"
        )

        val sortedList = selectedFloorList.sortedWith(compareBy { enumOrder.indexOf(it.floorName) })
            .toMutableList()

        var floorsList: MutableList<CeibroFloorV2> = mutableListOf()
        floorsList.clear()
        floorsList.addAll(sortedList)

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.floor_list_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val llFloorsList = dialog.findViewById<View>(R.id.llFloorsList) as LinearLayout
        llFloorsList.removeAllViews()
        val item = ArrayList<String>()

        item.add("B3")
        item.add("B2")
        item.add("B1")
        item.add("G")
        item.add("1")
        item.add("2")
        item.add("3")
        item.add("4")
        item.add("5")
        item.add("6")
        item.add("7")
        item.add("8")
        item.add("9")
        item.add("10")
        item.add("11")
        item.add("12")
        item.add("13")
        item.add("14")
        item.add("15")
        item.add("16")
        item.add("17")
        item.add("18")
        item.add("19")
        item.add("20")
        item.add("21")
        item.add("22")
        item.add("23")
        item.add("24")
        item.add("25")


        item.forEachIndexed { index, data ->

            val itemViewBinding: FloorCheckboxItemListingBinding = DataBindingUtil.inflate(
                LayoutInflater.from(binding.root.context),
                R.layout.floor_checkbox_item_listing,
                null,
                false
            )
            itemViewBinding.cbFloorName.text = data

            if (floorsList.isNotEmpty()) {

                val existingFloor = floorsList.find { it.floorName.equals(data, true) }

                if (existingFloor != null) {
                    itemViewBinding.cbFloorName.isChecked = true
                    if (existingFloor._id.isNotEmpty()) {
                        itemViewBinding.cbFloorName.isClickable = false
                        itemViewBinding.cbFloorName.isEnabled = false
                    } else {
                        itemViewBinding.cbFloorName.isClickable = true
                        itemViewBinding.cbFloorName.isEnabled = true
                    }
                } else {
                    itemViewBinding.cbFloorName.isChecked = false
                    itemViewBinding.cbFloorName.isClickable = true
                    itemViewBinding.cbFloorName.isEnabled = true
                }
            }

            itemViewBinding.tvAddFloors.setOnClickListener {
                itemViewBinding.cbFloorName.isChecked = !(itemViewBinding.cbFloorName.isChecked)
                val floorName1 = itemViewBinding.cbFloorName.text.toString()
                val existingFloor = floorsList.find { it.floorName.equals(floorName1, true) }

                if (itemViewBinding.cbFloorName.isChecked) {
                    if (existingFloor != null) {
                        // Check if the floor is not already in the list before adding
                        if (!floorsList.contains(existingFloor)) {
                            floorsList.add(existingFloor)
                        }
                    } else {
                        val floor = CeibroFloorV2(
                            _id = "",
                            createdAt = "",
                            updatedAt = "",
                            creator = "",
                            drawings = listOf(),
                            projectId = "",
                            floorName = floorName1
                        )
                        floorsList.add(floor)
                    }
                } else {
                    existingFloor?.let {
                        floorsList.remove(it)
                    }
                }
                floorsList = floorsList.distinctBy { it.floorName }.toMutableList()
                floorsList = floorsList.sortedWith(compareBy { enumOrder.indexOf(it.floorName) })
                    .toMutableList()
                callback.invoke(floorsList)
            }
            itemViewBinding.cbFloorName.setOnClickListener {
                val floorName1 = itemViewBinding.cbFloorName.text.toString()
                val existingFloor = floorsList.find { it.floorName.equals(floorName1, true) }

                if (itemViewBinding.cbFloorName.isChecked) {
                    if (existingFloor != null) {
                        if (!floorsList.contains(existingFloor)) {
                            floorsList.add(existingFloor)
                        }
                    } else {
                        val floor = CeibroFloorV2(
                            _id = "",
                            createdAt = "",
                            updatedAt = "",
                            creator = "",
                            drawings = listOf(),
                            projectId = "",
                            floorName = floorName1
                        )
                        floorsList.add(floor)
                    }
                } else {
                    existingFloor?.let {
                        floorsList.remove(it)
                    }
                }
                floorsList = floorsList.distinctBy { it.floorName }.toMutableList()
                floorsList = floorsList.sortedWith(compareBy { enumOrder.indexOf(it.floorName) })
                    .toMutableList()
                callback.invoke(floorsList)
            }
            llFloorsList.addView(itemViewBinding.root)
        }
        dialog.show()
    }
}
