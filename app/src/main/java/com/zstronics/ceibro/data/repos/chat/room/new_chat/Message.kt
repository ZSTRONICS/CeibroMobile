package com.zstronics.ceibro.data.repos.chat.room.new_chat

import android.os.Parcelable
import androidx.annotation.Keep
import com.zstronics.ceibro.data.repos.chat.room.Member
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Message(
    val id: String,
    val initiator: String,
    val isGroupChat: Boolean,
    val members: List<Member>,
    val name: String,
    val pinTitle: String,
) : Parcelable