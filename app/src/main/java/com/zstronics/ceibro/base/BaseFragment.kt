package com.zstronics.ceibro.base

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.fragment.app.Fragment
import com.zstronics.ceibro.base.interfaces.CanHandleBackPressed


abstract class BaseFragment : Fragment(), CanHandleBackPressed {

    protected val activityLauncher: BetterActivityResult<Intent, ActivityResult> =
        BetterActivityResult.registerActivityForResult(this)

    /**
     * Finishes the host [android.app.Activity] (see: [android.app.Activity.finish]).
     */
    protected fun finishActivity() {
        requireActivity().finish()
    }

    /**
     * Finishes the host [android.app.Activity] affinity (see: [android.app.Activity.finishAffinity]).
     */
    protected fun finishActivityAffinity() {
        requireActivity().finishAffinity()
    }

    /**
     * Get's called when it's the right time for you to initialize the UI elements.
     *
     * @param savedInstanceState the state bundle brought from the [Fragment.onViewCreated]
     */
    protected open fun init(savedInstanceState: Bundle?) {
        //
    }


    /**
     * Gets called right after the UI initialization.
     */
    protected open fun postInit() {
        //
    }

    /**
     * Gets called right after the UI executePendingBindings.
     */
    protected open fun postExecutePendingBindings(savedInstanceState: Bundle?) {
        //
    }

    /**
     * Gets called right before the UI initialization.
     */
    protected open fun preInit() {
        //
    }

    /**
     * Adding BackButtonDispatcher callback to activity
     * if you want to control back press functionality you must call this and implement [onBackPressed]
     */
    fun setBackButtonDispatcher() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onBackPressed(): Boolean = false
    abstract fun getToolBarTitle(): String?
    abstract fun toolBarVisibility(): Boolean?
    abstract fun setDisplayHomeAsUpEnabled(): Boolean?
    abstract fun setHomeAsUpIndicator(): Int?
    abstract fun hasOptionMenu(): Boolean?
}