package ee.zstronics.ceibro.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ceibro.permissionx.PermissionX
import com.github.florent37.inlineactivityresult.kotlin.startForResult
import ee.zstronics.photoediting.EditImageActivity
import java.io.File
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
            fileSizeReadAble = fileSizeReadAble,
            file = FileUtils.getFile(applicationContext, fileUri)
        )
    }

    fun startEditor(imageUri: Uri, onPhotoEditedCallback: (updatedUri: Uri?) -> Unit) {
        launchActivityForResult<EditImageActivity>(init = {
            this.data = imageUri
            action = Intent.ACTION_EDIT
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