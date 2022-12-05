package com.zstronics.ceibro.ui.projects

import com.zstronics.ceibro.base.interfaces.IBase

interface IProjects {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun loadProjects(publishStatus: String)
    }
}