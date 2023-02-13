package com.zstronics.ceibro.ui.photoeditor

import android.view.View
import android.widget.PopupWindow
import com.zstronics.ceibro.base.interfaces.IBase

interface IPhotoEditor {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {

    }
}