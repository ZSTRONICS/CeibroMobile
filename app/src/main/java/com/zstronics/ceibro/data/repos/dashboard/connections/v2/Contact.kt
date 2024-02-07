package com.zstronics.ceibro.data.repos.dashboard.connections.v2

data class Contact(
    val _id: String,
    val contactFirstName: String,
    val contactFullName: String,
    val contactSurName: String,
    val isCeiborUser: Boolean,
    val phoneNumber: String,
    val userCeibroData: String
)