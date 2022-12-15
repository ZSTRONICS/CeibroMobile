package com.zstronics.ceibro.data.repos.chat.room.new_chat


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.chat.room.LastMessage
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.chat.room.Project
import com.zstronics.ceibro.data.repos.chat.room.RemovedAcces
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class GroupChatRoom(
    @SerializedName("admins")
    val admins: List<Member>,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("groups")
    val groups: List<String>,
    @SerializedName("_id")
    val id: String,
    @SerializedName("initiator")
    val initiator: String,
    @SerializedName("isGroupChat")
    val isGroupChat: Boolean,
    @SerializedName("isProjectAttached")
    val isProjectAttached: Boolean,
    @SerializedName("lastMessage")
    val lastMessage: String,
    @SerializedName("lastMessageTime")
    val lastMessageTime: String,
    @SerializedName("members")
    val members: ArrayList<Member>,
    @SerializedName("mutedBy")
    val mutedBy: List<Member>,
    @SerializedName("name")
    val name: String,
    @SerializedName("pinTitle")
    val pinTitle: String,
    @SerializedName("pinnedBy")
    val pinnedBy: ArrayList<String>,
    @SerializedName("project")
    val project: Project,
    @SerializedName("removedAccess")
    val removedAccess: ArrayList<RemovedAcces>,
    @SerializedName("removedMembers")
    val removedMembers: ArrayList<Member>,
    @SerializedName("unreadCount")
    val unreadCount: Int,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("__v")
    val v: Int
) : BaseResponse(), Parcelable