package com.zstronics.ceibro.base.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zstronics.ceibro.utils.LocaleChanger

abstract class BaseActivity : AppCompatActivity() {

    /**
     * Gets called right before the UI initialization.
     */
    protected open fun preInit(savedInstanceState: Bundle?) {
        //
    }

    /**
     * Get's called when it's the right time for you to initialize the UI elements.
     *
     * @param savedInstanceState state bundle brought from the [android.app.Activity.onCreate]
     */
    protected open fun init(savedInstanceState: Bundle?) {
        //
    }

    /**
     * Gets called right after the UI executePendingBindings.
     */
    protected open fun postExecutePendingBindings(savedInstanceState: Bundle?) {
        //
    }

    /**
     * Gets called right after the UI initialization.
     */
    protected open fun postInit() {
        //
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleChanger.wrapContext(base))
    }
}