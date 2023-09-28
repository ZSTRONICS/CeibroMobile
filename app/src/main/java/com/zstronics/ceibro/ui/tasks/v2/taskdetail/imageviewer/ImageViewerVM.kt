package com.zstronics.ceibro.ui.tasks.v2.taskdetail.imageviewer

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

@HiltViewModel
class ImageViewerVM @Inject constructor(
    override val viewState: ImageViewerState,
) : HiltBaseViewModel<IImageViewer.State>(), IImageViewer.ViewModel {

    private val _images: MutableLiveData<MutableList<TaskFiles>?> = MutableLiveData()
    val images: MutableLiveData<MutableList<TaskFiles>?> = _images

    private val _localImages: MutableLiveData<MutableList<PickedImages>?> = MutableLiveData()
    val localImages: MutableLiveData<MutableList<PickedImages>?> = _localImages

    var imagePosition = 0

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val position = bundle?.getInt("position")
        val fromServerUrl = bundle?.getBoolean("fromServerUrl")
        imagePosition = position ?: 0

        if (fromServerUrl == true) {    //if fromServerUrl is false, then it means we are showing images before upload from local URI
            val imagesList = bundle.getParcelableArray("images")
            val allImages =
                imagesList?.map { it as TaskFiles }
                    ?.toMutableList()

            _images.postValue(allImages)
        } else {
            val imagesList = bundle?.getParcelableArray("images")
            val allImages =
                imagesList?.map { it as PickedImages }
                    ?.toMutableList()

            _localImages.postValue(allImages)
        }
    }

}