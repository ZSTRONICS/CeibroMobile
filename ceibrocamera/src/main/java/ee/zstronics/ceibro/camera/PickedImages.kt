package ee.zstronics.ceibro.camera

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PickedImages(
    val fileUri: Uri?,
    val comment: String = "",
    val fileName: String = "",
    val fileSizeReadAble: String = "",
    val editingApplied: Boolean = false,
    val attachmentType: AttachmentTypes
) : Parcelable