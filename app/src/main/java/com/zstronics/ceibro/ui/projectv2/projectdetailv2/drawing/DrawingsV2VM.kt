package com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawing

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DrawingsV2VM @Inject constructor(
    override val viewState: DrawingsV2State,
) : HiltBaseViewModel<IDrawingV2.State>(), IDrawingV2.ViewModel {

    private val _projectData: MutableLiveData<CeibroProjectV2> = MutableLiveData()
    val projectData: LiveData<CeibroProjectV2> = _projectData
    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)

        val project = CookiesManager.projectDataForDetails
        project?.let {
            _projectData.postValue(it)
        }
    }
}