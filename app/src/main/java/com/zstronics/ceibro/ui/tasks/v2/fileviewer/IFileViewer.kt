package com.zstronics.ceibro.ui.tasks.v2.fileviewer

import com.zstronics.ceibro.base.interfaces.IBase

interface IFileViewer {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}