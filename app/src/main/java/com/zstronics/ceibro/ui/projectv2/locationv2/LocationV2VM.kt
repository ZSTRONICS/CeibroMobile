package com.zstronics.ceibro.ui.projectv2.locationv2


import android.content.Context
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationV2VM @Inject constructor(
    override val viewState: LocationStateV2,
    private val remoteTask: TaskRemoteDataSource,
    private val sessionManager: SessionManager,
    private val taskDao: TaskV2Dao
) : HiltBaseViewModel<ILocationV2.State>(), ILocationV2.ViewModel {
    override fun getProjectName(context: Context) {

    }


}
