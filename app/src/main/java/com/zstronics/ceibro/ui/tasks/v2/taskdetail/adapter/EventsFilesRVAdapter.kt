package com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.base.extensions.cancelAndMakeToast
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroDownloadDrawingV2
import com.zstronics.ceibro.data.database.models.tasks.EventFiles
import com.zstronics.ceibro.databinding.LayoutCeibroFilesBinding
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class EventsFilesRVAdapter constructor(
    val networkConnectivityObserver: NetworkConnectivityObserver,
    val context: Context,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
) :
    RecyclerView.Adapter<EventsFilesRVAdapter.FilesViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: EventFiles) -> Unit)? =
        null
    var listItems: MutableList<EventFiles> = mutableListOf()


    var fileClickListener: ((view: View, position: Int, data: EventFiles, downloadedData: CeibroDownloadDrawingV2) -> Unit)? =
        null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionList13 = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    private val permissionList10 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    var requestPermissionClickListener: (() -> Unit)? = null

    fun requestPermissionCallBack(requestPermissionClickListener: () -> Unit) {
        this.requestPermissionClickListener = requestPermissionClickListener
    }

    var downloadFileClickListener: ((textView: TextView, ivDownload: AppCompatImageView, downloaded: AppCompatImageView, data: Triple<String, String, String>, tag: String) -> Unit)? =
        null

    fun downloadFileCallBack(itemClickListener: ((textView: TextView, ivDownload: AppCompatImageView, downloaded: AppCompatImageView, data: Triple<String, String, String>, tag: String) -> Unit)?) {
        this.downloadFileClickListener = itemClickListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FilesViewHolder {
        return FilesViewHolder(
            LayoutCeibroFilesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FilesViewHolder, position: Int) {
        holder.bind(listItems[position], position)
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<EventFiles>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class FilesViewHolder(private val binding: LayoutCeibroFilesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EventFiles, position: Int) {

            /*         binding.root.setOnClickListener {
                         fileClickListener?.invoke(it, absoluteAdapterPosition, item)
                     }
                     binding.mainLayout.setOnClickListener {
                         fileClickListener?.invoke(it, absoluteAdapterPosition, item)
                     }

                     binding.fileName.setOnClickListener {
                         fileClickListener?.invoke(it, absoluteAdapterPosition, item)
                     }
                     binding.fileSize.setOnClickListener {
                         fileClickListener?.invoke(it, absoluteAdapterPosition, item)
                     }

                     binding.clearIcon.setOnClickListener {
         //                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
                     }
         */





            binding.root.setOnClickListener { view ->
                MainScope().launch {
                    val drawingObject =
                        downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(item.id)
                    drawingObject?.let {

                        val file = File(it.localUri)

                        if (file.exists()) {
                            fileClickListener?.invoke(view, absoluteAdapterPosition, item, it)
                        } else {
                            cancelAndMakeToast(
                                view.context,
                                "File not downloaded",
                                Toast.LENGTH_SHORT
                            )
                        }
                    } ?: kotlin.run {
                        cancelAndMakeToast(
                            view.context,
                            "File not downloaded",
                            Toast.LENGTH_SHORT
                        )
                    }
                }
            }

            binding.mainLayout.setOnClickListener { view ->
                MainScope().launch {
                    val drawingObject =
                        downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(item.id)
                    drawingObject?.let {

                        val file = File(it.localUri)

                        if (file.exists()) {
                            fileClickListener?.invoke(view, absoluteAdapterPosition, item, it)
                        } else {
                            cancelAndMakeToast(
                                view.context,
                                "File not downloaded",
                                Toast.LENGTH_SHORT
                            )
                        }
                    } ?: kotlin.run {
                        cancelAndMakeToast(
                            view.context,
                            "File not downloaded",
                            Toast.LENGTH_SHORT
                        )
                    }
                }
            }

            binding.fileName.setOnClickListener { view ->
                MainScope().launch {
                    val drawingObject =
                        downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(item.id)
                    drawingObject?.let {

                        val file = File(it.localUri)

                        if (file.exists()) {
                            fileClickListener?.invoke(view, absoluteAdapterPosition, item, it)
                        } else {
                            cancelAndMakeToast(
                                view.context,
                                "File not downloaded",
                                Toast.LENGTH_SHORT
                            )
                        }
                    } ?: kotlin.run {
                        cancelAndMakeToast(
                            view.context,
                            "File not downloaded",
                            Toast.LENGTH_SHORT
                        )
                    }
                }
            }
            binding.fileSize.setOnClickListener { view ->
                MainScope().launch {
                    val drawingObject =
                        downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(item.id)
                    drawingObject?.let {

                        val file = File(it.localUri)

                        if (file.exists()) {
                            fileClickListener?.invoke(view, absoluteAdapterPosition, item, it)
                        } else {
                            cancelAndMakeToast(
                                view.context,
                                "File not downloaded",
                                Toast.LENGTH_SHORT
                            )
                        }
                    } ?: kotlin.run {
                        cancelAndMakeToast(
                            view.context,
                            "File not downloaded",
                            Toast.LENGTH_SHORT
                        )
                    }
                }
            }


            MainScope().launch {
                val drawingObject =
                    downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(item.id)
                drawingObject?.let {

                    if (it.isDownloaded && it.localUri.isNotEmpty()) {
                        binding.ivDownloaded.visibility = View.VISIBLE
                        binding.tvDownloadProgress.visibility = View.GONE
                        binding.ivDownloadFile.visibility = View.GONE
                    } else if (it.downloading) {
                        binding.ivDownloaded.visibility = View.GONE
                        binding.tvDownloadProgress.visibility = View.VISIBLE
                        binding.ivDownloadFile.visibility = View.GONE
                        getDownloadProgress(
                            binding.tvDownloadProgress.context,
                            it.downloadId
                        ) { status, filepath, progress ->
                            MainScope().launch {
                                if (status.equals("downloaded", true)) {
                                    if (filepath.isNotEmpty()) {
                                        binding.ivDownloadFile.visibility =
                                            View.GONE
                                        binding.tvDownloadProgress.visibility =
                                            View.GONE
                                        binding.ivDownloaded.visibility =
                                            View.VISIBLE
                                        binding.tvDownloadProgress.text = progress
                                    } else {
                                        downloadedDrawingV2Dao.deleteByDrawingID(item.id)
                                        binding.ivDownloaded.visibility = View.GONE
                                        binding.tvDownloadProgress.visibility =
                                            View.GONE
                                        binding.ivDownloadFile.visibility =
                                            View.VISIBLE
                                    }
                                } else if (status == "retry" || status == "failed") {
                                    downloadedDrawingV2Dao.deleteByDrawingID(item.id)
                                    binding.tvDownloadProgress.text = "0%"
                                    binding.ivDownloaded.visibility = View.GONE
                                    binding.tvDownloadProgress.visibility = View.GONE
                                    binding.ivDownloadFile.visibility = View.VISIBLE
                                } else if (status == "downloading") {
                                    binding.ivDownloadFile.visibility = View.GONE
                                    binding.tvDownloadProgress.visibility = View.VISIBLE
                                    binding.tvDownloadProgress.text = progress
                                }
                            }
                        }
                    } else {
                        binding.ivDownloaded.visibility = View.GONE
                        binding.tvDownloadProgress.visibility = View.GONE
                        binding.ivDownloadFile.visibility = View.VISIBLE
                    }
                } ?: kotlin.run {
                    binding.ivDownloaded.visibility = View.GONE
                    binding.tvDownloadProgress.visibility = View.GONE
                    binding.ivDownloadFile.visibility = View.VISIBLE
                }
            }

            binding.ivDownloadFile.setOnClickListener {
                if (item.fileUrl.isEmpty()) {
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
                            binding.tvDownloadProgress.visibility = View.VISIBLE
                            downloadFileClickListener?.invoke(
                                binding.tvDownloadProgress,
                                binding.ivDownloadFile,
                                binding.ivDownloaded,
                                Triple(item.id, item.fileName, item.fileUrl),
                                ""
                            )

                            checkDownloadStatus(
                                item.id,
                                binding.ivDownloaded,
                                binding.tvDownloadProgress,
                                binding.ivDownloadFile,
                                position
                            ) {

                                notifyItemChanged(position)
                            }
                        } else {

                            requestPermissionClickListener?.invoke()
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


            val context = binding.uploadImg.context

            binding.fileName.text = item.fileName
            binding.fileSize.text = "File size: unknown"
            binding.clearIcon.visibility = View.GONE

        }
    }


    private fun getFileNameFromUri(uri: Uri): String? {
        val file = File(uri.path ?: "")
        return file.name
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
                            val fileAbsolutePath = copyFileToInternalStorage(it, uri, context)
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
                                    "100 %"
                                )
                            }
                        }

                        break
                    }

                    val downloadedPercent = ((bytesDownloaded * 100L) / bytesTotal).toInt()

                    println("StatusProgress %: $downloadedPercent")
                    println("StatusDownloaded: $bytesDownloaded")
                    println("StatusTotal: $bytesTotal")

                    itemClickListener?.invoke("downloading", "", "$downloadedPercent %")
                    if (bytesTotal > 0) {
                        println("Progress: " + ((bytesDownloaded * 100L) / bytesTotal).toInt())
                    }
                } else {
                    itemClickListener?.invoke("retry", "", "")
                    break
                }

                cursor.close()

                // Add a delay before the next iteration
                delay(500)
            }
        }
    }

    private fun copyFileToInternalStorage(
        fileName: String,
        uri: Uri,
        context: Context
    ): String? {
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

            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun checkDownloadStatus(
        id: String,
        ivDownloaded: AppCompatImageView,
        tvDownloadProgress: TextView,
        ivDownloadFile: AppCompatImageView,
        position: Int,
        callBack: (Int) -> Unit
    ) {
        MainScope().launch {
            delay(500)
            val drawingObject =
                downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(id)
            drawingObject?.let {

                if (it.isDownloaded && it.localUri.isNotEmpty()) {
                    ivDownloaded.visibility = View.VISIBLE
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
                                callBack.invoke(position)
                                if (filepath.isNotEmpty()) {
                                    ivDownloadFile.visibility =
                                        View.GONE
                                    tvDownloadProgress.visibility =
                                        View.GONE
                                    ivDownloaded.visibility =
                                        View.VISIBLE
                                    tvDownloadProgress.text = progress
                                } else {
                                    downloadedDrawingV2Dao.deleteByDrawingID(id)
                                    ivDownloaded.visibility = View.GONE
                                    tvDownloadProgress.visibility =
                                        View.GONE
                                    ivDownloadFile.visibility =
                                        View.VISIBLE
                                }
                            } else if (status == "retry" || status == "failed") {
                                downloadedDrawingV2Dao.deleteByDrawingID(id)
                                tvDownloadProgress.text = "0 %"
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
    }

}