package com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawing

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DrawingsV2VM @Inject constructor(
    override val viewState: DrawingsV2State,
    private val projectRepository: IProjectRepository,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
    private val groupsV2Dao: GroupsV2Dao,
) : HiltBaseViewModel<IDrawingV2.State>(), IDrawingV2.ViewModel {

    private val _projectData: MutableLiveData<CeibroProjectV2> = MutableLiveData()
    val projectData: LiveData<CeibroProjectV2> = _projectData

    private val _groupData: MutableLiveData<MutableList<CeibroGroupsV2>> = MutableLiveData(
        mutableListOf()
    )
    val groupData: LiveData<MutableList<CeibroGroupsV2>> = _groupData

    private val _otherGroupsData: MutableLiveData<MutableList<CeibroGroupsV2>> = MutableLiveData(
        mutableListOf()
    )
    val otherGroupsData: LiveData<MutableList<CeibroGroupsV2>> = _otherGroupsData

    private val _favoriteGroups: MutableLiveData<MutableList<CeibroGroupsV2>> = MutableLiveData(
        mutableListOf()
    )
    val favoriteGroups: LiveData<MutableList<CeibroGroupsV2>> = _favoriteGroups
    var originalGroups: MutableLiveData<MutableList<CeibroGroupsV2>> =
        MutableLiveData(mutableListOf())


    var originalAllGroups: MutableList<CeibroGroupsV2> = mutableListOf()
    var originalFavouriteGroups: MutableList<CeibroGroupsV2> = mutableListOf()
    var originalOtherGroups: MutableList<CeibroGroupsV2> = mutableListOf()
    var orignalMyGroups: MutableList<CeibroGroupsV2> = mutableListOf()
    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        val project = CookiesManager.projectDataForDetails
        project?.let {
            _projectData.postValue(it)
            getGroupsByProjectID(it._id)
        }
    }

    fun getGroupsByProjectID(projectId: String) {
        launch {
//            loading(true)
            val groupsList = groupsV2Dao.getAllProjectGroups(projectId)
            originalAllGroups = groupsList.toMutableList()
            originalGroups.value = groupsList.toMutableList()
            if (groupsList.isNotEmpty()) {
                val favoriteGroups = groupsList.filter { it.isFavoriteByMe } ?: listOf()
                originalFavouriteGroups=favoriteGroups.toMutableList()

                val creatorGroups =
                    groupsList.filter { (!it.isFavoriteByMe) && (it.isCreator) } ?: listOf()

                val otherGroups =
                    groupsList.filter { (!it.isFavoriteByMe) && (!it.isCreator) } ?: listOf()
                orignalMyGroups=otherGroups.toMutableList()

                originalOtherGroups=otherGroups.toMutableList()


                _favoriteGroups.value = favoriteGroups.toMutableList()
                _groupData.value = creatorGroups.toMutableList()
                _otherGroupsData.value = otherGroups.toMutableList()
//                loading(false, "")
                viewState.isVisible.value = false
            } else {
                viewState.isVisible.value = true
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


    fun publicOrPrivateGroup(group: CeibroGroupsV2) {
        launch {
            loading(true)
            when (val response = projectRepository.makeGroupPublicOrPrivate(
                state = !group.publicGroup,
                groupId = group._id
            )) {

                is ApiResponse.Success -> {
                    groupsV2Dao.insertGroup(response.data.group)
                    projectData.value?.let { getGroupsByProjectID(it._id) }
                    if (response.data.group.publicGroup) {
                        loading(false, "Group is now public")
                    } else {
                        loading(false, "Group is now private")
                    }
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }


    fun filterAllProjects(search: String) {
        if (search.isEmpty()) {
            if (originalAllGroups.isNotEmpty()) {
                _groupData.postValue(originalAllGroups)
            }
            return
        }
        val filtered = originalAllGroups.filter {
            (it.groupName.isNotEmpty() && it.groupName.contains(search, true)) ||
                    (("${it.creator.firstName} ${it.creator.surName}").isNotEmpty() && ("${it.creator.firstName} ${it.creator.surName}").lowercase()
                        .contains(search, true)) ||
                    (it.drawings.isNotEmpty() && isDrawingNameExist(it.drawings, search))
        }

        if (filtered.isNotEmpty())
            _groupData.postValue(filtered.toMutableList())
        else
            _groupData.postValue(mutableListOf())
    }

    fun filterFavouriteGroups(search: String) {
        if (search.isEmpty()) {
            if (originalFavouriteGroups.isNotEmpty()) {
                _favoriteGroups.postValue(originalFavouriteGroups)
            }
            return
        }
        val filtered = originalFavouriteGroups.filter {
            (it.groupName.isNotEmpty() && it.groupName.contains(search, true)) ||
                    (("${it.creator.firstName} ${it.creator.surName}").isNotEmpty() && ("${it.creator.firstName} ${it.creator.surName}").lowercase()
                        .contains(search, true)) ||
                    (it.drawings.isNotEmpty() && isDrawingNameExist(it.drawings, search))
        }

        if (filtered.isNotEmpty())
            _favoriteGroups.postValue(filtered.toMutableList())
        else
            _favoriteGroups.postValue(mutableListOf())
    }
    fun filterOtherGroups(search: String) {
        if (search.isEmpty()) {
            if (originalOtherGroups.isNotEmpty()) {
                _otherGroupsData.postValue(originalOtherGroups)
            }
            return
        }
        val filtered = originalOtherGroups.filter {
            (it.groupName.isNotEmpty() && it.groupName.contains(search, true)) ||
                    (("${it.creator.firstName} ${it.creator.surName}").isNotEmpty() && ("${it.creator.firstName} ${it.creator.surName}").lowercase()
                        .contains(search, true)) ||
                    (it.drawings.isNotEmpty() && isDrawingNameExist(it.drawings, search))
        }

        if (filtered.isNotEmpty())
            _otherGroupsData.postValue(filtered.toMutableList())
        else
            _otherGroupsData.postValue(mutableListOf())
    }


    private fun isDrawingNameExist(drawings: List<DrawingV2>, search: String): Boolean {

        val found = drawings.filter {
            it.fileName.contains(search, true)
        }

        return (found.isNotEmpty())
    }


}