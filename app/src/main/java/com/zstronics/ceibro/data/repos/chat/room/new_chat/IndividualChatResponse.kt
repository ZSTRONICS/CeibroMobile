package com.zstronics.ceibro.data.repos.chat.room.new_chat

import android.os.Parcelable
import androidx.annotation.Keep
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class IndividualChatResponse(
    val message: Message
): BaseResponse(), Parcelable