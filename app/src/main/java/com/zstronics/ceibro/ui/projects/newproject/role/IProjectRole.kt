package com.zstronics.ceibro.ui.projects.newproject.role

import com.zstronics.ceibro.base.interfaces.IBase

interface IProjectRole {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}