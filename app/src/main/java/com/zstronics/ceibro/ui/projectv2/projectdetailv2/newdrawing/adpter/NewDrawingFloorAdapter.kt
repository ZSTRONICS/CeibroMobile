package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing.adpter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2
import com.zstronics.ceibro.databinding.FloorCheckboxItemListBinding
import javax.inject.Inject

class NewDrawingFloorAdapter :
    RecyclerView.Adapter<NewDrawingFloorAdapter.NewFloorGroupViewHolder>() {

    var deleteClickListener: ((data: CeibroFloorV2) -> Unit)? = null
    var itemClickListener: ((data: CeibroFloorV2, list: List<CeibroFloorV2>) -> Unit)? = null
    var listItems: MutableList<CeibroFloorV2> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewFloorGroupViewHolder {
        return NewFloorGroupViewHolder(
            FloorCheckboxItemListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NewFloorGroupViewHolder, position: Int) {
        holder.bind(listItems[position], position)
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<CeibroFloorV2>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    fun deleteItem(floorName: String) {
        val allItems = this.listItems
        val foundItem = allItems.find { it.floorName == floorName }
        if (foundItem != null) {
            val index = allItems.indexOf(foundItem)
            allItems.removeAt(index)
            this.listItems = allItems
            notifyDataSetChanged()
        }
    }

    inner class NewFloorGroupViewHolder(private val binding: FloorCheckboxItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CeibroFloorV2, index: Int) {

            binding.tvFloorName.text = data.floorName
            binding.tvFloorName.setOnClickListener {
                itemClickListener?.invoke(data, listItems)
            }

            binding.ivMenuBtn.setOnClickListener {

                createPopupWindow(it, index) {

                    deleteClickListener?.invoke(listItems[index])
                }
            }
        }
    }


    private fun createPopupWindow(v: View, index: Int, callback: (Int) -> Unit): PopupWindow {

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

        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        if (positionOfIcon > height) {
            popupWindow.showAsDropDown(v, -215, -80)
        } else {
            popupWindow.showAsDropDown(v, -215, -80)
        }
        return popupWindow
    }
}