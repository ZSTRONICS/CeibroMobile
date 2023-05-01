package com.zstronics.ceibro.data.repos.dashboard.contacts


import com.google.gson.annotations.SerializedName

data class SyncContactsRequest(
    @SerializedName("contacts")
    val contacts: List<CeibroContactLight>
) {
    data class CeibroContactLight(
        @SerializedName("contactFirstName")
        var contactFirstName: String = "",
        @SerializedName("contactSurName")
        var contactSurName: String = "",
        @SerializedName("countryCode")
        var countryCode: String = "",
        @SerializedName("phoneNumber")
        var phoneNumber: String = "",
        var beneficiaryPictureUrl: String = "",
        var email: String = "",
        var isChecked: Boolean = false
    )
}