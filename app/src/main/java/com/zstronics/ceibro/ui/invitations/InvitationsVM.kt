package com.zstronics.ceibro.ui.invitations

import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InvitationsVM @Inject constructor(
    override val viewState: InvitationsState,
) : HiltBaseViewModel<IInvitations.State>(), IInvitations.ViewModel {
}