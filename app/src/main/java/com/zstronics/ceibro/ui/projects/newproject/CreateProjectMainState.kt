package com.zstronics.ceibro.ui.projects.newproject

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class CreateProjectMainState @Inject constructor() : BaseState(), ICreateProjectMain.State {
    override val isProjectCreated: MutableLiveData<Boolean> = MutableLiveData(false)
    override val selectedTabId: MutableLiveData<String> =
        MutableLiveData(CreateProjectMainFragment.CreateProjectTabs.Overview.name)
}