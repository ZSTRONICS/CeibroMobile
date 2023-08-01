package com.zstronics.ceibro.data.repos.dashboard.connections.v2


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class RecentCeibroConnections(
    @SerializedName("recentContacts")
    val recentContacts: List<AllCeibroConnections.CeibroConnection>
) : BaseResponse(), Parcelable