package com.zstronics.ceibro.ui.invitations

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.finish
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitationsItem
import com.zstronics.ceibro.databinding.FragmentInvitationsBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import com.zstronics.ceibro.ui.chat.adapter.ChatRoomAdapter
import com.zstronics.ceibro.ui.invitations.adapter.AllInvitationsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InvitationsFragment :
    BaseNavViewModelFragment<FragmentInvitationsBinding, IInvitations.State, InvitationsVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: InvitationsVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_invitations
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> navigateBack()
            R.id.inviteBtn -> {
                val emailPattern = Patterns.EMAIL_ADDRESS
                if (emailPattern.matcher(viewState.inviteEmail.value.toString()).matches()) {
                    viewModel.onInvite()
                } else {
                    shortToastNow("Invalid Email Address")
                }
            }
        }
    }

    @Inject
    lateinit var adapter: AllInvitationsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView(adapter)

        viewModel.allInvites.observe(viewLifecycleOwner) {
            adapter.setList(it)

            if (viewModel.allInvites.value?.size == 0){
                mViewDataBinding.acceptAllBtn.isEnabled = false
                mViewDataBinding.acceptAllBtn.setTextColor(R.color.appTextGrey)
                mViewDataBinding.declineAllBtn.isEnabled = false
                mViewDataBinding.declineAllBtn.setTextColor(R.color.appTextGrey)
            }
        }

        adapter.itemClickListener = { _: View, position: Int, data: MyInvitationsItem ->
//            navigateToMsgView(data)
        }
        adapter.childItemClickListener = { view: View, position: Int, data: MyInvitationsItem ->
            if (view.id == R.id.acceptInviteBtn) {
//                shortToastNow("acceptInviteBtn")
                viewModel.acceptOrRejectInvitation(
                    accepted = true,
                    inviteId = data.id,
                    position = position
                )
            }
            else if (view.id == R.id.rejectInviteBtn) {
//                shortToastNow("rejectInviteBtn")
                viewModel.acceptOrRejectInvitation(
                    accepted = false,
                    inviteId = data.id,
                    position = position
                )
            }
        }




    }

    private fun initRecyclerView(adapter: AllInvitationsAdapter) {
        mViewDataBinding.invitationsRV.adapter = adapter

        adapter.itemLongClickListener =
            { _: View, position: Int, data: MyInvitationsItem ->
//                showChatActionSheet(data,position)
            }
    }

}