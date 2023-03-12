package com.zstronics.ceibro.ui.projects.newproject.members

import com.zstronics.ceibro.base.interfaces.IBase

interface IProjectMembers {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}