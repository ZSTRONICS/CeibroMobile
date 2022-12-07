package com.zstronics.ceibro.ui.chat.individualchat

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SingleNewChatVM @Inject constructor(
    override val viewState: SingleNewChatState,
) : HiltBaseViewModel<ISingleNewChat.State>(), ISingleNewChat.ViewModel {
}