@file:JvmName("BundleUtils")

package com.zstronics.ceibro.base.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.zstronics.ceibro.base.interfaces.CanFetchExtras

/**
 * Adds the contents of the specified [Bundle] into
 * the current [Bundle].
 *
 * @return the current [Bundle] with appended content
 */
operator fun Bundle.plus(other: Bundle): Bundle {
    return this.also { it.putAll(other) }
}

/**
 * Applies the [handleExtras] method to each of the [Fragment]s
 * present in the specified [Collection].
 *
 * @param extras the [Bundle] of arguments
 */
fun Collection<Fragment>.handleExtras(extras: Bundle?) {
    this.forEach { it.handleExtras(extras) }
}

/**
 * Propagates the extras for further handling to the specified [Fragment]
 * only if it (the specified [Fragment]) can handle the specified [Bundle] of arguments
 * (if it (the specified [Fragment]) implements the [CanFetchExtras] interface)
 *
 * @param extras the [Bundle] of arguments
 */
fun Fragment.handleExtras(extras: Bundle?) {
    if (this is CanFetchExtras) {
        this.fetchExtras(extras)
    }
}
