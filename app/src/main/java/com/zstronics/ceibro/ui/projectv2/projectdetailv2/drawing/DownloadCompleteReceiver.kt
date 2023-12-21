package com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawing

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class DownloadCompleteReceiver : BroadcastReceiver() {

    @Inject
    lateinit var downloadedDrawingV2Dao: DownloadedDrawingV2Dao

    @SuppressLint("Range")
    override fun onReceive(context: Context?, intent: Intent?) {


        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent?.action) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            println("downloadId: ${downloadId} Folder name")
            if (downloadId != -1L) {
                // Retrieve the downloaded file information
                val query = DownloadManager.Query().setFilterById(downloadId)
                val manager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val cursor = manager.query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        val uri =
                            Uri.parse(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))
                        val fileName = getFileNameFromUri(uri)
                        fileName?.let {
                            val fileUri = copyFileToInternalStorage(it, uri, context)
                            GlobalScope.launch {
                                val downloadedDrawing =
                                    downloadedDrawingV2Dao.getDownloadedDrawingByDownloadId(
                                        downloadId
                                    )
                                downloadedDrawing?.let {
                                    it.apply {
                                        downloading = false
                                        isDownloaded = true
                                        localUri = fileUri.toString()
                                    }
                                    downloadedDrawingV2Dao.insertDownloadDrawing(downloadedDrawing)
                                }

                            }


                        }
                    }
                }
                cursor.close()
            }
        }


    }


    private fun copyFileToInternalStorage(
        fileName: String,
        uri: Uri,
        context: Context
    ): Uri? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputStream: FileOutputStream

        try {
            // Create a file in the internal storage
            val file = File(context.filesDir, fileName)
            outputStream = FileOutputStream(file)

            // Copy the content of the input stream to the output stream
            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            return Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val file = File(uri.path ?: "")
        return file.name
    }
}
