package com.zstronics.ceibro.ui.signup.photo

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class PhotoState @Inject constructor() : BaseState(), IPhoto.State {
    override val isPhotoPicked: MutableLiveData<Boolean> = MutableLiveData(false)
}