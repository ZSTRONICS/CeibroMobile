package com.zstronics.ceibro.ui.tasks.v2.newtask

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse

interface INewTaskV2 {
    interface State : IBase.State {
        var dueDate: MutableLiveData<String>
        val isDoneReqAllowed: MutableLiveData<Boolean>
        val isAttachLayoutOpen: MutableLiveData<Boolean>
        var taskTitle: MutableLiveData<String>
        var selectedTopic: MutableLiveData<TopicsResponse.TopicData>
        var selectedProject: MutableLiveData<CeibroProjectV2>
        var selectedContacts: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>>
        var selfAssigned: MutableLiveData<Boolean>
        var assignToText: MutableLiveData<String>
        var projectText: MutableLiveData<String>
        var description: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
    }
}