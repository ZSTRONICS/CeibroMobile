package com.zstronics.ceibro.ui.inbox

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.InboxV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.inbox.CeibroInboxV2
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InboxVM @Inject constructor(
    override val viewState: InboxState,
    val inboxV2Dao: InboxV2Dao,
    val taskDao: TaskV2Dao,
) : HiltBaseViewModel<IInbox.State>(), IInbox.ViewModel {

    val _inboxTasks: MutableLiveData<MutableList<CeibroInboxV2>> = MutableLiveData()
    val inboxTasks: MutableLiveData<MutableList<CeibroInboxV2>> = _inboxTasks

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        launch {
//            val allInboxTasks = inboxV2Dao.getAllInboxItems().toMutableList()
//            CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks)
//            _inboxTasks.postValue(allInboxTasks)
        }
    }
}