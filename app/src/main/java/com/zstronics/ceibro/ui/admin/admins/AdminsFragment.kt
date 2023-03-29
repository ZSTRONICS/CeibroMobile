package com.zstronics.ceibro.ui.admin.admins

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.admins.AdminUsersResponse
import com.zstronics.ceibro.databinding.FragmentAdminsBinding
import com.zstronics.ceibro.ui.admin.AdminAndUserAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AdminsFragment :
    BaseNavViewModelFragment<FragmentAdminsBinding, IAdmins.State, AdminsVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: AdminsVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_admins
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }


    @Inject
    lateinit var adapter: AdminAndUserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.adminsRV.adapter = adapter

        viewModel.allAdmins.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }
        adapter.itemClickListener = { _: View, position: Int, data: AdminUsersResponse.AdminUserData ->
            //navigateToMsgView(data)
        }
        adapter.optionMenuItemClickListener = { childView: View, position: Int, data: AdminUsersResponse.AdminUserData ->
            adminPopupMenu(
                position = position,
                v = childView,
                data
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAdmins(mViewDataBinding.adminsRV)
    }


    private fun adminPopupMenu(
        position: Int,
        v: View,
        data: AdminUsersResponse.AdminUserData
    ): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_admin_user_menu, null)

        //following code is to make popup at top if the view is at bottom
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view

        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        if (positionOfIcon > height) {
            popupWindow.showAsDropDown(v, 0, -375)
        } else {
            popupWindow.showAsDropDown(v, 0, 5)
        }
        //////////////////////

        val viewBtn = view.findViewById<LinearLayoutCompat>(R.id.viewBtn)

        viewBtn.setOnClick {
            navigateToPersonalDetails(data)
            popupWindow.dismiss()
        }

        return popupWindow
    }

    private fun navigateToPersonalDetails(data: AdminUsersResponse.AdminUserData) {
        val bundle = Bundle()
        bundle.putParcelable("adminUserData", data)
        navigate(R.id.personalDetailFragment, bundle)
    }

}