package com.zstronics.ceibro.ui.tasks.v3.bottomsheets


import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.databinding.FragmentUsersSheetBinding
import com.zstronics.ceibro.ui.tasks.v3.TasksParentTabV3VM
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.adapters.UsersBottomSheetTabLayoutAdapter

class UsersBottomSheet(
    val viewModel: TasksParentTabV3VM,
    private val connectionscallBack: (ArrayList<AllCeibroConnections.CeibroConnection>) -> Unit,
    private val groupsCallBack: (ArrayList<CeibroConnectionGroupV2>) -> Unit
) :
    BottomSheetDialogFragment() {

    val list = ArrayList<String>()
    lateinit var mViewDataBinding: FragmentUsersSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mViewDataBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_users_sheet,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return mViewDataBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        mViewDataBinding.backBtn.setOnClick {
            dismiss()
        }
        list.add(getString(R.string.users_heading))
        list.add(getString(R.string.users_groups))
        val adapter = UsersBottomSheetTabLayoutAdapter(requireActivity(), viewModel, {
            connectionscallBack.invoke(it)
        }, {
            groupsCallBack.invoke(it)
        })
        mViewDataBinding.viewPager.adapter = adapter


        TabLayoutMediator(mViewDataBinding.tabLayout, mViewDataBinding.viewPager) { tab, position ->
            tab.text = list[position]
        }.attach()

        val tabTextColors = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf()),
            intArrayOf(resources.getColor(R.color.black), resources.getColor(R.color.appBlue))
        )
        mViewDataBinding.tabLayout.tabTextColors = tabTextColors
        mViewDataBinding.tabLayout.setSelectedTabIndicatorColor(Color.BLACK)


    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = false
            dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        return dialog
    }
}