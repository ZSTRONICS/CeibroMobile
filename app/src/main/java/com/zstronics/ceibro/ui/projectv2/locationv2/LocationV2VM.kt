package com.zstronics.ceibro.ui.projectv2.locationv2


import android.content.Context
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationV2VM @Inject constructor(
    override val viewState: LocationStateV2,
) : HiltBaseViewModel<ILocationV2.State>(), ILocationV2.ViewModel {
    override fun getProjectName(context: Context) {

    }


}
