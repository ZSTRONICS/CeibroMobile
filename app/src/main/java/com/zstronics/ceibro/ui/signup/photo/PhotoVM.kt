package com.zstronics.ceibro.ui.signup.photo

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PhotoVM @Inject constructor(
    override val viewState: PhotoState,
) : HiltBaseViewModel<IPhoto.State>(), IPhoto.ViewModel {
}