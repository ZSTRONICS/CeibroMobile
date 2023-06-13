package ee.zstronics.ceibro.camera

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class PickedImages(
    var fileUri: Uri?,
    var comment: String = "",
    val fileName: String = "",
    val fileSizeReadAble: String = "",
    val editingApplied: Boolean = false,
    val attachmentType: AttachmentTypes,
    var file: File
) : Parcelable