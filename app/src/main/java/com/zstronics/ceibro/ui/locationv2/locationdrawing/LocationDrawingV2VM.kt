package com.zstronics.ceibro.ui.locationv2.locationdrawing

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationDrawingV2VM @Inject constructor(
    override val viewState: LocationDrawingV2State,
    private val projectRepository: IProjectRepository,
    private val groupsV2Dao: GroupsV2Dao,
) : HiltBaseViewModel<ILocationDrawingV2.State>(), ILocationDrawingV2.ViewModel {

    private val _projectData: MutableLiveData<CeibroProjectV2> = MutableLiveData()
    val projectData: LiveData<CeibroProjectV2> = _projectData

    private val _groupData: MutableLiveData<MutableList<CeibroGroupsV2>> = MutableLiveData(
        mutableListOf()
    )
    val groupData: LiveData<MutableList<CeibroGroupsV2>> = _groupData

    private val _favoriteGroups: MutableLiveData<MutableList<CeibroGroupsV2>> = MutableLiveData(
        mutableListOf()
    )
    val favoriteGroups: LiveData<MutableList<CeibroGroupsV2>> = _favoriteGroups
    var originalGroups: MutableLiveData<MutableList<CeibroGroupsV2>> = MutableLiveData(mutableListOf())
    var favoriteGroupsOnceSet = false
    var allGroupsOnceSet = false


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        val project = CookiesManager.locationProjectDataForDetails
        project?.let {
            _projectData.postValue(it)
            getGroupsByProjectID(it._id)
        }
    }

    fun getGroupsByProjectID(projectId: String) {
        launch {
//            loading(true)
            val groupsList = groupsV2Dao.getAllProjectGroups(projectId)
            originalGroups.value = groupsList.toMutableList()
            if (groupsList.isNotEmpty()) {
                val favorites = groupsList.filter { it.isFavoriteByMe } ?: listOf()
                _favoriteGroups.value = favorites.toMutableList()
                _groupData.value = groupsList.toMutableList()
//                loading(false, "")
            } else {
//                when (val response = projectRepository.getGroupsByProjectId(projectId)) {
//
//                    is ApiResponse.Success -> {
//                        groupsV2Dao.insertMultipleGroups(response.data.groups)
//                        _groupData.value = response.data.groups.toMutableList()
//                        loading(false, "")
//                    }
//
//                    is ApiResponse.Error -> {
//                        loading(false, response.error.message)
//                    }
//                }
            }
        }
    }

}