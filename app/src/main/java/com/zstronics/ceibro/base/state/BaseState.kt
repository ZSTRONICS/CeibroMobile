package com.zstronics.ceibro.base.state

import androidx.databinding.BaseObservable
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.interfaces.IBase


abstract class BaseState : BaseObservable(), IBase.State {
    override var toolbarTitle: MutableLiveData<String> = MutableLiveData("")

    override var toolsBarVisibility: MutableLiveData<Boolean> = MutableLiveData(false)
    override var uiState: MutableLiveData<UIState> = MutableLiveData()

    override fun onStart() {
    }

    override fun onStop() {
    }

    override fun destroy() {
    }

    override fun onCreate() {
    }

    override fun resume() {
    }

    override fun pause() {
    }

}