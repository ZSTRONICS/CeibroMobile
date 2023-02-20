package com.zstronics.ceibro.ui.photoeditor

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PhotoEditorVM @Inject constructor(
    override val viewState: PhotoEditorState,
    val sessionManager: SessionManager
) : HiltBaseViewModel<IPhotoEditor.State>(), IPhotoEditor.ViewModel {
    private val _photoUri: MutableLiveData<Uri?> = MutableLiveData()
    val photoUri = _photoUri
    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val imageUri = bundle?.getParcelable<Uri>("imageUri")
        _photoUri.postValue(imageUri)
    }
}