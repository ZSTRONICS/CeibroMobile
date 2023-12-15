package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing.adpter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.projects.group.GroupResponseV2
import com.zstronics.ceibro.databinding.FloorCheckboxItemListBinding
import javax.inject.Inject

class NewDrawingGroupAdapter @Inject constructor() :
    RecyclerView.Adapter<NewDrawingGroupAdapter.NewDrawingGroupViewHolder>() {


    var deleteClickListener: ((GroupResponseV2) -> Unit)? = null
    var renameClickListener: ((GroupResponseV2) -> Unit)? = null

    var itemClickListener: ((GroupResponseV2) -> Unit)? = null


    var listItems: MutableList<GroupResponseV2> = mutableListOf()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewDrawingGroupViewHolder {
        return NewDrawingGroupViewHolder(
            FloorCheckboxItemListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NewDrawingGroupViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<GroupResponseV2>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    fun addiItem(item: GroupResponseV2) {
        this.listItems.add(item)
        notifyDataSetChanged()
    }

    inner class NewDrawingGroupViewHolder(private val binding: FloorCheckboxItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: GroupResponseV2) {


            binding.tvFloorName.text = data.groupName
            binding.tvFloorName.setOnClickListener {
                itemClickListener?.invoke(data)
            }
            binding.ivMenuBtn.setOnClickListener {

                createPopupWindow(it, data) { tag, data ->
                    if (tag == "delete") {

                        data.Id?.let {
                            deleteClickListener?.invoke(data)
                        }

                    }
                    else if (tag == "rename"){
                        data.Id?.let {
                            renameClickListener?.invoke(data)
                        }
                    }
                }
            }


        }
    }

    private fun createPopupWindow(
        v: View,
        groupResponseV2: GroupResponseV2,
        callback: (String, GroupResponseV2) -> Unit
    ): PopupWindow {
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
            // callback.invoke("delete", groupResponseV2)
            updateGroup(it.context, groupResponseV2) { tag, data ->
                callback.invoke(tag, data)
            }
            popupWindow.dismiss()
        }
        renameGroupBtn.setOnClickListener {
            callback.invoke("rename", groupResponseV2)
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
        context: Context,
        groupResponseV2: GroupResponseV2,
        callback: (String, GroupResponseV2) -> Unit
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
            callback.invoke("delete", groupResponseV2)
            alertDialog.dismiss()

        }

        noBtn.setOnClickListener {
            alertDialog.dismiss()
        }
    }
}