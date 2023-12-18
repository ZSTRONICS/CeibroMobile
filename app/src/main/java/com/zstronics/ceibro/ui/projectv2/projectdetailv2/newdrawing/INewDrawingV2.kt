package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2

interface INewDrawingV2 {
    interface State : IBase.State {
        var isFilterVisible: MutableLiveData<Boolean>

        var projectName: MutableLiveData<String>
        var projectPhoto: MutableLiveData<String>
        var projectDescription: MutableLiveData<String>
        var floorName: MutableLiveData<String>
        var groupName: MutableLiveData<String>

    }

    interface ViewModel : IBase.ViewModel<State> {

        fun uploadDrawing(
            context: Context,
            floorId: String,
            groupId: String
        )


        fun createGroupByProjectTIDV2(
            projectId: String,
            groupName: String,
            callback: (CeibroGroupsV2) -> Unit
        )

        fun updateGroupByIDV2(
            groupId: String,
            groupName: String,
            callback: (group: CeibroGroupsV2) -> Unit
        )

        fun createFloorByProjectID(
            projectId: String,
            floorName: String,
            callback: (floor: CeibroFloorV2) -> Unit
        )

        fun getGroupsByProjectID(projectId: String)
        fun getFloorsByProjectID(projectId: String)
        fun deleteGroupByID(groupId: String, callback: () -> Unit)

    }
}
