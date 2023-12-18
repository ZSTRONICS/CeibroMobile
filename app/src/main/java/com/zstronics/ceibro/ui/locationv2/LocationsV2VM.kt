package com.zstronics.ceibro.ui.locationv2

import android.os.Bundle
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.CookiesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationsV2VM @Inject constructor(
    override val viewState: LocationsV2State,
) : HiltBaseViewModel<ILocationsV2.State>(), ILocationsV2.ViewModel {

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        println("CookiesManager.drawingFileForLocation11: ${CookiesManager.drawingFileForLocation.value}")
    }

}