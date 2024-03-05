package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing.users

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class UsersFiltersState @Inject constructor() : BaseState(), IUsersFilters.State {
    override val isSelfAssigned: MutableLiveData<Boolean> = MutableLiveData(false)
}