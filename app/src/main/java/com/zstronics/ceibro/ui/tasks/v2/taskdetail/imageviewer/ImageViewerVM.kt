package com.zstronics.ceibro.ui.tasks.v2.taskdetail.imageviewer

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.tasks.LocalTaskDetailFiles
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
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

    private val _detailViewImages: MutableLiveData<MutableList<LocalTaskDetailFiles>?> = MutableLiveData()
    val detailViewImages: MutableLiveData<MutableList<LocalTaskDetailFiles>?> = _detailViewImages


    var imagePosition = 0

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val position = bundle?.getInt("position")
        val fromServerUrl = bundle?.getBoolean("fromServerUrl")
        val fromDetailView = bundle?.getBoolean("fromDetailView")
        imagePosition = position ?: 0

        if (fromDetailView == true) {
            val imagesList = bundle.getParcelableArray("images")
            val allImages =
                imagesList?.map { it as LocalTaskDetailFiles }
                    ?.toMutableList()

            _detailViewImages.postValue(allImages)
        } else {
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

}