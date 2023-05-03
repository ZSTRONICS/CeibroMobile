package com.zstronics.ceibro.ui.profile

import android.content.Context
import android.view.View
import android.widget.PopupWindow
import com.zstronics.ceibro.base.interfaces.IBase

interface IProfile {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun showMenuPopup(v : View)
        fun popUpMenu(v : View): PopupWindow
        fun endUserSession(context: Context)
    }
}