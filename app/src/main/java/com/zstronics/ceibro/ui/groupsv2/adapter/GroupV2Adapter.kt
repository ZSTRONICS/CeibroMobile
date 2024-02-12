package com.zstronics.ceibro.ui.groupsv2.adapter


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncDBContactsList
import com.zstronics.ceibro.databinding.LayoutGroupBoxV2Binding
import com.zstronics.ceibro.ui.contacts.toLightDBGroupContacts
import javax.inject.Inject

class GroupV2Adapter @Inject constructor() :
    RecyclerView.Adapter<GroupV2Adapter.GroupV2ViewHolder>() {

    var groupListItems: MutableList<CeibroConnectionGroupV2> = mutableListOf()
    var selectedGroup: ArrayList<CeibroConnectionGroupV2> = arrayListOf()


    var deleteClickListener: ((CeibroConnectionGroupV2) -> Unit)? = null
    var renameClickListener: ((CeibroConnectionGroupV2, List<SyncDBContactsList.CeibroDBContactsLight>) -> Unit)? = null
    var itemClickListener: ((list: ArrayList<CeibroConnectionGroupV2>) -> Unit)? = null
    private var editFlag = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupV2ViewHolder {
        return GroupV2ViewHolder(
            LayoutGroupBoxV2Binding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GroupV2ViewHolder, position: Int) {
        holder.bind(groupListItems[position])
    }

    override fun getItemCount(): Int {
        return groupListItems.size
    }

    fun setList(list: List<CeibroConnectionGroupV2>, editFlag: Boolean) {
        this.groupListItems.clear()
        this.groupListItems.addAll(list)
        this.editFlag = editFlag

        notifyDataSetChanged()
    }

    fun selectAllGroups(list: List<CeibroConnectionGroupV2>) {
        this.selectedGroup.clear()
        this.selectedGroup.addAll(list)
        notifyDataSetChanged()
    }

    fun changeEditFlag(editFlag: Boolean) {
        this.editFlag = editFlag
        notifyDataSetChanged()
    }

    inner class GroupV2ViewHolder(private val binding: LayoutGroupBoxV2Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CeibroConnectionGroupV2) {
            val context = binding.root.context

            binding.apply {

                groupName.text = item.name
                groupMemberCount.text = if (item.contacts.size > 1) {
                    "(${item.contacts.size} members)"
                } else {
                    "(${item.contacts.size} member)"
                }

                groupCheckBox.isChecked = selectedGroup.contains(groupListItems[position])

                if (editFlag) {
                    groupCheckBox.visibility = View.VISIBLE
                    groupMenu.visibility = View.GONE
                } else {
                    groupCheckBox.visibility = View.GONE
                    groupMenu.visibility = View.VISIBLE
                }

                groupCheckBox.setOnClickListener {

                    if (groupCheckBox.isChecked) {
                        if (!selectedGroup.contains(groupListItems[position])) {
                            selectedGroup.add(groupListItems[position])
                        }
                    } else {
                        if (selectedGroup.contains(groupListItems[position])) {
                            selectedGroup.remove(groupListItems[position])
                        }
                    }
                    itemClickListener?.invoke(selectedGroup)
                    notifyItemChanged(position)
                }
                root.setOnClickListener {

                    if (editFlag) {


                        if (groupCheckBox.isChecked) {
                            groupCheckBox.isChecked = false

                            if (selectedGroup.contains(groupListItems[position])) {
                                selectedGroup.remove(groupListItems[position])
                            }
                        } else {
                            groupCheckBox.isChecked = true
                            if (!selectedGroup.contains(groupListItems[position])) {
                                selectedGroup.add(groupListItems[position])
                            }
                        }
                        itemClickListener?.invoke(selectedGroup)
                        notifyItemChanged(position)
                    }

                    groupMenu.setOnClickListener {
                        Handler(Looper.getMainLooper()).postDelayed({
                            createPopupWindow(it, item) { tag, data ->
                                if (tag == "delete") {
                                    deleteClickListener?.invoke(data)
                                } else if (tag == "rename") {
                                    renameClickListener?.invoke(data,item.contacts.toLightDBGroupContacts())

                                }
                            }
                        }, 200)
                    }
                }
            }
        }

    }
}

private fun createPopupWindow(
    v: View,
    groupResponseV2: CeibroConnectionGroupV2,
    callback: (String, CeibroConnectionGroupV2) -> Unit
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
        if (true) {
            popupWindow.dismiss()
            deleteGroupDialog(it.context, groupResponseV2) { tag, data ->
                callback.invoke(tag, data)
            }
        } else {
            cannotDeleteAlertBox(it.context)
        }

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
        popupWindow.showAsDropDown(v, -200, -170)
    } else {
        popupWindow.showAsDropDown(v, -205, -60)
    }


    return popupWindow
}

private fun deleteGroupDialog(
    context: Context,
    groupResponseV2: CeibroConnectionGroupV2,
    callback: (String, CeibroConnectionGroupV2) -> Unit
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
        callback.invoke("delete", groupResponseV2)
    }

    noBtn.setOnClickListener {
        alertDialog.dismiss()
    }
}


private fun cannotDeleteAlertBox(
    context: Context,
) {
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view: View = inflater.inflate(R.layout.layout_custom_dialog, null)

    val builder: androidx.appcompat.app.AlertDialog.Builder =
        androidx.appcompat.app.AlertDialog.Builder(context).setView(view)
    val alertDialog = builder.create()

    val yesBtn = view.findViewById<Button>(R.id.yesBtn)
    yesBtn.text = context.getString(R.string.ok)
    val noBtn = view.findViewById<Button>(R.id.noBtn)
    val saperater = view.findViewById<View>(R.id.viewSaperator)
    saperater.visibility = View.GONE
    noBtn.visibility = View.GONE

    val dialogText = view.findViewById<TextView>(R.id.dialog_text)
    dialogText.text =
        context.resources.getString(R.string.cannot_delete_group)
    alertDialog.window?.setBackgroundDrawable(null)
    alertDialog.show()

    yesBtn.setOnClickListener {
        alertDialog.dismiss()
    }

    noBtn.setOnClickListener {
        alertDialog.dismiss()
    }
}
