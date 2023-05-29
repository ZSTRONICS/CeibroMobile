package ee.zstronics.ceibro.camera

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PickedImages(
    val fileUri: Uri?,
    var comment: String = "",
    val fileName: String = "",
    val fileSizeReadAble: String = "",
    val editingApplied: Boolean = false,
    val attachmentType: AttachmentTypes
) : Parcelable