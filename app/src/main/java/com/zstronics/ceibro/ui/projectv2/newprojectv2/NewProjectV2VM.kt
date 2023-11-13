package com.zstronics.ceibro.ui.projectv2.newprojectv2


import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewProjectV2VM @Inject constructor(
    override val viewState: NewProjectStateV2,
) : HiltBaseViewModel<INewProjectV2.State>(), INewProjectV2.ViewModel {


    override fun getProjectName(context: Context) {

    }

    override fun addNewProject() {

    }


}
