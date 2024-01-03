package com.zstronics.ceibro.ui.tasks.v2.fileviewer

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.projects.CeibroDownloadDrawingV2
import com.zstronics.ceibro.data.database.models.tasks.EventFiles
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FileViewerVM @Inject constructor(
    override val viewState: FileViewerState,
) : HiltBaseViewModel<IFileViewer.State>(), IFileViewer.ViewModel {

    private val _fileData: MutableLiveData<CeibroDownloadDrawingV2> =
        MutableLiveData()
    val fileData: MutableLiveData<CeibroDownloadDrawingV2> = _fileData

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val taskFile: TaskFiles? = bundle?.getParcelable("taskFile")
        val downloadedFile: CeibroDownloadDrawingV2? = bundle?.getParcelable("downloadedFile")
        val eventFile: EventFiles? = bundle?.getParcelable("eventFile")
        downloadedFile?.let {
            _fileData.value = it
        }
    }

}