package com.zstronics.ceibro.base.clickevents

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import java.util.concurrent.atomic.AtomicInteger

class SingleClickEvent : MutableLiveData<Int>() {
    companion object {
        private val TAG = "SingleClickEvent"
    }

    private val defaultValue: Int = -1
    private val debounceDelay: Long = 600L
    private val mPending = AtomicInteger(defaultValue)
    private var payload: AdaptorPayLoadHolder? = null

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in Int>) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
        }

        // Observe the internal MutableLiveData
        super.observe(owner, Observer { t ->
            if (mPending.get() != defaultValue) {
                observer.onChanged(t)
                Handler(Looper.getMainLooper()).postDelayed(
                    { mPending.set(defaultValue) },
                    debounceDelay
                )
            }
        })
    }

    override fun postValue(value: Int) {
        if (mPending.get() == defaultValue) {
            super.postValue(value)
        }
    }

    @MainThread
    override fun setValue(t: Int) {
        if (mPending.get() == defaultValue) {
            mPending.set(t)
            super.setValue(t)
        }
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        setValue(0)
    }

    fun setPayload(payload: AdaptorPayLoadHolder?) {
        if (mPending.get() == defaultValue) {
            this.payload = payload
        }
    }

    fun setPayload2(payload: AdaptorPayLoadHolder?) {
        this.payload = payload
    }

    fun getPayload(): AdaptorPayLoadHolder? {
        return payload
    }

    data class AdaptorPayLoadHolder(val view: View, val itemData: Any, val position: Int)

}
