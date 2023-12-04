package com.zstronics.ceibro.ui.locationv2

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationsV2VM @Inject constructor(
    override val viewState: LocationsV2State,
) : HiltBaseViewModel<ILocations.State>(), ILocations.ViewModel {
}