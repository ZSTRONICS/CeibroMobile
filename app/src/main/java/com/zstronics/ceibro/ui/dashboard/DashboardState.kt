package com.zstronics.ceibro.ui.dashboard

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class DashboardState @Inject constructor() : BaseState(), IDashboard.State {
    override val toMeSelected: MutableLiveData<Boolean> = MutableLiveData(true)
    override val fromMeSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    override val hiddenSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    override val locationSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    override val projectsSelected: MutableLiveData<Boolean> = MutableLiveData(false)
    override val selectedItem: MutableLiveData<Int> = MutableLiveData(R.id.nav_home)
    override var connectionCount: MutableLiveData<Int> = MutableLiveData(0)
}