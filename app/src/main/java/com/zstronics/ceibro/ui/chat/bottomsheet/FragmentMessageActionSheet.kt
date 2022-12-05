package com.zstronics.ceibro.ui.chat.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.FragmentMessageActionSheetBinding
import com.zstronics.ceibro.ui.enums.MessageActions

class FragmentMessageActionSheet : BottomSheetDialogFragment(), View.OnClickListener {
    lateinit var binding: FragmentMessageActionSheetBinding

    var actionClick: ((view: View?, messageAction: MessageActions) -> Unit)? =
        null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_message_action_sheet,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.chatMsgReplyBtn.setOnClickListener(this)
        binding.chatMsgForwardBtn.setOnClickListener(this)
        binding.chatMsgChatMembers.setOnClickListener(this)
        binding.chatMsgMessageInfo.setOnClickListener(this)
        binding.chatMenuDeleteConv.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        val action: MessageActions = when (p0) {
            binding.chatMsgReplyBtn -> MessageActions.REPLY_TO_MESSAGE
            binding.chatMsgForwardBtn -> MessageActions.FORWARD_MESSAGE
            binding.chatMsgChatMembers -> MessageActions.VIEW_CHAT_MEMBERS
            binding.chatMsgMessageInfo -> MessageActions.VIEW_MESSAGE_INFO
            binding.chatMenuDeleteConv -> MessageActions.DELETE_CONVERSATION
            else -> MessageActions.NO_ACTION
        }
        actionClick?.invoke(p0, action)
        dismiss()
    }
}