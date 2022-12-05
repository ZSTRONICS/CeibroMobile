package com.zstronics.ceibro.base.viewmodel


import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.zstronics.ceibro.base.interfaces.CanFetchExtras

abstract class BaseViewModel : ViewModel(), CanFetchExtras, LifecycleObserver {
    private var isFirstTimeUiCreate = true

    /**
     * called after fragment / activity is created with input bundle arguments
     *
     * @param bundle argument data
     */
    @CallSuper
    open fun onCreate(bundle: Bundle?) {
        if (isFirstTimeUiCreate) {
//            mUserLiveData.value = SessionManager.user
            // this.bundle = bundle
            onFirsTimeUiCreate(bundle)
            isFirstTimeUiCreate = false
        }
    }


    /**
     * Called when UI create for first time only, since activity / fragment might be rotated,
     * we don't need to re-init data, because view model will survive, data aren't destroyed
     *
     * @param bundle
     */
    open fun onFirsTimeUiCreate(bundle: Bundle?) {}
    override fun fetchExtras(extras: Bundle?) {

    }
}