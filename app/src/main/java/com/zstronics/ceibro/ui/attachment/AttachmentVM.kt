package com.zstronics.ceibro.ui.attachment

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AttachmentVM @Inject constructor(
    override val viewState: AttachmentState,
) : HiltBaseViewModel<IAttachment.State>(), IAttachment.ViewModel {
}