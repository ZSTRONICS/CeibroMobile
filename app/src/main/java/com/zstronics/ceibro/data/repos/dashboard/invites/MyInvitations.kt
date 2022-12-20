package com.zstronics.ceibro.data.repos.dashboard.invites


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

data class MyInvitations(
    @SerializedName("invites")
    var invites: ArrayList<MyInvitationsItem>
) : BaseResponse()