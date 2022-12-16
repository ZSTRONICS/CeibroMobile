package com.zstronics.ceibro.ui.invitations

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentInvitationsBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvitationsFragment :
    BaseNavViewModelFragment<FragmentInvitationsBinding, IInvitations.State, InvitationsVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: InvitationsVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_invitations
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }
}