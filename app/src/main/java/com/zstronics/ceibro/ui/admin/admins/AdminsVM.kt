package com.zstronics.ceibro.ui.admin.admins

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminsVM @Inject constructor(
    override val viewState: AdminsState,
) : HiltBaseViewModel<IAdmins.State>(), IAdmins.ViewModel {
}