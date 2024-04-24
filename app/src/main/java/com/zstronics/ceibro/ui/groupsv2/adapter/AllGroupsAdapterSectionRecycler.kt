package com.zstronics.ceibro.ui.groupsv2.adapter

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.databinding.LayoutGroupBoxV2Binding
import com.zstronics.ceibro.databinding.LayoutItemHeaderBinding
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver

class AllGroupsAdapterSectionRecycler(
    val context: Context,
    sectionList: MutableList<GroupSectionHeader>,
    val networkConnectivityObserver: NetworkConnectivityObserver
) : SectionRecyclerViewAdapter<
        GroupSectionHeader,
        CeibroConnectionGroupV2,
        AllGroupsAdapterSectionRecycler.ConnectionsSectionViewHolder,
        AllGroupsAdapterSectionRecycler.ConnectionsChildViewHolder>(
    context,
    sectionList
) {

    var groupListItems: MutableList<CeibroConnectionGroupV2> = mutableListOf()
    var selectedGroup: ArrayList<CeibroConnectionGroupV2> = arrayListOf()

    var deleteClickListener: ((CeibroConnectionGroupV2) -> Unit)? = null
    var renameClickListener: ((CeibroConnectionGroupV2) -> Unit)? = null
    var openInfoClickListener: ((CeibroConnectionGroupV2) -> Unit)? = null
    var itemClickListener: ((list: ArrayList<CeibroConnectionGroupV2>) -> Unit)? = null
    private var editFlag = false
    private var selectAllGroups = false


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionList13 = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    private val permissionList10 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    var popupMenu: PopupMenu? = null
    private var isPopupMenuShowing = false
    var drawingFileClickListener: ((view: View, data: DrawingV2, absolutePath: String) -> Unit)? =
        null

    fun drawingFileClickListenerCallBack(itemClickListener: ((view: View, data: DrawingV2, absolutePath: String) -> Unit)?) {
        this.drawingFileClickListener = itemClickListener
    }

    var downloadFileClickListener: ((view: TextView, ivDownloadFile: AppCompatImageView, ivDownloaded: AppCompatImageView, data: DrawingV2, tag: String) -> Unit)? =
        null

    fun downloadFileCallBack(itemClickListener: ((view: TextView, ivDownload: AppCompatImageView, iv: AppCompatImageView, data: DrawingV2, tag: String) -> Unit)?) {
        this.downloadFileClickListener = itemClickListener
    }

    var requestPermissionClickListener: ((tag: String) -> Unit)? = null

    fun requestPermissionCallBack(requestPermissionClickListener: (tag: String) -> Unit) {
        this.requestPermissionClickListener = requestPermissionClickListener
    }

    var publicGroupClickListener: ((tag: String, CeibroGroupsV2?) -> Unit)? = null

    fun publicGroupCallBack(publicGroupClickListener: (tag: String, CeibroGroupsV2?) -> Unit) {
        this.publicGroupClickListener = publicGroupClickListener
    }

    override fun onCreateSectionViewHolder(
        sectionViewGroup: ViewGroup?,
        viewType: Int
    ): ConnectionsSectionViewHolder {
        return ConnectionsSectionViewHolder(
            LayoutItemHeaderBinding.inflate(
                LayoutInflater.from(context),
                sectionViewGroup,
                false
            )
        )
    }

    override fun onBindSectionViewHolder(
        connectionsSectionViewHolder: ConnectionsSectionViewHolder?,
        sectionPosition: Int,
        connectionsSectionHeader: GroupSectionHeader?
    ) {
        connectionsSectionViewHolder?.bind(connectionsSectionHeader)
    }

    override fun onCreateChildViewHolder(
        childViewGroup: ViewGroup?,
        viewType: Int
    ): ConnectionsChildViewHolder {
        return ConnectionsChildViewHolder(
            LayoutGroupBoxV2Binding.inflate(
                LayoutInflater.from(context),
                childViewGroup,
                false
            )
        )
    }

    override fun onBindChildViewHolder(
        holder: ConnectionsChildViewHolder?,
        sectionPosition: Int,
        childPostitoin: Int,
        p3: CeibroConnectionGroupV2?
    ) {
        holder?.bind(p3)
    }


    inner class ConnectionsSectionViewHolder constructor(private val binding: LayoutItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroupSectionHeader?) {
            binding.headerTitle.text = item?.getSectionText()

            if (item?.childItems.isNullOrEmpty()) {
                binding.headerTitle.visibility = View.GONE
            } else {
                binding.headerTitle.visibility = View.VISIBLE
            }
        }
    }

    inner class ConnectionsChildViewHolder constructor(val binding: LayoutGroupBoxV2Binding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CeibroConnectionGroupV2?) {

            binding.apply {
                if (item != null) {
                    groupName.text = item.name
                    groupMemberCount.text = if (item.contacts.size > 1) {
                        "(${item.contacts.size} members)"
                    } else {
                        "(${item.contacts.size} member)"
                    }
                    if (selectAllGroups) {
                        if (!selectedGroup.contains(item)) {
                            selectedGroup.add(item)
                        }
                    }
                    groupCheckBox.isChecked = selectedGroup.contains(item)
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
                        } else {
                            openInfoClickListener?.invoke(item)
                        }

                    }

                    groupMenu.setOnClickListener {
//                    Handler(Looper.getMainLooper()).postDelayed({
                        createPopupWindow(it, item) { tag ->
                            if (tag == "delete") {
                                deleteClickListener?.invoke(item)
                            } else if (tag == "rename") {
                                renameClickListener?.invoke(item)

                            }
                        }
//                    }, 50)
                    }
                }
            }
        }
    }


    private fun createPopupWindow(
        v: View,
        groupResponseV2: CeibroConnectionGroupV2,
        callback: (String) -> Unit
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
                deleteGroupDialog(it.context, groupResponseV2) { tag ->
                    callback.invoke(tag)
                }
            } else {
                cannotDeleteAlertBox(it.context)
            }

        }
        renameGroupBtn.setOnClickListener {
            callback.invoke("rename")
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
        callback: (String) -> Unit
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
            callback.invoke("delete")
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

    fun changeEditFlag(editFlag: Boolean) {
        this.editFlag = editFlag
        // selectAllGroups = editFlag
        notifyDataSetChanged()
    }

    fun selectAllGroups(list: List<CeibroConnectionGroupV2>) {
        this.selectedGroup.clear()
        selectAllGroups = true
        notifyDataSetChanged()
    }

    fun unSelectAllGroups() {
        this.selectedGroup.clear()
        selectAllGroups = false
        notifyDataSetChanged()
    }

}