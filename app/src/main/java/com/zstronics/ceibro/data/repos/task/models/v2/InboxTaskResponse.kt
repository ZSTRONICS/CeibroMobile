package com.zstronics.ceibro.data.repos.task.models.v2


import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNamesV2
import com.zstronics.ceibro.data.database.models.inbox.CeibroInboxV2
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class InboxTaskResponse(
    @SerializedName("inboxEvents")
    val inboxEvents: List<CeibroInboxV2>
) : BaseResponse(), Parcelable
