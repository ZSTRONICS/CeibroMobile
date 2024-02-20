package com.zstronics.ceibro.ui.tasks.v2.newtask.tag

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TagsVM @Inject constructor(
    override val viewState: TagsState,
    private val sessionManager: SessionManager,
) : HiltBaseViewModel<ITags.State>(), ITags.ViewModel {
    val user = sessionManager.getUser().value
    val projects = sessionManager.getProjects().value
    var taskFilters: LocalEvents.ApplyFilterOnTask? = null
    var subTaskFilters: LocalEvents.ApplyFilterOnSubTask? = null



    private val _myGroupData: MutableLiveData<MutableList<CeibroGroupsV2>> = MutableLiveData(mutableListOf())
    val myGroupData: LiveData<MutableList<CeibroGroupsV2>> = _myGroupData

}