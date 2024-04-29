package com.zstronics.ceibro.ui.locationv2

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.zstronics.ceibro.base.extensions.cancelAndMakeToast
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.databinding.SpinnerDrawingItemsBinding
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import com.zstronics.ceibro.utils.Filer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class SpinnerAdapter constructor(
    private val downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
    private val networkConnectivityObserver: NetworkConnectivityObserver
) : BaseAdapter() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionList13 = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    private val permissionList10 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private var listItems: ArrayList<DrawingV2> = ArrayList()
    var spinner: Spinner? = null
    private var drawingFileClickListener: ((view: View, data: DrawingV2, absolutePath: String) -> Unit)? =
        null
    private var downloadFileClickListener: ((view: TextView, ivDownloadFile: AppCompatImageView, ivDownloaded: AppCompatImageView, data: DrawingV2, tag: String) -> Unit)? =
        null
    private var requestPermissionClickListener: ((tag: String) -> Unit)? = null

    fun setDrawingFileClickListener(listener: (view: View, data: DrawingV2, absolutePath: String) -> Unit) {
        drawingFileClickListener = listener
    }

    fun setDownloadFileClickListener(listener: (view: TextView, ivDownloadFile: AppCompatImageView, ivDownloaded: AppCompatImageView, data: DrawingV2, tag: String) -> Unit) {
        downloadFileClickListener = listener
    }

    fun setRequestPermissionClickListener(listener: (tag: String) -> Unit) {
        requestPermissionClickListener = listener
    }


    fun setList(list: List<DrawingV2>) {
        this.listItems.clear()
        this.listItems.addAll(list.toMutableList())
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return listItems.size
    }

    override fun getItem(position: Int): Any {
        return listItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            val binding = SpinnerDrawingItemsBinding.inflate(
                LayoutInflater.from(parent?.context),
                parent,
                false
            )
            view = binding.root
            holder = ViewHolder(binding)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        holder.bind(listItems[position], position)

        return view
    }

    private inner class ViewHolder(private val binding: SpinnerDrawingItemsBinding) {
        private val context: Context = binding.root.context

        fun bind(data: DrawingV2, position: Int) {
            binding.apply {
                tvSample.text = "${data.fileName}"
                tvFloor.text = "${data.floor.floorName} Floor"

                MainScope().launch {
                    val drawingObject =
                        downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(data._id)
                    drawingObject?.let {

                        if (it.isDownloaded && it.localUri.isNotEmpty()) {
                            ivDownloaded.visibility =
                                View.GONE
                            viewOne.visibility =
                                View.GONE
                          //  ivDownloaded.visibility = View.VISIBLE
                            tvDownloadProgress.visibility = View.GONE
                            ivDownloadFile.visibility = View.GONE
                        } else if (it.downloading) {
                            ivDownloaded.visibility = View.GONE
                            tvDownloadProgress.visibility = View.VISIBLE
                            ivDownloadFile.visibility = View.GONE
                            getDownloadProgress(
                                tvDownloadProgress.context,
                                it.downloadId
                            ) { status, filepath, progress ->
                                MainScope().launch {
                                    if (status.equals("downloaded", true)) {

                                        ivDownloaded.visibility =
                                            View.GONE
                                        viewOne.visibility =
                                            View.GONE
                                        notifyPropertyChanged(position)

                                        if (filepath.isNotEmpty()) {
                                            ivDownloadFile.visibility =
                                                View.GONE
                                            tvDownloadProgress.visibility =
                                                View.GONE
                                       //     ivDownloaded.visibility = View.VISIBLE
                                            tvDownloadProgress.text = progress
                                        } else {
                                            downloadedDrawingV2Dao.deleteByDrawingID(data._id)
                                            ivDownloaded.visibility = View.GONE
                                            tvDownloadProgress.visibility =
                                                View.GONE
                                            ivDownloadFile.visibility =
                                                View.VISIBLE
                                        }
                                    } else if (status == "retry" || status == "failed") {
                                        downloadedDrawingV2Dao.deleteByDrawingID(data._id)
                                        tvDownloadProgress.text = "0%"
                                        ivDownloaded.visibility = View.GONE
                                        tvDownloadProgress.visibility = View.GONE
                                        ivDownloadFile.visibility = View.VISIBLE
                                    } else if (status == "downloading") {
                                        ivDownloadFile.visibility = View.GONE
                                        tvDownloadProgress.visibility = View.VISIBLE
                                        tvDownloadProgress.text = progress
                                    }
                                }
                            }
                        } else {
                            ivDownloaded.visibility = View.GONE
                            tvDownloadProgress.visibility = View.GONE
                            ivDownloadFile.visibility = View.VISIBLE
                        }
                    } ?: kotlin.run {
                        ivDownloaded.visibility = View.GONE
                        tvDownloadProgress.visibility = View.GONE
                        ivDownloadFile.visibility = View.VISIBLE
                    }
                }
                ivDownloadFile.setOnClickListener {

                    if (data.fileUrl.isEmpty()) {
                        cancelAndMakeToast(
                            it.context,
                            "File address is invalid or file is corrupted",
                            Toast.LENGTH_SHORT
                        )
                    } else {
                        if (networkConnectivityObserver.isNetworkAvailable()) {

                            if (checkDownloadFilePermission(
                                    context
                                )
                            ) {

                                it.visibility = View.GONE
                                tvDownloadProgress.visibility = View.VISIBLE
                                downloadFileClickListener?.invoke(
                                    tvDownloadProgress,
                                    ivDownloadFile,
                                    ivDownloaded,
                                    data,
                                    ""
                                )


                                Handler(Looper.getMainLooper()).postDelayed({
                                    MainScope().launch {
                                        val drawingObject =
                                            downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(
                                                data._id
                                            )
                                        drawingObject?.let {

                                            if (it.isDownloaded && it.localUri.isNotEmpty()) {
                                                ivDownloaded.visibility =
                                                    View.GONE
                                                viewOne.visibility =
                                                    View.GONE
                                              //  ivDownloaded.visibility = View.VISIBLE
                                                tvDownloadProgress.visibility = View.GONE
                                                ivDownloadFile.visibility = View.GONE
                                            } else if (it.downloading) {
                                                ivDownloaded.visibility = View.GONE
                                                tvDownloadProgress.visibility = View.VISIBLE
                                                ivDownloadFile.visibility = View.GONE
                                                getDownloadProgress(
                                                    tvDownloadProgress.context,
                                                    it.downloadId
                                                ) { status, filepath, progress ->
                                                    MainScope().launch {
                                                        if (status.equals("downloaded", true)) {
                                                            ivDownloaded.visibility =
                                                                View.GONE
                                                            viewOne.visibility =
                                                                View.GONE
                                                            notifyPropertyChanged(position)
                                                            if (filepath.isNotEmpty()) {
                                                                ivDownloadFile.visibility =
                                                                    View.GONE
                                                                tvDownloadProgress.visibility =
                                                                    View.GONE
                                                               // ivDownloaded.visibility = View.VISIBLE
                                                                tvDownloadProgress.text = progress
                                                            } else {
                                                                downloadedDrawingV2Dao.deleteByDrawingID(
                                                                    data._id
                                                                )
                                                                ivDownloaded.visibility = View.GONE
                                                                tvDownloadProgress.visibility =
                                                                    View.GONE
                                                                ivDownloadFile.visibility =
                                                                    View.VISIBLE
                                                            }
                                                        } else if (status == "retry" || status == "failed") {
                                                            downloadedDrawingV2Dao.deleteByDrawingID(
                                                                data._id
                                                            )
                                                            tvDownloadProgress.text = "0%"
                                                            ivDownloaded.visibility = View.GONE
                                                            tvDownloadProgress.visibility =
                                                                View.GONE
                                                            ivDownloadFile.visibility = View.VISIBLE
                                                        } else if (status == "downloading") {
                                                            ivDownloadFile.visibility = View.GONE
                                                            tvDownloadProgress.visibility =
                                                                View.VISIBLE
                                                            tvDownloadProgress.text = progress
                                                        }
                                                    }
                                                }
                                            } else {
                                                ivDownloaded.visibility = View.GONE
                                                tvDownloadProgress.visibility = View.GONE
                                                ivDownloadFile.visibility = View.VISIBLE
                                            }
                                        } ?: kotlin.run {
                                            ivDownloaded.visibility = View.GONE
                                            tvDownloadProgress.visibility = View.GONE
                                            ivDownloadFile.visibility = View.VISIBLE
                                        }
                                    }

                                }, 500)


                            } else {

                                requestPermissionClickListener?.invoke("getpermissoin")
                            }

                        } else {
                            cancelAndMakeToast(
                                it.context,
                                "No Internet Available.",
                                Toast.LENGTH_SHORT
                            )
                        }
                    }
                }

            }
        }
    }

    private fun checkDownloadFilePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermissions(permissionList13, context)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            true
        } else {
            checkPermissions(permissionList10, context)
        }
    }

    private fun checkPermissions(permissions: Array<String>, context: Context): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("Range")
    private fun getDownloadProgress(
        context: Context?,
        downloadId: Long,
        itemClickListener: ((tag: String, fileAbsolutePath: String, progress: String) -> Unit)?
    ) {
        GlobalScope.launch {
            while (true) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val manager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val cursor = manager.query(query)

                if (cursor.moveToFirst()) {
                    val bytesDownloaded =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytesTotal =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                    val status =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    println("Status: $status")

                    if (status.toInt() == DownloadManager.STATUS_FAILED) {
                        println("Status failed: $status")
                        itemClickListener?.invoke("failed", "", "")
                        break
                    } else if (status.toInt() == DownloadManager.STATUS_SUCCESSFUL) {
                        val uri =
                            Uri.parse(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))
                        val fileName = getFileNameFromUri(uri)
                        fileName?.let {
                            val fileAbsolutePath =
                                Filer.copyFileToInternalStorageExtension(context, uri, "", it)
                            GlobalScope.launch {
                                val downloadedDrawing =
                                    downloadedDrawingV2Dao.getDownloadedDrawingByDownloadId(
                                        downloadId
                                    )
                                fileAbsolutePath?.let { fileAbsolutePath ->
                                    downloadedDrawing?.let {
                                        it.apply {
                                            downloading = false
                                            isDownloaded = true
                                            localUri = fileAbsolutePath
                                        }
                                        downloadedDrawingV2Dao.insertDownloadDrawing(
                                            downloadedDrawing
                                        )
                                    }
                                }
                                itemClickListener?.invoke(
                                    "downloaded",
                                    fileAbsolutePath ?: "",
                                    "100%"
                                )
                            }
                        }

                        break
                    }

                    val downloadedPercent = ((bytesDownloaded * 100L) / bytesTotal).toInt()

                    println("StatusProgress %: $downloadedPercent")
                    println("StatusDownloaded: $bytesDownloaded")
                    println("StatusTotal: $bytesTotal")

                    itemClickListener?.invoke("downloading", "", "$downloadedPercent%")
                    if (bytesTotal > 0) {
                        println("Progress: " + ((bytesDownloaded * 100L) / bytesTotal).toInt())
                    }
                } else {
                    itemClickListener?.invoke("retry", "", "")
                    break
                }

                cursor.close()

                delay(500)
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val file = File(uri.path ?: "")
        return file.name
    }

//    private fun copyFileToInternalStorage(
//        fileName: String,
//        uri: Uri,
//        context: Context
//    ): String? {
//        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
//        val outputStream: FileOutputStream
//
//        try {
//            val file = File(context.filesDir, fileName)
//            outputStream = FileOutputStream(file)
//
//
//            inputStream?.copyTo(outputStream)
//
//            inputStream?.close()
//            outputStream.close()
//
//            return file.absolutePath
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//        return null
//    }
}
