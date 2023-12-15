package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase

interface INewDrawingV2 {
    interface State : IBase.State {
        var isFilterVisible: MutableLiveData<Boolean>

        var projectName: MutableLiveData<String>
        var projectPhoto: MutableLiveData<String>
        var projectDescription: MutableLiveData<String>

    }

    interface ViewModel : IBase.ViewModel<State> {

        fun createGroupByProjectTIDV2(projectId: String, groupName: String)
        fun updateGroupByIDV2(groupId: String, groupName: String)
        fun createFloorByProjectTID(projectId: String, floorName: String)
        fun getGroupsByProjectTID(projectId: String)
        fun getFloorsByProjectTID(projectId: String)
        fun deleteGroupByID(projectId: String)

    }
}
