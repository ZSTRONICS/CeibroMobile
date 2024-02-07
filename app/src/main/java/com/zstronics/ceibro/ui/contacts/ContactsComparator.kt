package com.zstronics.ceibro.ui.contacts

import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncDBContactsList

fun compareContactsAndUpdateList(
    roomContacts: List<AllCeibroConnections.CeibroConnection>,
    phoneContacts: List<SyncContactsRequest.CeibroContactLight>
): List<SyncContactsRequest.CeibroContactLight> {
    // Convert room contacts to a map with phoneNumber as the key
    val roomContactsMap = roomContacts.associateBy { it.phoneNumber }

    // Prepare a list to store updated contacts
    val updatedContacts = mutableListOf<SyncContactsRequest.CeibroContactLight>()

    // Iterate through phone contacts and compare with room contacts
    for (phoneContact in phoneContacts) {
        val roomContact = roomContactsMap[phoneContact.phoneNumber]
        if (roomContact != null) {
            // Contact exists in the room, check for updates
            if (phoneContact.contactFirstName != roomContact.contactFirstName ||
                phoneContact.contactSurName != roomContact.contactSurName
            ) {
                // If any of the contact details have changed, add to the list of updated contacts
                updatedContacts.add(phoneContact)
            }
        }
    }

    return updatedContacts
}

fun compareExistingAndNewContacts(
    roomContacts: List<AllCeibroConnections.CeibroConnection>,
    phoneContacts: List<SyncContactsRequest.CeibroContactLight>
): List<SyncContactsRequest.CeibroContactLight> {
    // Convert room contacts to a map with phoneNumber as the key
    val roomContactsMap = roomContacts.associateBy { it.phoneNumber }

    // Prepare a list to store updated contacts
    val updatedAndNewContacts = mutableListOf<SyncContactsRequest.CeibroContactLight>()

    // Iterate through phone contacts and compare with room contacts
    for (phoneContact in phoneContacts) {
        val roomContact = roomContactsMap[phoneContact.phoneNumber]
        if (roomContact != null) {
            println("UpdateRoomContacts: $phoneContact -> $roomContact")
            // Contact exists in the room, check for updates
            if (phoneContact.contactFirstName != roomContact.contactFirstName ||
                phoneContact.contactSurName != roomContact.contactSurName
            ) {
                println("UpdateRoomContacts: Name updated")
                // If any of the contact details have changed, add to the list of updated contacts
                updatedAndNewContacts.add(phoneContact)
            }
            //in else case, room contact and phone contact are same, so it is skipped
        }
        else {
            //add new contact that does not exist in room
            updatedAndNewContacts.add(phoneContact)
        }
    }

    return updatedAndNewContacts
}

fun findDeletedContacts(
    roomContacts: List<AllCeibroConnections.CeibroConnection>,
    phoneContacts: List<SyncContactsRequest.CeibroContactLight>
): List<AllCeibroConnections.CeibroConnection> {
    // Convert room contacts to a set with phoneNumber as the key
    val phoneContactMap = phoneContacts.associateBy { it.phoneNumber }

    // Prepare a list to store deleted contacts
    val deletedContacts = mutableListOf<AllCeibroConnections.CeibroConnection>()

    // Iterate through phone contacts and compare with room contacts
    for (roomContact in roomContacts) {
        val foundContact = phoneContactMap[roomContact.phoneNumber]
        if (foundContact == null) {
            deletedContacts.add(roomContact)
        }
    }

    return deletedContacts
}

fun findNewContacts(
    roomContacts: List<AllCeibroConnections.CeibroConnection>,
    phoneContacts: List<SyncContactsRequest.CeibroContactLight>
): List<SyncContactsRequest.CeibroContactLight> {
    // Convert room contacts to a set with phoneNumber as the key
    val roomContactsSet = roomContacts.map { it.phoneNumber }.toSet()

    // Prepare a list to store new contacts
    val newContacts = mutableListOf<SyncContactsRequest.CeibroContactLight>()

    // Iterate through phone contacts and compare with room contacts
    for (phoneContact in phoneContacts) {
        if (!roomContactsSet.contains(phoneContact.phoneNumber)) {
            // If the phone contact's phoneNumber is not found in room contacts, it is a new contact
            newContacts.add(phoneContact)
        }
    }

    return newContacts
}

fun List<AllCeibroConnections.CeibroConnection>.toLightContacts(): List<SyncContactsRequest.CeibroContactLight> {
    return this.map { connection ->
        SyncContactsRequest.CeibroContactLight(
            contactFirstName = connection.contactFirstName ?: "",
            contactSurName = connection.contactSurName ?: "",
            countryCode = connection.countryCode,
            phoneNumber = connection.phoneNumber,
            contactFullName = connection.contactFullName,
        )
    }
}

fun List<AllCeibroConnections.CeibroConnection>.toLightDBContacts(): List<SyncDBContactsList.CeibroDBContactsLight> {
    return this.map { connection ->
        SyncDBContactsList.CeibroDBContactsLight(
            connectionId = connection.id,
            contactFirstName = connection.contactFirstName ?: "",
            contactSurName = connection.contactSurName ?: "",
            countryCode = connection.countryCode,
            phoneNumber = connection.phoneNumber,
            contactFullName = connection.contactFullName,
            isCeibroUser = connection.isCeiborUser,
            userCeibroData = connection.userCeibroData
        )
    }
}

fun List<SyncContactsRequest.CeibroContactLight>.toCeibroContacts(): List<AllCeibroConnections.CeibroConnection> {
    return this.map { contact ->
        AllCeibroConnections.CeibroConnection(
            contactFirstName = contact.contactFirstName,
            contactSurName = contact.contactSurName,
            countryCode = contact.countryCode,
            phoneNumber = contact.phoneNumber,
            contactFullName = contact.contactFullName,
            id = "",
            createdAt = "",
            isBlocked = false,
            isCeiborUser = false,
            isSilent = false,
            updatedAt = "",
            userCeibroData = null,
            isChecked = false
        )
    }
}