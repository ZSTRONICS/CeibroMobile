package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing


import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.floor.CreateNewFloorRequest
import com.zstronics.ceibro.data.repos.projects.group.CreateNewGroupV2Request
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewDrawingV2VM @Inject constructor(
    override val viewState: NewDrawingsV2State,
    private val projectRepository: IProjectRepository,
    private val sessionManager: SessionManager,
    private val projectDao: ProjectsV2Dao,
) : HiltBaseViewModel<INewDrawingV2.State>(), INewDrawingV2.ViewModel {
    val user = sessionManager.getUser().value


    var pdfFilePath = MutableLiveData<String>("")
    var projectId = MutableLiveData<String>("")


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        pdfFilePath.value = bundle?.getString("pdfFilePath").toString()
        projectId.value = bundle?.getString("projectId").toString()
    }


    override fun getGroupsByProjectTid(projectId: String) {
        launch {
            when (val response = projectRepository.getGroupsByProjectTid(projectId)) {

                is ApiResponse.Success -> {

                    val groups = response.data.groups
                    groups.size
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }


    override fun createGroupByProjectTid(
        projectId: String,
        groupName: String
    ) {
        val request = CreateNewGroupV2Request(groupName)
        launch {
            when (val response = projectRepository.createGroupV2(projectId, request)) {

                is ApiResponse.Success -> {

                    val floor = response.data.group
                    floor?.projectId
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }


    override fun createFloorByProjectTid(
        projectId: String,
        floorName: String
    ) {
        val request = CreateNewFloorRequest(floorName)
        launch {
            when (val response = projectRepository.createFloorV2(projectId, request)) {

                is ApiResponse.Success -> {

                    val floor = response.data.floor
                    floor?.projectId
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }


    override fun getFloorsByProjectTid(projectId: String) {
        launch {
            when (val response = projectRepository.getFloorsByProjectTid(projectId)) {

                is ApiResponse.Success -> {

                    val floor = response.data.floors

                    if (!floor.isNullOrEmpty()){
                        floor.size
                    }
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }


    override fun addNewProject(context: Context, callBack: (isSuccess: Boolean) -> Unit) {

    }


}
