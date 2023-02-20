package com.zstronics.ceibro.ui.chat.messageinfo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentMessageInfoBinding
import com.zstronics.ceibro.ui.chat.adapter.MessageInfoReadByAdapter
import com.zstronics.ceibro.ui.enums.MessageType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MessageInfoFragment :
    BaseNavViewModelFragment<FragmentMessageInfoBinding, IMessageInfo.State, MessageInfoVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: MessageInfoVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_message_info
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
        }
    }

    @Inject
    lateinit var adapter: MessageInfoReadByAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.readByRV.adapter = adapter

        viewState.message.observe(viewLifecycleOwner) {
            with(mViewDataBinding) {
                if (it.type == MessageType.QUESTIONIAR.name.lowercase()) {
                    questionLayout.visibility = View.VISIBLE
                    senderMsgText.visibility = View.GONE
                } else {
                    questionLayout.visibility = View.GONE
                    senderMsgText.visibility = View.VISIBLE
                }
            }
        }

        viewModel.readBy.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.setList(it)
            }
        }
    }
}