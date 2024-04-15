package com.zstronics.ceibro.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URLEncoder

object Filer{
    fun copyFileToInternalStorage(mContext: Context, uri: Uri, newDirName: String): String {
        val returnCursor: Cursor? = mContext.contentResolver.query(
            uri,
            arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE
            ),
            null,
            null,
            null
        )

        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val size = returnCursor.getLong(sizeIndex).toString()
        val output: File
        if (newDirName != "") {
            val dir = File(
                mContext.filesDir.toString()
                        + "/" + newDirName
            )
            if (!dir.exists()) {
                dir.mkdir()
            }
            output = File(
                mContext.filesDir.toString()
                        + "/" + newDirName + "/"
                        + URLEncoder.encode(name, "utf-8")
            )
        } else {
            output = File(
                mContext.filesDir.toString()
                        + "/" + URLEncoder.encode(name, "utf-8")
            )
        }
        try {
            val inputStream: InputStream? =
                mContext.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(output)
            var read = 0
            val bufferSize = 1024
            val buffers = ByteArray(bufferSize)
            while (inputStream!!.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: java.lang.Exception) {
            Log.e("Exception", e.message!!)
        }
        return output.path
    }
    val fileMimeType = arrayOf(
        "text/plain",
        "text/csv",
        "application/pdf",  //                        "application/rtf",
        //                        "application/zip",
        "application/vnd.oasis.opendocument.text",  // .odt
        "application/vnd.oasis.opendocument.spreadsheet",  // .ods
        "application/vnd.oasis.opendocument.presentation",  // .odp
        //                        "application/x-rar-compressed",
        //                        "application/vnd.android.package-archive",      //for APK file
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // .docx
        "application/vnd.ms-word.document.macroEnabled.12",  // .doc
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // .xlsx
        "application/vnd.ms-excel.sheet.macroEnabled.12",  // .xls
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",  // .pptx
        "application/vnd.ms-powerpoint.presentation.macroEnabled.12",  // .ppt
        //                        "image/vnd.dwg",    // AutoCAD Drawing Database (DWG)
        //                        "application/acad"  // AutoCAD Drawing
        //                        "image/vnd.adobe.photoshop", // Photoshop Document (PSD)
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/webp",
        "image/bmp",
        "image/*"
    )
}