package com.zstronics.ceibro.ui.chat.extensions

import com.zstronics.ceibro.data.repos.auth.login.User
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.data.repos.chat.room.Member

fun ChatRoom.getChatTitle(user: User?): String {
    var otherUser: Member? = this.members.find { member -> member.id != user?.id }
    if (otherUser == null) {
        otherUser = this.removedAccess.find { member -> member.id != user?.id }
    }
    return if (this.isGroupChat)
        this.name
    else
        "${otherUser?.firstName} ${otherUser?.surName}"

}