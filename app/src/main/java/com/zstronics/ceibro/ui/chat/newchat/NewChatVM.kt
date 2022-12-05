package com.zstronics.ceibro.ui.chat.newchat

import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewChatVM @Inject constructor(
    override val viewState: NewChatState,
) : HiltBaseViewModel<INewChat.State>(), INewChat.ViewModel {
}