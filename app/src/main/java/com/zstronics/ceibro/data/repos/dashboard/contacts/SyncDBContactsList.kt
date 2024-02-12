package com.zstronics.ceibro.data.repos.dashboard.contacts


import com.google.gson.annotations.SerializedName
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections

data class SyncDBContactsList(
    @SerializedName("contacts")
    val contacts: List<CeibroDBContactsLight>
) {
    data class CeibroDBContactsLight(
        @SerializedName("connectionId")
        var connectionId: String = "",
        @SerializedName("contactFirstName")
        var contactFirstName: String = "",
        @SerializedName("contactSurName")
        var contactSurName: String = "",
        @SerializedName("phoneNumber")
        var phoneNumber: String = "",
        @SerializedName("contactFullName")
        var contactFullName: String? = "",
        @SerializedName("isCeibroUser")
        val isCeibroUser: Boolean = false,
        @SerializedName("userCeibroData")
        val userCeibroData: AllCeibroConnections.CeibroConnection.UserCeibroData?,
        @Transient
        var beneficiaryPictureUrl: String = "",
        @Transient
        var isChecked: Boolean = false
    )
}