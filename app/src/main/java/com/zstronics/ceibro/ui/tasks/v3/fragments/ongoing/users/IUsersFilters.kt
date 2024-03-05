package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing.users

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase


interface IUsersFilters {
    interface State : IBase.State {
        val isSelfAssigned: MutableLiveData<Boolean>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}