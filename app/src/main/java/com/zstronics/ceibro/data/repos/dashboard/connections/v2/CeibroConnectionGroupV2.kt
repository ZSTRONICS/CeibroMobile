package com.zstronics.ceibro.data.repos.dashboard.connections.v2

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNamesV2
import kotlinx.parcelize.Parcelize

@Entity(tableName = TableNamesV2.ConnectionGroup, primaryKeys = ["_id"])
@Parcelize
@Keep
data class CeibroConnectionGroupV2(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("contacts")
    val contacts: List<GroupContact>,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("creator")
    val creator: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("updatedAt")
    val updatedAt: String
) : BaseResponse(), Parcelable


@Parcelize
@Keep
data class GroupContact(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("contactFirstName")
    val contactFirstName: String,
    @SerializedName("contactFullName")
    val contactFullName: String,
    @SerializedName("contactSurName")
    val contactSurName: String,
    @SerializedName("isCeiborUser")
    val isCeibroUser: Boolean,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("userCeibroData")
    val userCeibroData: AllCeibroConnections.CeibroConnection.UserCeibroData?
) : Parcelable