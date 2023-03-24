package com.zstronics.ceibro.ui.admin.users

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AllUsersVM @Inject constructor(
    override val viewState: AllUsersState,
) : HiltBaseViewModel<IAllUsers.State>(), IAllUsers.ViewModel {
}