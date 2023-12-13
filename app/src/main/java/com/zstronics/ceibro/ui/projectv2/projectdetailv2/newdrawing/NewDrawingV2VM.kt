package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing


import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewDrawingV2VM @Inject constructor(
    override val viewState: NewDrawingsV2State,
    private val projectRepository: IProjectRepository,
    private val sessionManager: SessionManager,
    private val projectDao: ProjectsV2Dao,
) : HiltBaseViewModel<INewDrawingV2.State>(), INewDrawingV2.ViewModel {
    val user = sessionManager.getUser().value
    var pdfFilePath = MutableLiveData<String>("")


    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        pdfFilePath.value = bundle?.getString("pdfFilePath").toString()
    }

    override fun getProjectName(context: Context) {

    }

    override fun addNewProject(context: Context, callBack: (isSuccess: Boolean) -> Unit) {

    }


}
