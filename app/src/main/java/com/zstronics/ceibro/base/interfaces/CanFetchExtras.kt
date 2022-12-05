package com.zstronics.ceibro.base.interfaces

import android.os.Bundle

/**
 * A marker interface used to indicate the support for the handling of the arguments passed around in a [Bundle].
 */
interface CanFetchExtras {

    /**
     * Processes (extracts) the specified [Bundle] of arguments.
     *
     * @param extras a bundle of arguments
     */
    fun fetchExtras(extras: Bundle?)

}