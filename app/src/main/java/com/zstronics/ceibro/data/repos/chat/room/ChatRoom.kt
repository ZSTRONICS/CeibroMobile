package com.zstronics.ceibro.data.repos.chat.room


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.ArrayList

@Keep
@Parcelize
data class ChatRoom(
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("_id")
    val id: String,
    @SerializedName("initiator")
    val initiator: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("isGroupChat")
    val isGroupChat: Boolean,
    @SerializedName("lastMessage")
    val lastMessage: LastMessage?,
    @SerializedName("members")
    val members: ArrayList<Member>,
    @SerializedName("mutedBy")
    val mutedBy: List<Member>,
    @SerializedName("pinTitle")
    val pinTitle: String,
    @SerializedName("pinnedBy")
    val pinnedBy: ArrayList<String>,
    @SerializedName("removedMembers")
    val removedMembers: List<Member>,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("__v")
    val v: Int,
    @SerializedName("project")
    val project: Project?,
    @SerializedName("unreadCount")
    val unreadCount: Int,
) : BaseResponse(), Parcelable

@Keep
@Parcelize
data class Project(
    @SerializedName("title")
    val title: String,
    @SerializedName("id")
    val id: String,
) : BaseResponse(), Parcelable