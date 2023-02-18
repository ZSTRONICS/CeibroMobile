package com.zstronics.ceibro.ui.attachment.addattachments

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddAttachmentVM @Inject constructor(
    override val viewState: AddAttachmentState
) : HiltBaseViewModel<IAddAttachment.State>(), IAddAttachment.ViewModel {
}