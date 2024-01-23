package com.zstronics.ceibro.ui.inbox

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InboxVM @Inject constructor(
    override val viewState: InboxState,
) : HiltBaseViewModel<IInbox.State>(), IInbox.ViewModel {
    private val _newTasks: MutableLiveData<MutableList<CeibroTaskV2>> = MutableLiveData()
    val newTasks: MutableLiveData<MutableList<CeibroTaskV2>> = _newTasks

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        _newTasks.value= arrayListOf()
    }
}