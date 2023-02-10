package com.zstronics.ceibro.ui.attachment.addattachments

import com.zstronics.ceibro.base.interfaces.IBase

interface IAddAttachment {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}