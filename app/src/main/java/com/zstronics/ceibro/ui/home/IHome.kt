package com.zstronics.ceibro.ui.home

import com.zstronics.ceibro.base.interfaces.IBase

interface IHome {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun loadProjects(publishStatus: String)
        fun getTasks()
    }
}