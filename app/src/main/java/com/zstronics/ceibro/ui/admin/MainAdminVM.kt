package com.zstronics.ceibro.ui.admin

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainAdminVM @Inject constructor(
    override val viewState: MainAdminState,
) : HiltBaseViewModel<IMainAdmin.State>(), IMainAdmin.ViewModel {
}