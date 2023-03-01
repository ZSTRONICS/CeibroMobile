package com.zstronics.ceibro.data.repos.task.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SubTaskRejections(
    @SerializedName("_id") val _id: String,
    @SerializedName("access") val access: List<String>?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("files") val files: List<String>?,
    @SerializedName("isFileAttached") val isFileAttached: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("seenBy") val seenBy: List<String>?,
    @SerializedName("sender") val sender: Sender?,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("userState") val userState: String
) : BaseResponse(), Parcelable {

    @Keep
    @Parcelize
    data class Sender(
        @SerializedName("_id") val _id: String,
        @SerializedName("firstName") val firstName: String?,
        @SerializedName("surName") val surName: String?,
        @SerializedName("profilePic") val profilePic: String?
    ) : BaseResponse(), Parcelable

}