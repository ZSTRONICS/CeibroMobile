package com.zstronics.ceibro.ui.locationv2.drawing

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DrawingsV2VM @Inject constructor(
    override val viewState: DrawingsV2State,
) : HiltBaseViewModel<IDrawingV2.State>(), IDrawingV2.ViewModel {
}