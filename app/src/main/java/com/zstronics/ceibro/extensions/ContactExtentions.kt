package com.zstronics.ceibro.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.base.extensions.removeSpecialChar
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.utils.getDefaultCountryCode

fun getLocalContacts(context: Context) = fetchContacts(context)

private fun fetchContacts(context: Context): MutableList<SyncContactsRequest.CeibroContactLight> {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val contacts = LinkedHashMap<Long, SyncContactsRequest.CeibroContactLight>()

        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.DISPLAY_NAME_PRIMARY,
            ContactsContract.Data.PHOTO_THUMBNAIL_URI,
            ContactsContract.Data.DATA1,
            ContactsContract.Data.MIMETYPE
        )
        val cursor = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            generateSelection(), null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " ASC"
        )

        if (cursor != null) {
            cursor.moveToFirst()
            val idColumnIndex = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
            val displayNamePrimaryColumnIndex =
                cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME_PRIMARY)
            val thumbnailColumnIndex =
                cursor.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI)
            val mimetypeColumnIndex = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE)
            val dataColumnIndex = cursor.getColumnIndex(ContactsContract.Data.DATA1)
            while (!cursor.isAfterLast) {
                val id = cursor.getLong(idColumnIndex)
                var contact = contacts[id]
                if (contact == null) {

                    contact = SyncContactsRequest.CeibroContactLight()
                    val displayName = cursor.getString(displayNamePrimaryColumnIndex)
                    if (displayName != null && displayName.isNotEmpty()) {
                        val names = displayName.split(" ", limit = 2)
                        if (names.size > 1) {
                            contact.contactFirstName = names[0]
                            contact.contactSurName = names[1]
                        } else if (names.size == 1) {
                            contact.contactFirstName = names[0]
                            contact.contactSurName = ""
                        } else {
                            contact.contactFirstName = "No Name"
                            contact.contactSurName = ""
                        }
                    }
                    mapThumbnail(cursor, contact, thumbnailColumnIndex)
                    contacts[id] = contact
                }
                val mimetype = cursor.getString(mimetypeColumnIndex)
                when (mimetype) {
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> mapEmail(
                        cursor,
                        contact,
                        dataColumnIndex
                    )
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                        var phoneNumber: String? = cursor.getString(dataColumnIndex)
                        if (phoneNumber != null && phoneNumber.isNotEmpty()) {
                            phoneNumber = phoneNumber.replace("\\s+".toRegex(), "")

                            try {
                                val pn =
                                    PhoneNumberUtil.getInstance()
                                        .parse(phoneNumber, getDefaultCountryCode(context))
                                contact.phoneNumber = "+${pn.countryCode}${pn.nationalNumber.toString()}"
                                contact.countryCode = "+${pn.countryCode}"
                            } catch (e: Exception) {
                            }
                        }
                    }
                }
                cursor.moveToNext()
            }
            cursor.close()
        }
        return ArrayList(contacts.values)
    }
    return mutableListOf()
}

private fun generateSelection(): String {
    val mSelectionBuilder = StringBuilder()
    if (mSelectionBuilder.isNotEmpty())
        mSelectionBuilder.append(" AND ")
    mSelectionBuilder.append(ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER)
        .append(" = 1")
    return mSelectionBuilder.toString()
}

private fun mapThumbnail(
    cursor: Cursor,
    contact: SyncContactsRequest.CeibroContactLight,
    columnIndex: Int
) {
    val uri = cursor.getString(columnIndex)
    if (uri != null && uri.isNotEmpty()) {
        contact.beneficiaryPictureUrl = uri
    }
}

private fun mapEmail(
    cursor: Cursor,
    contact: SyncContactsRequest.CeibroContactLight,
    columnIndex: Int
) {
    val email = cursor.getString(columnIndex)
    if (email != null && email.isNotEmpty()) {
        contact.email = email
    }
}

//fun MutableList<Contact>.removeOwnContact(): MutableList<Contact> {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//        this.removeIf { it.mobileNo == SessionManager.user?.currentCustomer?.mobileNo }
//    } else {
//        this.remove(this.find { it.mobileNo == SessionManager.user?.currentCustomer?.mobileNo })
//    }
//    return this
//}
