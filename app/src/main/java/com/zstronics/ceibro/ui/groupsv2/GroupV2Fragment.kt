package com.zstronics.ceibro.ui.groupsv2

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentGroupV2Binding
import com.zstronics.ceibro.ui.groupsv2.adapter.GroupV2Adapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.AddNewLocationBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.shortToastNow
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
            }

            R.id.deleteAll -> {
                deleteGroupDialog(requireContext()) {
                    mViewDataBinding.cbSelectAll.isChecked = false
                    mViewDataBinding.selectionHeader.visibility = View.GONE
                    viewState.setAddTaskButtonVisibility.postValue(true)
                    adapter.selectedGroup = arrayListOf()
                    adapter.changeEditFlag(false)
                }
            }

            R.id.createNewGroupBtn -> {
                openNewGroupSheet()
            }
        }
    }


    @Inject
    lateinit var adapter: GroupV2Adapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.apply {

            groupsRV.adapter = adapter

            viewModel.connectionGroups.observe(viewLifecycleOwner) {
                if (!it.isNullOrEmpty()) {
                    adapter.setList(it, false)
                } else {
                    adapter.setList(mutableListOf(), false)
                }
            }

//            adapter.itemClickListener = { list ->
//
//                if (list.size >= 10) {
//                    cbSelectAll.isChecked = true
//                } else {
//                    cbSelectAll.isChecked = false
//                }
//                shortToastNow(list.size.toString())
//            }
            cbSelectAll.setOnClickListener {
                if (cbSelectAll.isChecked) {
                    adapter.selectedGroup = arrayListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.selectedGroup = arrayListOf()
                    adapter.notifyDataSetChanged()
                }
            }
        }
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
            viewModel
        )

        sheet.createGroupClickListener = { groupName, selectedContactIds ->
            viewModel.createConnectionGroup(groupName, selectedContactIds) {
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