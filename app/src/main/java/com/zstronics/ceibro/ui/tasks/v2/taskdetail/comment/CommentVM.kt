package com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

@HiltViewModel
class CommentVM @Inject constructor(
    override val viewState: CommentState,
    val sessionManager: SessionManager,
) : HiltBaseViewModel<IComment.State>(), IComment.ViewModel {
    val user = sessionManager.getUser().value
    val listOfImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val onlyImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())
    val imagesWithComments: MutableLiveData<ArrayList<PickedImages>> =
        MutableLiveData(arrayListOf())
    val documents: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData(arrayListOf())




}