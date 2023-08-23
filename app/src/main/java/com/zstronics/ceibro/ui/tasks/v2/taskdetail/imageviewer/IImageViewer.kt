package com.zstronics.ceibro.ui.tasks.v2.taskdetail.imageviewer

import com.zstronics.ceibro.base.interfaces.IBase

interface IImageViewer {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}