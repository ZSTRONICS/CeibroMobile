package com.zstronics.ceibro.ui.tasks.v2.taskdetail.imageviewer

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageViewerVM @Inject constructor(
    override val viewState: ImageViewerState,
) : HiltBaseViewModel<IImageViewer.State>(), IImageViewer.ViewModel {

    private val _images: MutableLiveData<MutableList<TaskFiles>?> = MutableLiveData()
    val images: MutableLiveData<MutableList<TaskFiles>?> = _images

    var imagePosition = 0

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val position = bundle?.getInt("position")
        imagePosition = position ?: 0

        val imagesList = bundle?.getParcelableArray("images")
        val allImages =
            imagesList?.map { it as TaskFiles }
                ?.toMutableList()

        _images.postValue(allImages)
    }

}