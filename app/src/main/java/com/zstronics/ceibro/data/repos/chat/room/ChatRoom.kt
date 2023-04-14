package com.zstronics.ceibro.data.repos.chat.room


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ChatRoom(
//    @SerializedName("admins")
//    val admins: List<String>,         //admins are not populated, they are list of string when get chat, but on new chat creation it's populated
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("groups")
    val groups: List<String>,
    @SerializedName("_id")
    val id: String,
//    @SerializedName("initiator")
//    val initiator: Initiator,         //initiator is populated when get chat, but on new chat creation it's not populated
    @SerializedName("isGroupChat")
    val isGroupChat: Boolean,
    @SerializedName("isProjectAttached")
    val isProjectAttached: Boolean,
    @SerializedName("lastMessage")
    var lastMessage: LastMessage?,
    @SerializedName("lastMessageTime")
    val lastMessageTime: String,
    @SerializedName("members")
    val members: ArrayList<Member>,
    @SerializedName("mutedBy")
    val mutedBy: List<String>,
    @SerializedName("name")
    val name: String,
    @SerializedName("pinTitle")
    val pinTitle: String,
    @SerializedName("pinnedBy")
    val pinnedBy: ArrayList<String>,
    @SerializedName("project")
    val project: Project,
    @SerializedName("removedAccess")
    val removedAccess: ArrayList<Member>,
    @SerializedName("removedMembers")
    val removedMembers: ArrayList<Member>,
    @SerializedName("unreadCount")
    var unreadCount: Int,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("__v")
    val v: Int
) : BaseResponse(), Parcelable