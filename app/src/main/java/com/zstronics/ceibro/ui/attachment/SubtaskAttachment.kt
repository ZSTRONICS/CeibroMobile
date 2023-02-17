package com.zstronics.ceibro.ui.attachment

import android.net.Uri

data class SubtaskAttachment(
    val attachmentType: AttachmentTypes,
    val attachmentUri: Uri?,
    val fileSize: Int = 0,
    val fileSizeReadAble: String = "",
    val fileName: String = ""
)