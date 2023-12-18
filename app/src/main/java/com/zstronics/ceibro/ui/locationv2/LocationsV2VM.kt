package com.zstronics.ceibro.ui.locationv2

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationsV2VM @Inject constructor(
    override val viewState: LocationsV2State,
) : HiltBaseViewModel<ILocationsV2.State>(), ILocationsV2.ViewModel {


    val _drawingFile: MutableLiveData<DrawingV2> =
        MutableLiveData()
    val drawingFile: LiveData<DrawingV2> = _drawingFile

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

    }

}