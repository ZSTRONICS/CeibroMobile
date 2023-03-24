package com.zstronics.ceibro.ui.admin.admins

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentAdminsBinding
import com.zstronics.ceibro.databinding.FragmentMainAdminBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

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
}