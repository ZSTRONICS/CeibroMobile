package com.zstronics.ceibro.data.repos.dashboard.connections


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.chat.room.Member

@Keep
data class MyConnection(
    @SerializedName("from")
    val from: From?,
    @SerializedName("_id")
    val id: String,
    @SerializedName("sentByMe")
    val sentByMe: Boolean,
    @SerializedName("isEmailInvite")
    val isEmailInvite: Boolean,
    @SerializedName("email")
    val email: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("to")
    val to: To
) : BaseResponse()

fun MyConnection.toMember(): Member {
    return Member(
        id = this.from?.id.toString(),
        firstName = this.from?.firstName.toString(),
        surName = this.from?.surName.toString(),
        companyName = this.from?.companyName.toString(),
        profilePic = this.from?.profilePic.toString(),
    )
}