package com.zstronics.ceibro.ui.chat.media

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MediaVM @Inject constructor(
    override val viewState: MediaState,
) : HiltBaseViewModel<IMedia.State>(), IMedia.ViewModel {
}