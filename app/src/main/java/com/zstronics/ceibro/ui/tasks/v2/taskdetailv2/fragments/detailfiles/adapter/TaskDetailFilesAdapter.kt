package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailfiles.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroDownloadDrawingV2
import com.zstronics.ceibro.data.database.models.tasks.LocalTaskDetailFiles
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.databinding.LayoutCeibroTaskDetailFilesBinding
import com.zstronics.ceibro.ui.attachment.imageExtensions
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import com.zstronics.ceibro.utils.DateUtils
import ee.zstronics.ceibro.camera.cancelAndMakeToast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class TaskDetailFilesAdapter constructor(
    val networkConnectivityObserver: NetworkConnectivityObserver,
    val context: Context,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
) :
    RecyclerView.Adapter<TaskDetailFilesAdapter.FilesViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: LocalTaskDetailFiles) -> Unit)? =
        null
    var listItems: MutableList<LocalTaskDetailFiles> = mutableListOf()


    var fileClickListener: ((view: View, position: Int, data: LocalTaskDetailFiles, downloadedData: CeibroDownloadDrawingV2) -> Unit)? =
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
            LayoutCeibroTaskDetailFilesBinding.inflate(
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

    fun setList(list: MutableList<LocalTaskDetailFiles>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class FilesViewHolder(private val binding: LayoutCeibroTaskDetailFilesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LocalTaskDetailFiles, position: Int) {
            val context = binding.fileName.context

            binding.apply {
                fileName.text = item.fileName
                fileSenderName.text = "${item.uploadedBy.firstName} ${item.uploadedBy.surName}"
                fileSize.text = "Size: unknown"
                fileCreatedAt.text =
                    DateUtils.formatCreationUTCTimeToCustomForDetailFiles(
                        utcTime = item.createdAt,
                        inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
                    )

//                val fileExtension = item.fileUrl.substringAfterLast(".")
                val fileExtension = getFileUrlExtension(item.fileUrl)

                if (isImageExtension(fileExtension)) {
                    val circularProgressDrawable = CircularProgressDrawable(context)
                    circularProgressDrawable.strokeWidth = 4f
                    circularProgressDrawable.centerRadius = 14f
                    circularProgressDrawable.start()

                    val requestOptions = RequestOptions()
                        .placeholder(circularProgressDrawable)
                        .error(R.drawable.icon_corrupted)
                        .skipMemoryCache(true)
                        .centerCrop()

                    Glide.with(context)
                        .load(item.fileUrl)
                        .apply(requestOptions)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                circularProgressDrawable.stop()
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                circularProgressDrawable.stop()
                                return false
                            }
                        })
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.fileImage)
                    binding.fileImage.scaleType = ImageView.ScaleType.CENTER_CROP

                } else {
                    if (fileExtension.equals("pdf", true)) {
                        binding.fileImage.setImageResource(R.drawable.icon_pdf)
                        binding.fileImage.scaleType = ImageView.ScaleType.FIT_CENTER
                    } else if (fileExtension.equals("odt", true) || fileExtension.equals(
                            "odp",
                            true
                        ) ||
                        fileExtension.equals("docx", true) || fileExtension.equals("doc", true) ||
                        fileExtension.equals("xlsx", true) || fileExtension.equals("xls", true) ||
                        fileExtension.equals("pptx", true) || fileExtension.equals("ppt", true)
                    ) {
                        binding.fileImage.setImageResource(R.drawable.icon_doc)
                        binding.fileImage.scaleType = ImageView.ScaleType.FIT_CENTER
                    } else {
                        binding.fileImage.setImageResource(R.drawable.icon_corrupted)
                        binding.fileImage.scaleType = ImageView.ScaleType.FIT_CENTER
                    }
                }
            }

            binding.root.setOnClickListener { view ->
//                MainScope().launch {
//                    val drawingObject =
//                        downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(item.fileID)
//                    drawingObject?.let {
//
//                        val file = File(it.localUri)
//
//                        if (file.exists()) {
//                            fileClickListener?.invoke(view, absoluteAdapterPosition, item, it)
//                        } else {
//                            cancelAndMakeToast(
//                                view.context,
//                                "File not downloaded",
//                                Toast.LENGTH_SHORT
//                            )
//                        }
//                    } ?: kotlin.run {
//                        cancelAndMakeToast(
//                            view.context,
//                            "File not downloaded",
//                            Toast.LENGTH_SHORT
//                        )
//                    }
//                }
                cancelAndMakeToast(
                    view.context,
                    "Coming soon",
                    Toast.LENGTH_SHORT
                )
            }

            MainScope().launch {
                /*val drawingObject =
                    downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(item.fileID)
                drawingObject?.let {

                    if (it.isDownloaded && it.localUri.isNotEmpty()) {
                        binding.ivDownloaded.visibility = View.VISIBLE
                        binding.tvDownloadProgress.visibility = View.GONE
                        //    binding.ivDownloadFile.visibility = View.GONE
                    } else if (it.downloading) {
                        binding.ivDownloaded.visibility = View.GONE
                        binding.tvDownloadProgress.visibility = View.VISIBLE
                        //binding.ivDownloadFile.visibility = View.GONE
                        getDownloadProgress(
                            binding.tvDownloadProgress.context,
                            it.downloadId
                        ) { status, filepath, progress ->
                            MainScope().launch {
                                if (status.equals("downloaded", true)) {
                                    if (filepath.isNotEmpty()) {
                                        //        binding.ivDownloadFile.visibility =
                                        View.GONE
                                        binding.tvDownloadProgress.visibility =
                                            View.GONE
                                        binding.ivDownloaded.visibility =
                                            View.VISIBLE
                                        binding.tvDownloadProgress.text = progress
                                    } else {
                                        downloadedDrawingV2Dao.deleteByDrawingID(item.fileID)
                                        binding.ivDownloaded.visibility = View.GONE
                                        binding.tvDownloadProgress.visibility =
                                            View.GONE
                                        //  binding.ivDownloadFile.visibility = View.VISIBLE
                                    }
                                } else if (status == "retry" || status == "failed") {
                                    downloadedDrawingV2Dao.deleteByDrawingID(item.fileID)
                                    binding.tvDownloadProgress.text = "0%"
                                    binding.ivDownloaded.visibility = View.GONE
                                    binding.tvDownloadProgress.visibility = View.GONE
                                    //     binding.ivDownloadFile.visibility = View.VISIBLE
                                } else if (status == "downloading") {
                                    // binding.ivDownloadFile.visibility = View.GONE
                                    binding.tvDownloadProgress.visibility = View.VISIBLE
                                    binding.tvDownloadProgress.text = progress
                                }
                            }
                        }
                    } else {
                        binding.ivDownloaded.visibility = View.GONE
                        binding.tvDownloadProgress.visibility = View.GONE
                        //    binding.ivDownloadFile.visibility = View.VISIBLE
                    }
                } ?: kotlin.run {
                    binding.ivDownloaded.visibility = View.GONE
                    binding.tvDownloadProgress.visibility = View.GONE
                    //  binding.ivDownloadFile.visibility = View.VISIBLE
                }*/
            }

            /* binding.ivDownloadFile.setOnClickListener {
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

*/


            binding.ivDots.setOnClickListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    createPopupWindow(it, null) { tag, data ->
                        if (tag == "delete") {

                        } else if (tag == "rename") {

                        }
                    }
                }, 200)
            }
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

    private fun createPopupWindow(
        v: View,
        groupResponseV2: CeibroConnectionGroupV2?,
        callback: (String, CeibroConnectionGroupV2) -> Unit
    ): PopupWindow {
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.task_detail_files_menu_dialog, null)

        val popupWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true


        val tvFileLocation: TextView = view.findViewById(R.id.tvFileLocation)
        val tvShare: TextView = view.findViewById(R.id.tvShare)
        val tvDownload: TextView = view.findViewById(R.id.tvShare)


        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        if (positionOfIcon > height) {
            popupWindow.showAsDropDown(v, -200, -170)
        } else {
            popupWindow.showAsDropDown(v, -205, -60)
        }


        return popupWindow
    }

    private fun isImageExtension(extension: String): Boolean {
        val imageExtensions = listOf("png", "jpg", "jpeg", "gif", "bmp", "webp", "heic")
        return extension.lowercase() in imageExtensions
    }

    private fun getFileUrlExtension(url: String): String {
        val lastDotIndex = url.lastIndexOf('.')
        return if (lastDotIndex != -1) {
            url.substring(lastDotIndex + 1)
        } else {
            ""
        }
    }

}