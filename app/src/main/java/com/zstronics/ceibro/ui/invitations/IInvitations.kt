package com.zstronics.ceibro.ui.invitations

import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.base.interfaces.IBase

interface IInvitations {
    interface State : IBase.State {
        var inviteEmail: MutableLiveData<String>
    }

    interface ViewModel : IBase.ViewModel<State> {
        fun loadInvitations(invitationsRV: RecyclerView)
        fun onInvite()
        fun sendInvite(email: String)
        fun acceptOrRejectInvitation(accepted: Boolean, inviteId: String, position: Int)
        fun acceptOrRejectAllInvitations(accepted: Boolean)
    }
}