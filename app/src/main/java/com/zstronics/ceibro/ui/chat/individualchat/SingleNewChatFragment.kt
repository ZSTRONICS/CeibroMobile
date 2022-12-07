package com.zstronics.ceibro.ui.chat.individualchat

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentSingleNewChatBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SingleNewChatFragment :
    BaseNavViewModelFragment<FragmentSingleNewChatBinding, ISingleNewChat.State, SingleNewChatVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: SingleNewChatVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_single_new_chat
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> navigateBack()
        }
    }
}