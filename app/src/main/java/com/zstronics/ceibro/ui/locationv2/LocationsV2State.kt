package com.zstronics.ceibro.ui.locationv2

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class LocationsV2State @Inject constructor(
) : BaseState(), ILocationsV2.State {

    override var isFilterVisible: MutableLiveData<Boolean> = MutableLiveData(false)

    override var isToNewClicked: MutableLiveData<Boolean> = MutableLiveData(true)
    override var isToOngoingClicked: MutableLiveData<Boolean> = MutableLiveData(true)
    override var isToDoneClicked: MutableLiveData<Boolean> = MutableLiveData(true)

    override var isFromUnreadClicked: MutableLiveData<Boolean> = MutableLiveData(true)
    override var isFromOngoingClicked: MutableLiveData<Boolean> = MutableLiveData(true)
    override var isFromDoneClicked: MutableLiveData<Boolean> = MutableLiveData(true)

    override var isHiddenOngoingClicked: MutableLiveData<Boolean> = MutableLiveData(true)
    override var isHiddenDoneClicked: MutableLiveData<Boolean> = MutableLiveData(true)
    override var isHiddenCancelled: MutableLiveData<Boolean> = MutableLiveData(true)
}