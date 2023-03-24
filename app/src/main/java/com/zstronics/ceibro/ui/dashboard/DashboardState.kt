package com.zstronics.ceibro.ui.dashboard

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class DashboardState @Inject constructor() : BaseState(), IDashboard.State {
    override val selectedItem: MutableLiveData<Int> = MutableLiveData(R.id.nav_home)
    override var connectionCount: MutableLiveData<Int> = MutableLiveData(0)
}