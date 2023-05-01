package com.zstronics.ceibro.ui.pixiImagePicker

import com.zstronics.ceibro.base.validator.IValidator
import com.zstronics.ceibro.base.validator.Validator
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.resourses.IResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PixImagePickerVM @Inject constructor(
    override val viewState: PixImagePickerState,
    override var validator: Validator?,
    private val sessionManager: SessionManager,
    private val resProvider: IResourceProvider,
    private val dashboardRepository: IDashboardRepository
) : HiltBaseViewModel<IPixImagePicker.State>(), IPixImagePicker.ViewModel, IValidator {

}

