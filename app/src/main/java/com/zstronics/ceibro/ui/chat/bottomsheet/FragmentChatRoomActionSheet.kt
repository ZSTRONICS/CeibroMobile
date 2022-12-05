package com.zstronics.ceibro.ui.chat.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.FragmentChatRoomActionSheetBinding
import com.zstronics.ceibro.ui.enums.ChatActions

class FragmentChatRoomActionSheet : BottomSheetDialogFragment(), View.OnClickListener {
    lateinit var binding: FragmentChatRoomActionSheetBinding

    var actionClick: ((view: View?, chatActions: ChatActions) -> Unit)? =
        null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_chat_room_action_sheet,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.chatMenuMarkUnread.setOnClickListener(this)
        binding.chatMenuMuteChat.setOnClickListener(this)
        binding.chatMenuAddFav.setOnClickListener(this)
        binding.chatMenuDeleteConv.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        val action: ChatActions = when (p0) {
            binding.chatMenuMarkUnread -> ChatActions.MARK_AS_UNREAD
            binding.chatMenuMuteChat -> ChatActions.MUTE_CHAT
            binding.chatMenuAddFav -> ChatActions.ADD_TO_FAV
            binding.chatMenuDeleteConv -> ChatActions.DELETE_CHAT
            else -> ChatActions.NO_ACTION
        }
        actionClick?.invoke(p0, action)
        dismiss()
    }
}