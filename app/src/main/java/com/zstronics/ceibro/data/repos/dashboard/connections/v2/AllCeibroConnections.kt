package com.zstronics.ceibro.data.repos.dashboard.connections.v2


import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.database.TableNamesV2
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class AllCeibroConnections(
    @SerializedName("contacts")
    val contacts: List<CeibroConnection>
) : BaseResponse(), Parcelable {

    @Entity(tableName = TableNamesV2.Connections)
    @Parcelize
    @Keep
    data class CeibroConnection(
        @PrimaryKey
        @SerializedName("_id")
        val id: String,
        @SerializedName("contactFullName")
        val contactFullName: String?,
        @SerializedName("contactFirstName")
        val contactFirstName: String?,
        @SerializedName("contactSurName")
        val contactSurName: String?,
        @SerializedName("countryCode")
        val countryCode: String,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("isBlocked")
        var isBlocked: Boolean,
        @SerializedName("isCeiborUser")
        val isCeiborUser: Boolean,
        @SerializedName("isSilent")
        val isSilent: Boolean,
        @SerializedName("phoneNumber")
        val phoneNumber: String,
        @SerializedName("updatedAt")
        val updatedAt: String,
        @SerializedName("userCeibroData")
        val userCeibroData: UserCeibroData?,
        var isChecked: Boolean = false
    ) : BaseResponse(), Parcelable {
        @Parcelize
        @Keep
        data class UserCeibroData(
            @PrimaryKey(autoGenerate = true)
            val UserCeibroDataId: Int = 0,
            @SerializedName("companyName")
            val companyName: String,
            @SerializedName("email")
            val email: String?,
            @SerializedName("firstName")
            val firstName: String?,
            @SerializedName("_id")
            val id: String,
            @SerializedName("jobTitle")
            val jobTitle: String,
            @SerializedName("phoneNumber")
            val phoneNumber: String,
            @SerializedName("profilePic")
            val profilePic: String,
            @SerializedName("surName")
            val surName: String?
        ) : BaseResponse(), Parcelable
    }
}

//@Entity(tableName = TableNamesV2.Connections)
data class ConnectionsV2DatabaseEntity(
    @PrimaryKey
    val id: Int = 0,
    @ColumnInfo("contacts")
    val contacts: List<AllCeibroConnections.CeibroConnection>
)

data class HeaderItem(val title: String) : BaseResponse()