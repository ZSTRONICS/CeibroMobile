package com.zstronics.ceibro.data.repos.projects.projectsmain

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.chat.room.Member

@Keep
data class GetAvailableMemberResponse(
    @SerializedName("result")
    val members: List<Member>
) : BaseResponse()