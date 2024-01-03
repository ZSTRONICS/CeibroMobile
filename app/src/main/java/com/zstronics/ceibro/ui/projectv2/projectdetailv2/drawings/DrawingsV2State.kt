package com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawings

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class DrawingsV2State @Inject constructor(
) : BaseState(), IDrawingV2.State {

    override var isFilterVisible: MutableLiveData<Boolean> = MutableLiveData(false)
    override var isVisible: MutableLiveData<Boolean> = MutableLiveData(false)


}