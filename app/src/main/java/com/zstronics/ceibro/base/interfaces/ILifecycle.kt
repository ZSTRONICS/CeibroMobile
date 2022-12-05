package com.zstronics.ceibro.base.interfaces

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

interface ILifecycle : LifecycleObserver {
    fun onCreate()
    fun onStart()
    fun onStop()
    fun onDestroy()
    fun onResume()
    fun onPause()
    fun registerLifecycleOwner(owner: LifecycleOwner?)
    fun unregisterLifecycleOwner(owner: LifecycleOwner?)
}