package com.zstronics.ceibro.ui.tasks.v2.taskdetail

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.Files
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

@HiltViewModel
class TaskDetailV2VM @Inject constructor(
    override val viewState: TaskDetailV2State,
    private val sessionManager: SessionManager,
) : HiltBaseViewModel<ITaskDetailV2.State>(), ITaskDetailV2.ViewModel {
    val user = sessionManager.getUser().value

    private val _taskDetail: MutableLiveData<CeibroTaskV2> = MutableLiveData()
    val taskDetail: LiveData<CeibroTaskV2> = _taskDetail

    private val _onlyImages: MutableLiveData<ArrayList<Files>> = MutableLiveData(arrayListOf())
    val onlyImages: MutableLiveData<ArrayList<Files>> = _onlyImages

    private val _imagesWithComments: MutableLiveData<ArrayList<Files>> = MutableLiveData(arrayListOf())
    val imagesWithComments: MutableLiveData<ArrayList<Files>> = _imagesWithComments

    private val _documents: MutableLiveData<ArrayList<Files>> = MutableLiveData(arrayListOf())
    val documents: MutableLiveData<ArrayList<Files>> = _documents


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        val taskData: CeibroTaskV2? = bundle?.getParcelable("taskDetail")
        taskData.let { _taskDetail.postValue(it) }

    }


    fun separateFiles(files: List<Files>) {
        val onlyImage: ArrayList<Files> = arrayListOf()
        val imagesWithComment: ArrayList<Files> = arrayListOf()
        val document: ArrayList<Files> = arrayListOf()

        for (item in files) {
            when (item.fileTag) {
                AttachmentTags.Image.tagValue -> {
                    onlyImage.add(item)
                }
                AttachmentTags.ImageWithComment.tagValue -> {
                    imagesWithComment.add(item)
                }
                AttachmentTags.File.tagValue -> {
                    document.add(item)
                }
            }
        }

        _onlyImages.postValue(onlyImage)
        _imagesWithComments.postValue(imagesWithComment)
        _documents.postValue(document)
    }

}