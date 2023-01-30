package com.zstronics.ceibro.data.repos.dashboard.invites


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse

@Keep
data class MyInvitations(
    @SerializedName("invites")
    val invites: List<MyInvitationsItem>
) : BaseResponse()