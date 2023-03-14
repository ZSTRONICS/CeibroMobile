package com.zstronics.ceibro.ui.projects.newproject.documents

import com.zstronics.ceibro.base.interfaces.IBase

interface IProjectDocuments {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}