package com.zstronics.ceibro.ui.groupsv2

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupWindow
import android.widget.SearchView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.navgraph.BackNavigationResult
import com.zstronics.ceibro.base.navgraph.BackNavigationResultListener
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncDBContactsList
import com.zstronics.ceibro.databinding.FragmentGroupV2Binding
import com.zstronics.ceibro.ui.contacts.toLightDBGroupContacts
import com.zstronics.ceibro.ui.contacts.toLightGroupContactsFromTaskMember
import com.zstronics.ceibro.ui.groupsv2.adapter.AllGroupsAdapterSectionRecycler
import com.zstronics.ceibro.ui.groupsv2.adapter.GroupSectionHeader
import com.zstronics.ceibro.ui.groupsv2.adapter.GroupV2Adapter
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawings.AllDrawingsAdapterSectionRecycler
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawings.DrawingSectionHeader
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


@AndroidEntryPoint
class GroupV2Fragment :
    BaseNavViewModelFragment<FragmentGroupV2Binding, IGroupV2.State, GroupV2VM>(),
    BackNavigationResultListener {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: GroupV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_group_v2
    override fun toolBarVisibility(): Boolean = false

    private val GROUP_ADMIN_REQUEST_CODE = 1
    private val ASSIGNEE_REQUEST_CODE = 2
    private val CONFIRMER_REQUEST_CODE = 3
    private val VIEWER_REQUEST_CODE = 4
    private val SHARE_REQUEST_CODE = 5
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                navigateBack()
            }

            R.id.groupMenuBtn -> {
                selectPopupWindow(mViewDataBinding.groupMenuBtn) {
                    if (it == "select") {
                        mViewDataBinding.groupMenuBtn.isEnabled = false
                        val newColor = ContextCompat.getColor(requireContext(), R.color.appGrey3)
                        mViewDataBinding.groupMenuBtn.setColorFilter(
                            newColor,
                            PorterDuff.Mode.SRC_IN
                        )

                        mViewDataBinding.cbSelectAll.isChecked = false
                        mViewDataBinding.selectionHeader.visibility = View.VISIBLE
                        viewState.setAddTaskButtonVisibility.postValue(false)
                        adapter.changeEditFlag(true)
                    }
                }
            }

            R.id.cancel -> {
                mViewDataBinding.cbSelectAll.isChecked = false
                mViewDataBinding.selectionHeader.visibility = View.GONE
                viewState.setAddTaskButtonVisibility.postValue(true)
                adapter.selectedGroup = arrayListOf()
                adapter.changeEditFlag(false)
                mViewDataBinding.deleteAll.isClickable = false
                mViewDataBinding.deleteAll.isEnabled = false
                mViewDataBinding.groupMenuBtn.isEnabled = true
                val newColor = ContextCompat.getColor(requireContext(), R.color.appBlue)
                mViewDataBinding.groupMenuBtn.setColorFilter(newColor, PorterDuff.Mode.SRC_IN)
            }

            R.id.deleteAll -> {
                deleteGroupDialog(requireContext()) {
                    if (adapter.selectedGroup.size > 0) {
                        viewModel.deleteConnectionGroupsInBulk(adapter.selectedGroup) { list ->
                            list.forEach { item ->
                                val allOriginalGroups = viewModel.originalConnectionGroups
                                val groupFound = allOriginalGroups.find { it._id == item }
                                if (groupFound != null) {
                                    val index = allOriginalGroups.indexOf(groupFound)
                                    allOriginalGroups.removeAt(index)
                                    viewModel.originalConnectionGroups = allOriginalGroups
                                }

                                val adapterItemFound =
                                    adapter.groupListItems.find { it._id == item }
                                if (adapterItemFound != null) {
                                    val index1 = adapter.groupListItems.indexOf(adapterItemFound)
                                    adapter.groupListItems.removeAt(index1)
                                    adapter.notifyItemRemoved(index1)
                                }

                            }


                            mViewDataBinding.deleteAll.isClickable = false
                            mViewDataBinding.deleteAll.isEnabled = false
                            mViewDataBinding.cbSelectAll.isChecked = false
                            mViewDataBinding.selectionHeader.visibility = View.GONE
                            viewState.setAddTaskButtonVisibility.postValue(true)
                            adapter.selectedGroup = arrayListOf()
                            adapter.changeEditFlag(false)
                            mViewDataBinding.groupMenuBtn.isEnabled = true
                            val newColor = ContextCompat.getColor(requireContext(), R.color.appBlue)
                            mViewDataBinding.groupMenuBtn.setColorFilter(
                                newColor,
                                PorterDuff.Mode.SRC_IN
                            )
                        }
                    }
                }
            }

            R.id.createNewGroupBtn -> {
                openNewGroupSheet()
            }

            R.id.groupSearchClearBtn -> {
                mViewDataBinding.groupSearchBar.setQuery("", true)
                mViewDataBinding.groupSearchBar.clearFocus()
                mViewDataBinding.groupSearchBar.hideKeyboard()
                mViewDataBinding.groupSearchClearBtn.visibility = View.GONE
            }
        }
    }


    @Inject
    lateinit var adapter: GroupV2Adapter

    private lateinit var sectionedAdapter: AllGroupsAdapterSectionRecycler
    private var sectionList: MutableList<GroupSectionHeader> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sectionList.add(
            0,
            GroupSectionHeader(
                mutableListOf(),
                getString(R.string.my_groups)
            )
        )
        sectionList.add(
            1,
            GroupSectionHeader(
                mutableListOf(),
                getString(R.string.other_groups)
            )
        )

        sectionedAdapter = AllGroupsAdapterSectionRecycler(
            requireContext(),
            sectionList,
            networkConnectivityObserver
        )

        val linearLayoutManager = LinearLayoutManager(requireContext())
        mViewDataBinding.groupsRV.layoutManager = linearLayoutManager
        mViewDataBinding.groupsRV.setHasFixedSize(true)
        mViewDataBinding.groupsRV.adapter = sectionedAdapter


        viewModel.myGroupData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                sectionList.removeAt(0)
                sectionList.add(
                    0, GroupSectionHeader(it, getString(R.string.my_groups))
                )
                sectionedAdapter.insertNewSection(
                    GroupSectionHeader(
                        it,
                        getString(R.string.my_groups)
                    ), 0
                )
                sectionedAdapter.notifyDataSetChanged()

            } else {
                sectionList.removeAt(0)
                sectionList.add(
                    0, GroupSectionHeader(mutableListOf(), getString(R.string.my_groups))
                )
                sectionedAdapter.insertNewSection(
                    GroupSectionHeader(
                        mutableListOf(),
                        getString(R.string.my_groups)
                    ), 0
                )
                sectionedAdapter.notifyDataSetChanged()
            }
        }

        viewModel.otherGroupsData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val groupedByCreatorId = it.groupBy { group -> group.creator.id }

                // Creating an array of arrays where each inner array contains groups with the same creator._id
                val result = groupedByCreatorId.values.toList()

                if (sectionList.size > 1) {
                    val iterator = sectionList.iterator()
                    var count = 0

                    while (iterator.hasNext()) {
                        if (count > 1) {
                            iterator.remove()
                        }

                        iterator.next()
                        count++
                    }
                }

                result.mapIndexed { index, creatorGroups ->
                    println("otherGroupsData: ${creatorGroups.size} groups: ${creatorGroups}")

                    sectionList.add(
                        index + 1,
                        GroupSectionHeader(
                            creatorGroups.toMutableList(),
                            "Group shared by: ${creatorGroups[0].creator.firstName} ${creatorGroups[0].creator.surName}"
                        )
                    )
                    sectionedAdapter.insertNewSection(
                        GroupSectionHeader(
                            creatorGroups.toMutableList(),
                            "Group shared by: ${creatorGroups[0].creator.firstName} ${creatorGroups[0].creator.surName}"
                        ), index + 1
                    )
                }
                sectionedAdapter.notifyDataSetChanged()


            } else {
                if (sectionList.size > 1) {
                    val iterator = sectionList.iterator()
                    var count = 0

                    while (iterator.hasNext()) {
                        if (count > 1) {
                            iterator.remove()
                        }

                        iterator.next()
                        count++
                    }
                }
                sectionList.add(
                    1, GroupSectionHeader(mutableListOf(), getString(R.string.other_groups))
                )
                sectionedAdapter.insertNewSection(
                    GroupSectionHeader(
                        mutableListOf(),
                        getString(R.string.other_groups)
                    ), 1
                )
                sectionedAdapter.notifyDataSetChanged()
            }
        }


