package com.zstronics.ceibro.ui.invitations

import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.state.BaseState
import javax.inject.Inject

class InvitationsState @Inject constructor() : BaseState(), IInvitations.State {
    override var inviteEmail: MutableLiveData<String> = MutableLiveData("")
}