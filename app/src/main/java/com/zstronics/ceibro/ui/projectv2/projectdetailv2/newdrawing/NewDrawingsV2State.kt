package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class NewDrawingsV2State @Inject constructor(
) : BaseState(), INewDrawingV2.State {

    override var projectName: MutableLiveData<String> = MutableLiveData("")
    override var projectPhoto: MutableLiveData<String> = MutableLiveData("")
    override var projectDescription: MutableLiveData<String> = MutableLiveData("")
    override var isFilterVisible: MutableLiveData<Boolean> = MutableLiveData(false)


}