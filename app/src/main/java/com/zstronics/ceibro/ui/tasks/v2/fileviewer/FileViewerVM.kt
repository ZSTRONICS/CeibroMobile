package com.zstronics.ceibro.ui.tasks.v2.fileviewer

import android.os.Bundle
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.tasks.EventFiles
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FileViewerVM @Inject constructor(
    override val viewState: FileViewerState,
) : HiltBaseViewModel<IFileViewer.State>(), IFileViewer.ViewModel {

    //    val _fileData: MutableLiveData<TaskFiles> = MutableLiveData()
//    val fileData: LiveData<TaskFiles> = _fileData
//
//    var fileData1: TaskFiles? = null
    var fileUrl: String = ""

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val taskFile: TaskFiles? = bundle?.getParcelable("taskFile")
        val eventFile: EventFiles? = bundle?.getParcelable("eventFile")
        taskFile?.let {
//            _fileData.postValue(it)
//            fileData1 = it
            fileUrl = it.fileUrl ?: ""
        }
        eventFile?.let {
//            _fileData.postValue(it)
//            fileData1 = it
            fileUrl = it.fileUrl ?: ""
        }
    }

}