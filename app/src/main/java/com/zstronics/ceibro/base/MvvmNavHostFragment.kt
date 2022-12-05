package com.zstronics.ceibro.base

import android.os.Bundle

import androidx.navigation.fragment.NavHostFragment
import com.zstronics.ceibro.base.extensions.handleExtras
import com.zstronics.ceibro.base.interfaces.CanFetchExtras


/**
 * [NavHostFragment]-based fragment which supports the handling and further propagation of the common [BaseViewModelActivity] events.
 * <br>
 * To be used as a Host Fragment for when you rely on the [BaseViewModelFragment].
 * <br>
 * When you include this fragment in your layout file you should give it the appropriate id ([R.id.nav_host_fragment])
 */
open class MvvmNavHostFragment : NavHostFragment(), CanFetchExtras {

//    @Inject
//    protected lateinit var daggerFragmentInjectionFactory: InjectingFragmentFactory

    override fun fetchExtras(extras: Bundle?) {
        childFragmentManager.fragments.handleExtras(extras)
    }
}