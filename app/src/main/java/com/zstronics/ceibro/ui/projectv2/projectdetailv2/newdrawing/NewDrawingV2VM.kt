package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing


import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.FloorsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroFloorV2
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.drawing.UploadDrawingV2FileMetaData
import com.zstronics.ceibro.data.repos.projects.floor.CreateNewFloorRequest
import com.zstronics.ceibro.data.repos.projects.group.CreateNewGroupV2Request
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class NewDrawingV2VM @Inject constructor(
    override val viewState: NewDrawingsV2State,
    private val projectRepository: IProjectRepository,
    private val sessionManager: SessionManager,
    private val projectDao: ProjectsV2Dao,
    private val groupsV2Dao: GroupsV2Dao,
    private val floorsV2Dao: FloorsV2Dao,
) : HiltBaseViewModel<INewDrawingV2.State>(), INewDrawingV2.ViewModel {
    val user = sessionManager.getUser().value

    val _groupList: MutableLiveData<MutableList<CeibroGroupsV2>> = MutableLiveData(arrayListOf())
    val groupList: MutableLiveData<MutableList<CeibroGroupsV2>> = _groupList

    val _floorList: MutableLiveData<MutableList<CeibroFloorV2>> = MutableLiveData(arrayListOf())
    val floorList: MutableLiveData<MutableList<CeibroFloorV2>> = _floorList


    var pdfFilePath = MutableLiveData<String>("")
    var pdfFileName = ""
    var projectId = MutableLiveData<String>("")
    var message = MutableLiveData<String?>("")

    var selectedGroup: CeibroGroupsV2? = null
    var selectedFloor: CeibroFloorV2? = null
    var floor = MutableLiveData<String?>("")


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        pdfFilePath.value = bundle?.getString("pdfFilePath").toString()
        pdfFileName = bundle?.getString("pdfFileName").toString()
        projectId.value = bundle?.getString("projectId").toString()

        getFloorsByProjectID(projectId.value.toString())
        getGroupsByProjectID(projectId.value.toString())
    }


    override fun getGroupsByProjectID(projectId: String) {
        launch {
            loading(true)
            val groupsList = groupsV2Dao.getAllProjectGroups(projectId)
            if (groupsList.isNotEmpty()) {
                _groupList.value = groupsList.toMutableList()
                loading(false, "")
            } else {
                when (val response = projectRepository.getGroupsByProjectId(projectId)) {

                    is ApiResponse.Success -> {
                        groupsV2Dao.insertMultipleGroups(response.data.groups)
                        _groupList.value = response.data.groups.toMutableList()
                        loading(false, "")
                    }

                    is ApiResponse.Error -> {
                        loading(false, response.error.message)
                    }
                }
            }
        }
    }

    override fun getFloorsByProjectID(projectId: String) {
        launch {
            loading(true)
            val floorList = floorsV2Dao.getAllProjectFloors(projectId)
            if (floorList.isNotEmpty()) {
                _floorList.value = floorList.toMutableList()
                loading(false, "")
            } else {
                when (val response = projectRepository.getFloorsByProjectTid(projectId)) {
                    is ApiResponse.Success -> {
                        floorsV2Dao.insertMultipleFloors(response.data.floors)
                        _floorList.value = response.data.floors.toMutableList()

                        loading(false, "")
                    }

                    is ApiResponse.Error -> {
                        loading(false, response.error.message)
                    }
                }
            }
        }
    }

    override fun uploadDrawing(
        context: Context,
        floorId: String,
        groupId: String
    ) {

        val filePath = pdfFilePath.value ?: ""
        val projectId = projectId.value.toString()

        val file = File(filePath)
        val fileList: MutableList<File> = mutableListOf()
        fileList.add(file)

        val projectID = projectId.toRequestBody("text/plain".toMediaTypeOrNull())
        val floorID = floorId.toRequestBody("text/plain".toMediaTypeOrNull())
        val groupID = groupId.toRequestBody("text/plain".toMediaTypeOrNull())
        val uploaderLocalId = System.currentTimeMillis().toString()

        val fileParts = fileList.map { file1 ->
            val reqFile =
                file1.asRequestBody(("image/" + file1.extension).toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files", pdfFileName, reqFile)
        }

        val metaData = fileList.map { file1 ->
            val tag = AttachmentTags.Drawing.tagValue

            UploadDrawingV2FileMetaData(
                fileName = pdfFileName,
                tag = tag,
                uploaderLocalFilePath = filePath,
                uploaderLocalId = uploaderLocalId
            )
        }
        val metadataString = Gson().toJson(metaData)
        val metadataString2 = Gson().toJson(metadataString)

        val metadataString2RequestBody =
            metadataString2.toRequestBody("text/plain".toMediaTypeOrNull())


        launch {
            loading(true)
            when (val response = projectRepository.uploadDrawing(
                projectId = projectID,
                floorId = floorID,
                groupId = groupID,
                metadata = metadataString2RequestBody,
                files = fileParts
            )) {
                is ApiResponse.Success -> {
                    loading(false, response.data.message)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }


    override fun createGroupByProjectTIDV2(
        projectId: String,
        groupName: String,
        callback: (CeibroGroupsV2) -> Unit
    ) {
        val request = CreateNewGroupV2Request(groupName)
        launch {
            loading(true, "")
            when (val response = projectRepository.createGroupV2(projectId, request)) {
                is ApiResponse.Success -> {
                    groupsV2Dao.insertGroup(response.data.group)
                    _groupList.value?.add(response.data.group)
                    callback.invoke(response.data.group)
                    loading(false, "")
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }

    override fun updateGroupByIDV2(
        groupId: String,
        groupName: String,
        callback: (group: CeibroGroupsV2) -> Unit
    ) {
        val request = CreateNewGroupV2Request(groupName)
        launch {
            loading(true)
            when (val response = projectRepository.updateGroupByIdV2(groupId, request)) {

                is ApiResponse.Success -> {
                    groupsV2Dao.insertGroup(response.data.group)
                    val group = response.data.group
                    val list = groupList.value
                    list?.forEachIndexed { index, ceibroGroupsV2 ->
                        if (ceibroGroupsV2._id == group._id) {
                            ceibroGroupsV2.groupName = group.groupName
                        }
                    }

                    list?.let {
                        _groupList.value = it
                    }
                    loading(false, "")
                    callback.invoke(group)
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }


    override fun createFloorByProjectID(
        projectId: String,
        floorName: String,
        callback: (floor: CeibroFloorV2) -> Unit
    ) {
        val request = CreateNewFloorRequest(floorName)
        launch {
            loading(true)
            when (val response = projectRepository.createFloorV2(projectId, request)) {

                is ApiResponse.Success -> {
                    val floor = response.data.floor
                    floor?.let {
                        floorsV2Dao.insertFloor(it)
                        _floorList.value?.add(it)
                        callback.invoke(floor)
                    }

                    loading(false, "")
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }


    override fun deleteGroupByID(groupId: String, callback: () -> Unit) {
        launch {
            loading(true)
            when (val response = projectRepository.deleteGroupByIdV2(groupId)) {

                is ApiResponse.Success -> {
                    groupsV2Dao.deleteGroupById(groupId)
                    _groupList.value?.let { currentList ->
                        val iterator = currentList.iterator()
                        while (iterator.hasNext()) {
                            val item = iterator.next()
                            if (groupId == item._id) {
                                iterator.remove()
                            }
                        }
                        _groupList.value = currentList
                    }
                    loading(false, response.data.message)
                    callback.invoke()
                }

                is ApiResponse.Error -> {
                    loading(false, response.error.message)
                }
            }
        }
    }


}
