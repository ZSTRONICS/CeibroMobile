package ee.zstronics.ceibro.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ceibro.permissionx.PermissionX
import com.github.florent37.inlineactivityresult.kotlin.startForResult
import ee.zstronics.photoediting.EditImageActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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

    fun pickImageFiles(
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
                        val fileName = getFileNameFromUri(this, fileUri)
                        var fileExtension = getFileExtension(this, fileUri)
                        if (fileExtension.isNullOrEmpty()){
                            fileExtension="jpg"
                        }
                        val newUri = createFileUriFromContentUri(fileUri, fileName, fileExtension)
                        pickedImages.add(getPickedImage(newUri))
                    }
                    onPickAttachmentsRef.invoke(pickedImages)
                } else {
                    val fileUri = data.data
                    val fileName = fileUri?.let { getFileNameFromUri(this, it) }
                    var fileExtension = fileUri?.let { getFileExtension(this, it) }
                    if (fileExtension.isNullOrEmpty()){
                        fileExtension="jpg"
                    }
                    val newUri = fileUri?.let { createFileUriFromContentUri(it, fileName, fileExtension) }
                    pickedImages.add(getPickedImage(newUri))
                    onPickAttachmentsRef.invoke(pickedImages)
                }
            }
        }
    }


    private fun getFileExtension(context: Context, fileUri: Uri): String? {
        val contentResolver: ContentResolver = context.contentResolver

        // Try to get the file extension using the ContentResolver
        val type = contentResolver.getType(fileUri)

        if (type != null) {
            // Extract file extension from the MIME type
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
        } else {
            // If ContentResolver couldn't determine the type, try using MimeTypeMap with URI path
            val pathSegments = fileUri.pathSegments
            val lastPathSegment = pathSegments.lastOrNull()

            if (lastPathSegment != null) {
                val dotIndex = lastPathSegment.lastIndexOf('.')
                if (dotIndex != -1 && dotIndex < lastPathSegment.length - 1) {
                    return lastPathSegment.substring(dotIndex + 1)
                }
            }
        }

        // Unable to determine file extension
        return null
    }

    @SuppressLint("Range")
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName =
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                fileName = displayName
            }
        }
        return fileName
    }

    //converting -> content://com.android.providers.media.documents/document/image%3A17     into next file format so that name would be accurate in all devices    file:///storage/emulated/0/Android/data/com.zstronics.ceibro.dev/files/1695738659642.jpg
    private fun createFileUriFromContentUri(
        contentUri: Uri,
        fileName: String?,
        fileExtension: String
    ): Uri? {
        val outputPath = getExternalFilesDir(null)?.absolutePath
        val filename = if (fileName.isNullOrEmpty()) {
            System.currentTimeMillis().toString() + ".jpg"
        } else {
            if (!ensureImageExtension(fileName).isNullOrEmpty()){
                fileName
            } else {
                "$fileName.$fileExtension"
            }
        }

        try {
            val input = contentResolver.openInputStream(contentUri)
            val destinationFile = File(outputPath, filename)
            val output = FileOutputStream(destinationFile)

            input?.use { input ->
                output.use { output ->
                    input.copyTo(output)
                }
            }

            return Uri.fromFile(destinationFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contentUri
    }

    private fun ensureImageExtension(fileName: String): String? {
        // List of common image extensions
        val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic")

        // Extract the extension from the file name
        val lastDotIndex = fileName.lastIndexOf('.')
        if (lastDotIndex != -1 && lastDotIndex < fileName.length - 1) {
            val extension = fileName.substring(lastDotIndex + 1).toLowerCase()
            if (extension in imageExtensions) {
                // The file name already has a valid image extension
                return extension
            }
        }
        return null
    }

    fun getPickedImage(fileUri: Uri?): PickedImages {
        val mimeType = FileUtils.getMimeType(applicationContext, fileUri)
        val fileName = FileUtils.getFileName(applicationContext, fileUri)
        val fileSize = FileUtils.getFileSizeInBytes(applicationContext, fileUri)
        val fileSizeReadAble = FileUtils.getReadableFileSize(fileSize)
        val attachmentType = when {
            mimeType == null -> {
                AttachmentTypes.Image
            }

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
            fileSizeReadAble = fileSizeReadAble,
            file = FileUtils.getFile(applicationContext, fileUri)
        )
    }

    fun startEditor(
        imageUri: Uri,
        editType: String,
        onPhotoEditedCallback: (updatedUri: Uri?) -> Unit
    ) {
        launchActivityForResult<EditImageActivity>(init = {
            this.data = imageUri
            action = Intent.ACTION_EDIT
            this.putExtra("editType", editType)

        }) { resultCode, data ->
//            println("ImagesURIInEditMode0: ${data?.data}")
            if (data?.data != null) {       //If null then it means no changes done in file so don't delete the file, issue fixed
                if (imageUri.toString().contains("content://media/")) {
                    try {
                        val contentResolver = applicationContext.contentResolver
                        val projection = arrayOf(MediaStore.Images.Media.DATA)
                        val cursor = contentResolver.query(imageUri, projection, null, null, null)
                        val filePath: String? = cursor?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                            } else {
                                null
                            }
                        }
                        val fileToDelete = filePath?.let { File(it) }
//                        println("ImagesURIInEditMode1: $fileToDelete")
                        if (fileToDelete?.exists() == true) {
                            val deleted = fileToDelete.delete()
//                            println("ImagesURIInEditMode1: File Deleted")
                        }
                    } catch (_: Exception) {
                    }
                } else {
                    try {
                        val oldFile = imageUri.toFile()
//                        println("ImagesURIInEditMode2: $oldFile")
                        if (oldFile.exists()) {
                            val deleted = oldFile.delete()
//                            println("ImagesURIInEditMode2: File Deleted")
                        }
                    } catch (_: Exception) {
                    }
                }
            }
            onPhotoEditedCallback(data?.data)
        }
    }

    inline fun <reified T : Any> newIntent(context: Context): Intent =
        Intent(context, T::class.java)

    inline fun <reified T : Any> Fragment.launchActivityForResult(
        options: Bundle? = null, clearPrevious: Boolean = false,
        noinline init: Intent.() -> Unit = {},
        noinline onActivityResult: ((resultCode: Int, data: Intent?) -> Unit)? = null
    ) {

        onActivityResult?.let {
            val intent = newIntent<T>(requireContext())
            intent.init()
            intent.putExtra(EXTRA, options)
            this.startForResult(intent) { result ->
                it.invoke(result.resultCode, result.data)
            }.onFailed { result ->
                it.invoke(result.resultCode, result.data)
            }
        } ?: run {
            launchActivity<T>(clearPrevious = clearPrevious, options = options)
        }
    }

    inline fun <reified T : Any> Activity.launchActivity(
        requestCode: Int = -1,
        options: Bundle? = null,
        clearPrevious: Boolean = false,
        noinline init: Intent.() -> Unit = {}
    ) {
        val intent = newIntent<T>(this)
        intent.init()
        intent.putExtra(EXTRA, options)
        if (clearPrevious) finish()
        startActivityForResult(intent, requestCode, options)

    }

    inline fun <reified T : Any> FragmentActivity.launchActivityForResult(
        requestCode: Int = -1,
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = {},
        noinline completionHandler: ((resultCode: Int, data: Intent?) -> Unit)? = null
    ) {
        completionHandler?.let {
            val intent = newIntent<T>(this)
            intent.init()
            intent.putExtra(EXTRA, options)
            this@launchActivityForResult.startForResult(intent) { result ->
                it.invoke(result.resultCode, result.data)
            }.onFailed { result ->
                it.invoke(result.resultCode, result.data)
            }
        } ?: run {
            launchActivity<T>(
                requestCode = requestCode,
                options = options,
                init = init
            )
        }
    }
    fun getCurrentTimeStamp(): String {
        val sdf = SimpleDateFormat(SERVER_DATE_FULL_FORMAT, Locale.getDefault())
        sdf.timeZone = UTC
        return sdf.format(Date())
    }
    companion object{
        const val SERVER_DATE_FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS"//2015-11-28 10:17:18
        val UTC: TimeZone = TimeZone.getTimeZone("UTC")
    }
}