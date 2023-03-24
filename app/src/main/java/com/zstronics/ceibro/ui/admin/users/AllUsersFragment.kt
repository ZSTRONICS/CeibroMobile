package com.zstronics.ceibro.ui.admin.users

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentAllUsersBinding
import com.zstronics.ceibro.databinding.FragmentMainAdminBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllUsersFragment :
    BaseNavViewModelFragment<FragmentAllUsersBinding, IAllUsers.State, AllUsersVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: AllUsersVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_all_users
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }
}