//        mViewDataBinding.groupsRV.adapter = adapter

        viewModel.connectionGroups.observe(viewLifecycleOwner) {
//            if (!it.isNullOrEmpty()) {
//                adapter.setList(it, false)
//            } else {
//                adapter.setList(mutableListOf(), false)
//            }
        }
        viewModel.filteredGroups.observe(viewLifecycleOwner) {
//            if (!it.isNullOrEmpty()) {
//                adapter.setList(it, false)
//            } else {
//                adapter.setList(mutableListOf(), false)
//            }
        }

        sectionedAdapter.openInfoClickListener = { group ->
            showGroupInfoBottomSheet(group)
        }

        sectionedAdapter.deleteClickListener = { item ->
            viewModel.deleteConnectionGroup(item._id) {
                val allOriginalGroups = viewModel.originalConnectionGroups
                val groupFound = allOriginalGroups.find { it._id == item._id }
                if (groupFound != null) {
                    val index = allOriginalGroups.indexOf(groupFound)
                    allOriginalGroups.removeAt(index)
                    viewModel.originalConnectionGroups = allOriginalGroups
                }

                val adapterItemFound = adapter.groupListItems.find { it._id == item._id }
                if (adapterItemFound != null) {
                    val index1 = adapter.groupListItems.indexOf(adapterItemFound)
                    adapter.groupListItems.removeAt(index1)
                    adapter.notifyItemRemoved(index1)
                }
            }
        }
        sectionedAdapter.renameClickListener = { item ->
            viewModel.updateGroupData(item)
            openUpdateGroupSheet(item)
        }

        adapter.itemClickListener = { list ->

            mViewDataBinding.cbSelectAll.isChecked =
                list.size == viewModel.originalConnectionGroups.size
            if (list.size > 0) {
                mViewDataBinding.deleteAll.isClickable = true
                mViewDataBinding.deleteAll.isEnabled = true

            } else {
                mViewDataBinding.deleteAll.isClickable = false
                mViewDataBinding.deleteAll.isEnabled = false

            }
        }

        mViewDataBinding.cbSelectAll.setOnClickListener {
            if (mViewDataBinding.cbSelectAll.isChecked) {

                adapter.selectAllGroups(viewModel.originalConnectionGroups)
                mViewDataBinding.deleteAll.isClickable = true
                mViewDataBinding.deleteAll.isEnabled = true
            } else {
                mViewDataBinding.deleteAll.isClickable = false
                mViewDataBinding.deleteAll.isEnabled = false
                adapter.selectedGroup = arrayListOf()
                adapter.notifyDataSetChanged()
            }
        }

        mViewDataBinding.groupSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.filterGroups(query)
                }
                if (!query.isNullOrEmpty()) {
                    mViewDataBinding.groupSearchClearBtn.visibility = View.VISIBLE
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewModel.filterGroups(newText)
                }
                if (!newText.isNullOrEmpty()) {
                    mViewDataBinding.groupSearchClearBtn.visibility = View.VISIBLE
                }
                return true
            }
        })
    }

    override fun onStart() {
        super.onStart()
        try {
            EventBus.getDefault().register(this)
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun selectPopupWindow(
        v: View,
        selectGroupCallBack: (type: String) -> Unit
    ): PopupWindow {
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.groupv2_menu_dialog, null)

        val popupWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true


        val selectGroup: TextView = view.findViewById(R.id.selectGroup)

        selectGroup.setOnClickListener {
            popupWindow.dismiss()
            selectGroupCallBack.invoke("select")
        }

        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        if (positionOfIcon > height) {
            popupWindow.showAsDropDown(v, -205, -170)
        } else {
            popupWindow.showAsDropDown(v, -205, -60)
        }

        return popupWindow
    }


    private fun openNewGroupSheet() {
        val sheet = AddNewGroupV2Sheet(
            viewModel.connectionsV2Dao,
            viewModel,
            false
        )

        sheet.createGroupClickListener = { groupName ->
            viewModel.createConnectionGroup(groupName) { createdGroup ->
                sheet.dismiss()

                val allOriginalGroups = viewModel.originalConnectionGroups
                val groupFound = allOriginalGroups.find { it._id == createdGroup._id }
                if (groupFound != null) {
                    val index = allOriginalGroups.indexOf(groupFound)
                    allOriginalGroups[index] = createdGroup
                    viewModel.originalConnectionGroups = allOriginalGroups
                } else {
                    allOriginalGroups.add(0, createdGroup)
                    viewModel.originalConnectionGroups = allOriginalGroups
                }

                val adapterItemFound = adapter.groupListItems.find { it._id == createdGroup._id }
                if (adapterItemFound != null) {
                    val index1 = adapter.groupListItems.indexOf(adapterItemFound)
                    adapter.groupListItems[index1] = createdGroup
                    adapter.notifyItemChanged(index1)
                } else {
                    adapter.groupListItems.add(0, createdGroup)
                    adapter.notifyItemInserted(0)
                }
            }
        }
        sheet.updateCallBack = { type ->

            getContactsFromNextScreen(type)

        }


        sheet.isCancelable = false
        sheet.show(childFragmentManager, "AddNewGroupV2Sheet")
    }

    private fun openUpdateGroupSheet(
        oldGroup: CeibroConnectionGroupV2
    ) {

        var groupToSendToSheet: CeibroConnectionGroupV2? = null
        var contactToSendToSheet: List<SyncDBContactsList.CeibroDBContactsLight>? = null

        val allOriginalGroups1 = viewModel.originalConnectionGroups
        val groupFound1 = allOriginalGroups1.find { it._id == oldGroup._id }
        if (groupFound1 != null) {
            groupToSendToSheet = groupFound1
            contactToSendToSheet = groupFound1.contacts.toLightGroupContactsFromTaskMember()
        } else {
            groupToSendToSheet = oldGroup
            contactToSendToSheet = oldGroup.contacts.toLightGroupContactsFromTaskMember()
        }

        val sheet = AddNewGroupV2Sheet(
            viewModel.connectionsV2Dao,
            viewModel,
            isUpdating = true,
            oldGroup = groupToSendToSheet,
            oldGroupContact = contactToSendToSheet
        )


        sheet.updateGroupClickListener = { item, groupName, isGroupNameSame ->
            viewModel.updateConnectionGroup(
                item,
                groupName, isGroupNameSame
            ) { updatedGroup ->

                val allOriginalGroups = viewModel.originalConnectionGroups
                val groupFound = allOriginalGroups.find { it._id == updatedGroup._id }
                if (groupFound != null) {
                    val index = allOriginalGroups.indexOf(groupFound)
                    allOriginalGroups[index] = updatedGroup
                    viewModel.originalConnectionGroups = allOriginalGroups
                }

                val adapterItemFound = adapter.groupListItems.find { it._id == updatedGroup._id }
                if (adapterItemFound != null) {
                    val index1 = adapter.groupListItems.indexOf(adapterItemFound)
                    adapter.groupListItems[index1] = updatedGroup
                    adapter.notifyItemChanged(index1)
                }


                sheet.dismiss()
            }
        }
        sheet.updateCallBack = { type ->
            getContactsFromNextScreen(type)
        }


        sheet.isCancelable = false
        sheet.show(childFragmentManager, "AddNewGroupV2Sheet")
    }

    private fun deleteGroupDialog(
        context: Context,
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
            context.resources.getString(R.string.are_you_sure_you_want_to_delete_groups)
        alertDialog.window?.setBackgroundDrawable(null)
        alertDialog.show()

        yesBtn.setOnClickListener {
            callback.invoke("delete")
            alertDialog.dismiss()

        }

        noBtn.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun showGroupInfoBottomSheet(group: CeibroConnectionGroupV2) {
        val sheet = GroupInfoBottomSheet(group)
//        sheet.dialog?.window?.setSoftInputMode(
//            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
//                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
//        );

        sheet.isCancelable = false
        sheet.show(childFragmentManager, "GroupInfoBottomSheet")
    }


    override fun onNavigationResult(result: BackNavigationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            when (result.requestCode) {

                GROUP_ADMIN_REQUEST_CODE -> {

                    val selfAssigned = result.data?.getBoolean("self-assign")
                    val selectedContact = result.data?.getParcelableArray("contacts")
                    val selectedContactList =
                        selectedContact?.map { it as AllCeibroConnections.CeibroConnection }
                            ?.toMutableList()
                    val selectedItem = selectedContactList?.find { item1 ->
                        item1.id == viewModel.user?.id
                    }
                    if (selectedItem != null) {
                        val index = selectedContactList.indexOf(selectedItem)
                        selectedContactList.removeAt(index)
                    }

                    var assigneeMembers = ""

                    if (selfAssigned != null) {
                        if (selfAssigned) {
                            assigneeMembers += if (selectedContactList.isNullOrEmpty()) {
                                "Me"
                            } else {
                                "Me; "
                            }
                        }
                        viewModel.adminSelfAssigned.value = selfAssigned
                    }

                    var index = 0
                    if (selectedContactList != null) {
                        for (item in selectedContactList) {
                            assigneeMembers += if (index == selectedContactList.size - 1) {
                                "${item.contactFirstName} ${item.contactSurName}"
                            } else {
                                "${item.contactFirstName} ${item.contactSurName}; "
                            }
                            index++
                        }
                        viewModel.adminSelectedContacts.value = selectedContactList
                    }
                    viewModel.adminAssignToText.value = assigneeMembers
                }

                ASSIGNEE_REQUEST_CODE -> {

                    val selfAssigned = result.data?.getBoolean("self-assign")
                    val selectedContact = result.data?.getParcelableArray("contacts")
                    val selectedContactList =
                        selectedContact?.map { it as AllCeibroConnections.CeibroConnection }
                            ?.toMutableList()
                    val selectedItem = selectedContactList?.find { item1 ->
                        item1.id == viewModel.user?.id
                    }
                    if (selectedItem != null) {
                        val index = selectedContactList.indexOf(selectedItem)
                        selectedContactList.removeAt(index)
                    }

                    var assigneeMembers = ""

                    if (selfAssigned != null) {
                        if (selfAssigned) {
                            assigneeMembers += if (selectedContactList.isNullOrEmpty()) {
                                "Me"
                            } else {
                                "Me; "
                            }
                        }
                        viewModel.assigneeSelfAssigned.value = selfAssigned
                    }

                    var index = 0
                    if (selectedContactList != null) {
                        for (item in selectedContactList) {
                            assigneeMembers += if (index == selectedContactList.size - 1) {
                                "${item.contactFirstName} ${item.contactSurName}"
                            } else {
                                "${item.contactFirstName} ${item.contactSurName}; "
                            }
                            index++
                        }
                        viewModel.assigneeSelectedContacts.value = selectedContactList
                    }
                    viewModel.assigneeAssignToText.value = assigneeMembers
                }

                CONFIRMER_REQUEST_CODE -> {

                    val selfAssigned = result.data?.getBoolean("self-assign")
                    val selectedContact = result.data?.getParcelableArray("contacts")
                    val selectedContactList =
                        selectedContact?.map { it as AllCeibroConnections.CeibroConnection }
                            ?.toMutableList()
                    val selectedItem = selectedContactList?.find { item1 ->
                        item1.id == viewModel.user?.id
                    }
                    if (selectedItem != null) {
                        val index = selectedContactList.indexOf(selectedItem)
                        selectedContactList.removeAt(index)
                    }

                    var assigneeMembers = ""

                    if (selfAssigned != null) {
                        if (selfAssigned) {
                            assigneeMembers += if (selectedContactList.isNullOrEmpty()) {
                                "Me"
                            } else {
                                "Me; "
                            }
                        }
                        viewModel.confirmerSelfAssigned.value = selfAssigned
                    }

                    var index = 0
                    if (selectedContactList != null) {
                        for (item in selectedContactList) {
                            assigneeMembers += if (index == selectedContactList.size - 1) {
                                "${item.contactFirstName} ${item.contactSurName}"
                            } else {
                                "${item.contactFirstName} ${item.contactSurName}; "
                            }
                            index++
                        }
                        viewModel.confirmerSelectedContacts.value = selectedContactList
                    }
                    viewModel.confirmerAssignToText.value = assigneeMembers
                }

                VIEWER_REQUEST_CODE -> {

                    val selfAssigned = result.data?.getBoolean("self-assign")
                    val selectedContact = result.data?.getParcelableArray("contacts")
                    val selectedContactList =
                        selectedContact?.map { it as AllCeibroConnections.CeibroConnection }
                            ?.toMutableList()
                    val selectedItem = selectedContactList?.find { item1 ->
                        item1.id == viewModel.user?.id
                    }
                    if (selectedItem != null) {
                        val index = selectedContactList.indexOf(selectedItem)
                        selectedContactList.removeAt(index)
                    }

                    var assigneeMembers = ""

                    if (selfAssigned != null) {
                        if (selfAssigned) {
                            assigneeMembers += if (selectedContactList.isNullOrEmpty()) {
                                "Me"
                            } else {
                                "Me; "
                            }
                        }
                        viewModel.viewerSelfAssigned.value = selfAssigned
                    }

                    var index = 0
                    if (selectedContactList != null) {
                        for (item in selectedContactList) {
                            assigneeMembers += if (index == selectedContactList.size - 1) {
                                "${item.contactFirstName} ${item.contactSurName}"
                            } else {
                                "${item.contactFirstName} ${item.contactSurName}; "
                            }
                            index++
                        }
                        viewModel.viewerSelectedContacts.value = selectedContactList
                    }
                    viewModel.viewerAssignToText.value = assigneeMembers
                }

                SHARE_REQUEST_CODE -> {

                    val selfAssigned = result.data?.getBoolean("self-assign")
                    val selectedContact = result.data?.getParcelableArray("contacts")
                    val selectedContactList =
                        selectedContact?.map { it as AllCeibroConnections.CeibroConnection }
                            ?.toMutableList()
                    val selectedItem = selectedContactList?.find { item1 ->
                        item1.id == viewModel.user?.id
                    }
                    if (selectedItem != null) {
                        val index = selectedContactList.indexOf(selectedItem)
                        selectedContactList.removeAt(index)
                    }

                    var assigneeMembers = ""

                    if (selfAssigned != null) {
                        if (selfAssigned) {
                            assigneeMembers += if (selectedContactList.isNullOrEmpty()) {
                                "Me"
                            } else {
                                "Me; "
                            }
                        }
                        viewModel.shareSelfAssigned.value = selfAssigned
                    }

                    var index = 0
                    if (selectedContactList != null) {
                        for (item in selectedContactList) {
                            assigneeMembers += if (index == selectedContactList.size - 1) {
                                "${item.contactFirstName} ${item.contactSurName}"
                            } else {
                                "${item.contactFirstName} ${item.contactSurName}; "
                            }
                            index++
                        }
                        viewModel.shareSelectedContacts.value = selectedContactList
                    }
                    viewModel.shareAssignToText.value = assigneeMembers
                }

            }
        }
    }

    private fun getContactsFromNextScreen(type: String) {
        if (type.equals("Admin", true)) {

            val selectedContacts= viewModel.adminSelectedContacts.value?: mutableListOf()
            val bundle = Bundle()
            bundle.putParcelableArray(
                "contacts",
                selectedContacts.toTypedArray()
            )
            bundle.putBoolean("self-assign", viewModel.adminSelfAssigned.value ?: false)
            bundle.putBoolean("isConfirmer", false)
            bundle.putBoolean("isViewer", false)

            navigateForResult(R.id.groupAssigneeFragment, GROUP_ADMIN_REQUEST_CODE, bundle)
        } else if (type.equals("Assign", true)) {

            val selectedContacts = viewModel.assigneeSelectedContacts.value?: mutableListOf()
            val disabledContacts = viewModel.viewerSelectedContacts.value ?: mutableListOf()

            val bundle = Bundle()
            bundle.putParcelableArray(
                "contacts",
                selectedContacts.toTypedArray()
            )
            bundle.putBoolean("self-assign", viewModel.assigneeSelfAssigned.value ?: false)
            bundle.putBoolean("isConfirmer", false)
            bundle.putBoolean("isViewer", false)
            bundle.putParcelableArray(
                "disabledContacts",
                disabledContacts.toTypedArray()
            )

            navigateForResult(R.id.groupAssigneeFragment, ASSIGNEE_REQUEST_CODE,bundle)
        } else if (type.equals("Confirmer", true)) {
            val selectedContacts = viewModel.confirmerSelectedContacts.value?: mutableListOf()
            val disabledContacts = viewModel.viewerSelectedContacts.value?: mutableListOf()

            val bundle = Bundle()
            bundle.putParcelableArray(
                "contacts",
                selectedContacts.toTypedArray()
            )
            bundle.putBoolean("self-assign", viewModel.confirmerSelfAssigned.value ?: false)
            bundle.putBoolean("isConfirmer", false)
            bundle.putBoolean("isViewer", false)
            bundle.putParcelableArray(
                "disabledContacts",
                disabledContacts.toTypedArray()
            )

            navigateForResult(R.id.groupAssigneeFragment, CONFIRMER_REQUEST_CODE,bundle)
        } else if (type.equals("Viewer", true)) {

            val selectedContacts = viewModel.viewerSelectedContacts.value?:mutableListOf()


            val assignee = viewModel.assigneeSelectedContacts.value ?: mutableListOf()
            val disabledContacts = mutableListOf<AllCeibroConnections.CeibroConnection>()
            assignee.forEach {
                disabledContacts.add(it.copy())
            }
            val confirmer = viewModel.confirmerSelectedContacts.value
            confirmer?.forEach {
                disabledContacts.add(it.copy())
            }


            val bundle = Bundle()
            bundle.putParcelableArray(
                "contacts",
                selectedContacts.toTypedArray()
            )
            bundle.putBoolean("self-assign", viewModel.viewerSelfAssigned.value ?: false)
            bundle.putBoolean("isConfirmer", false)
            bundle.putBoolean("isViewer", true)
            bundle.putParcelableArray(
                "disabledContacts",
                disabledContacts.toTypedArray()
            )

            navigateForResult(R.id.groupAssigneeFragment, VIEWER_REQUEST_CODE, bundle)
        } else if (type.equals("ShareWith", true)) {
            val selectedContacts=viewModel.shareSelectedContacts.value?: mutableListOf()
            val bundle = Bundle()
            bundle.putParcelableArray(
                "contacts",
                selectedContacts.toTypedArray()
            )
            bundle.putBoolean("self-assign", viewModel.shareSelfAssigned.value ?: false)
            bundle.putBoolean("isConfirmer", false)
            bundle.putBoolean("isViewer", false)

            navigateForResult(R.id.groupAssigneeFragment, SHARE_REQUEST_CODE, bundle)
        }

    }
}