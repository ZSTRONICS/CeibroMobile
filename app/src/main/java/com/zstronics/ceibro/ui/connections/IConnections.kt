package com.zstronics.ceibro.ui.connections

import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.base.interfaces.IBase

interface IConnections {
    interface State : IBase.State {
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun loadConnections(connectionRV: RecyclerView)
    }
}