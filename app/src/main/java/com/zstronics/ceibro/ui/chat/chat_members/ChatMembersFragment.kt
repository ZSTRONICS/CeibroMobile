package com.zstronics.ceibro.ui.chat.chat_members

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentChatMembersBinding
import com.zstronics.ceibro.ui.chat.adapter.ChatMembersAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatMembersFragment :
    BaseNavViewModelFragment<FragmentChatMembersBinding, IChatMembers.State, ChatMembersVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ChatMembersVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_chat_members
    override fun toolBarVisibility(): Boolean = true
    override fun getToolBarTitle() = "Chat Members"
    override fun setHomeAsUpIndicator() = R.drawable.icon_round_close
    override fun onClick(id: Int) {

    }

    @Inject
    lateinit var adapter: ChatMembersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.chatMembersRV.adapter = adapter
        viewModel.chatMembers.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }

    }
}