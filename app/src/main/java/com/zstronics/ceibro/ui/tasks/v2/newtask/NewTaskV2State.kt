package com.zstronics.ceibro.ui.tasks.v2.newtask

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import javax.inject.Inject

class NewTaskV2State @Inject constructor() : BaseState(), INewTaskV2.State {
    override var dueDate: MutableLiveData<String> = MutableLiveData("")
    override val isDoneReqAllowed: MutableLiveData<Boolean> = MutableLiveData(false)
    override val isAttachLayoutOpen: MutableLiveData<Boolean> = MutableLiveData(false)
    override var taskTitle: MutableLiveData<String> = MutableLiveData("")
    override var selectedTopic: MutableLiveData<TopicsResponse.TopicData> = MutableLiveData()
    override var selectedProject: MutableLiveData<CeibroProjectV2> = MutableLiveData()
    override var selectedContacts: MutableLiveData<MutableList<AllCeibroConnections.CeibroConnection>> = MutableLiveData()
    override var selfAssigned: MutableLiveData<Boolean> = MutableLiveData(false)
    override var assignToText: MutableLiveData<String> = MutableLiveData("")
    override var projectText: MutableLiveData<String> = MutableLiveData("")
    override var description: MutableLiveData<String> = MutableLiveData("")
}