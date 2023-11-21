package com.zstronics.ceibro.data.repos

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class NotificationTaskData(
    val avatar: String,
    val creator: String,
    val description: String,
    val taskId: String,
    val title: String,
    val eventId: String? = "",
    val userId: String
) : Parcelable