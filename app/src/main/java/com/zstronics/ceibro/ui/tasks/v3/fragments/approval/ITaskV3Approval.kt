package com.zstronics.ceibro.ui.tasks.v3.fragments.approval

import com.zstronics.ceibro.base.interfaces.IBase

interface ITaskV3Approval {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}