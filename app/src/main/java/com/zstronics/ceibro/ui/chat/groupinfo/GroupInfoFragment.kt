package com.zstronics.ceibro.ui.chat.groupinfo

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import com.zstronics.ceibro.databinding.FragmentGroupInfoBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupInfoFragment :
    BaseNavViewModelFragment<FragmentGroupInfoBinding, IGroupInfo.State, GroupInfoVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: GroupInfoVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_group_info
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
            R.id.groupInfoChatMembers -> navToChatMembers()
        }
    }

    private fun navToChatMembers() {
        navigate(R.id.action_groupInfoFragment_to_chatMembersFragment, arguments)
    }
}