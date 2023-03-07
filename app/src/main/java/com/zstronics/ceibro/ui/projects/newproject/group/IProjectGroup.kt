package com.zstronics.ceibro.ui.projects.newproject.group

import com.zstronics.ceibro.base.interfaces.IBase

interface IProjectGroup {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}