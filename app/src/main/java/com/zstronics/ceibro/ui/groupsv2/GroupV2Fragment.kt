package com.zstronics.ceibro.ui.groupsv2

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
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncDBContactsList
import com.zstronics.ceibro.databinding.FragmentGroupV2Binding
import com.zstronics.ceibro.ui.groupsv2.adapter.GroupV2Adapter
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


@AndroidEntryPoint
class GroupV2Fragment :
    BaseNavViewModelFragment<FragmentGroupV2Binding, IGroupV2.State, GroupV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: GroupV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_group_v2
    override fun toolBarVisibility(): Boolean = false
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
                    shortToastNow("Coming Soon")
                    mViewDataBinding.deleteAll.isClickable = false
                    mViewDataBinding.deleteAll.isEnabled = false
                    mViewDataBinding.cbSelectAll.isChecked = false
                    mViewDataBinding.selectionHeader.visibility = View.GONE
                    viewState.setAddTaskButtonVisibility.postValue(true)
                    adapter.selectedGroup = arrayListOf()
                    adapter.changeEditFlag(false)
                    mViewDataBinding.groupMenuBtn.isEnabled = true
                    val newColor = ContextCompat.getColor(requireContext(), R.color.appBlue)
                    mViewDataBinding.groupMenuBtn.setColorFilter(newColor, PorterDuff.Mode.SRC_IN)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mViewDataBinding.groupsRV.adapter = adapter

        viewModel.connectionGroups.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                adapter.setList(it, false)
            } else {
                adapter.setList(mutableListOf(), false)
            }
        }
        viewModel.filteredGroups.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                adapter.setList(it, false)
            } else {
                adapter.setList(mutableListOf(), false)
            }
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
        adapter.deleteClickListener = { item ->
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
        adapter.renameClickListener = { item, contacts ->
            openUpdateGroupSheet(item, contacts)
        }

        mViewDataBinding.cbSelectAll.setOnClickListener {
            if (mViewDataBinding.cbSelectAll.isChecked) {

                adapter.selectAllGroups(viewModel.originalConnectionGroups)
            } else {
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
            popupWindow.showAsDropDown(v, -200, -170)
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

        sheet.createGroupClickListener = { groupName, selectedContactIds ->
            viewModel.createConnectionGroup(groupName, selectedContactIds) { createdGroup ->
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

        sheet.isCancelable = false
        sheet.show(childFragmentManager, "AddNewGroupV2Sheet")
    }

    private fun openUpdateGroupSheet(
        item: CeibroConnectionGroupV2,
        contacts: List<SyncDBContactsList.CeibroDBContactsLight>
    ) {
        val sheet = AddNewGroupV2Sheet(
            viewModel.connectionsV2Dao,
            viewModel,
            isUpdating = true
        )
        sheet.item = item
        sheet.contact = contacts

        sheet.updateGroupClickListener = { item, groupName, selectedContactIds ->
            viewModel.updateConnectionGroup(item, groupName, selectedContactIds) { updatedGroup ->

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
}