package com.zstronics.ceibro.ui.chat.groupinfo

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import com.zstronics.ceibro.data.repos.chat.room.Project
import javax.inject.Inject

class GroupInfoState @Inject constructor() : BaseState(), IGroupInfo.State {
    override val chatTitle: MutableLiveData<String> = MutableLiveData()
    override val project: MutableLiveData<Project> = MutableLiveData(Project("", ""))
    override val isGroupChat: MutableLiveData<Boolean> = MutableLiveData()
}