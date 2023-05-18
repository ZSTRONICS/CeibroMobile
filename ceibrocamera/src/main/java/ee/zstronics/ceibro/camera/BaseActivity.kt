package ee.zstronics.ceibro.camera

import android.app.Activity
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.ceibro.permissionx.PermissionX

open class BaseActivity : AppCompatActivity() {
    fun checkPermission(permissionsList: List<String>, function: () -> Unit) {
        PermissionX.init(this).permissions(
            permissionsList
        )
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    function.invoke()
                } else {
                    //toast(getString(R.string.common_text_permissions_denied))
                }
            }
    }

    fun pickFiles(
        allowMultiple: Boolean = true,
        onPickAttachments: (list: ArrayList<PickedImages>) -> Unit
    ) {
        val pickedImages = arrayListOf<PickedImages>()
//        pickedImages.add(
//            PickedImages(
//                fileUri = "fileUri".toUri(),
//                attachmentType = AttachmentTypes.Image,
//                fileName = "fileName",
//                fileSizeReadAble = "fileSizeReadAble"
//            )
//        )
//        onPickAttachments.invoke(pickedImages)
//        return
        val onPickAttachmentsRef = onPickAttachments
        openFilePicker(
            allowMultiple = allowMultiple,
            mimeTypes = arrayOf(
                "image/png",
                "image/jpg",
                "image/jpeg",
                "image/*"
            )
        ) { resultCode, data ->
            if (resultCode == Activity.RESULT_OK && data != null) {
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val fileUri = clipData.getItemAt(i).uri
                        // Add the URI to the list
                        pickedImages.add(getPickedImage(fileUri))
                    }
                    onPickAttachmentsRef.invoke(pickedImages)
                } else {
                    val fileUri = data.data
                    pickedImages.add(getPickedImage(fileUri))
                    onPickAttachmentsRef.invoke(pickedImages)
                }
            }
        }
    }

    fun getPickedImage(fileUri: Uri?): PickedImages {
        val mimeType = FileUtils.getMimeType(applicationContext, fileUri)
        val fileName = FileUtils.getFileName(applicationContext, fileUri)
        val fileSize = FileUtils.getFileSizeInBytes(applicationContext, fileUri)
        val fileSizeReadAble = FileUtils.getReadableFileSize(fileSize)
        val attachmentType = when {
            mimeType.startsWith("image") -> {
                AttachmentTypes.Image
            }
            mimeType.startsWith("video") -> {
                AttachmentTypes.Video
            }
            mimeType == "application/pdf" -> {
                AttachmentTypes.Pdf
            }
            mimeType == "application/msword" || mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                AttachmentTypes.Doc
            }
            else -> AttachmentTypes.Doc
        }
        return PickedImages(
            fileUri = fileUri,
            attachmentType = attachmentType,
            fileName = fileName,
            fileSizeReadAble = fileSizeReadAble
        )
    }
}