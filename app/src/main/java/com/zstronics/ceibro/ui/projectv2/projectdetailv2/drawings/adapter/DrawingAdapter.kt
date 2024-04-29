package com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawings.adapter


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
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.base.extensions.cancelAndMakeToast
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.databinding.DrawingDetailItemListBinding
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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private val permissionList13 = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
private val permissionList10 = arrayOf(
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE
)

class DrawingAdapter constructor(
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
    val networkConnectivityObserver: NetworkConnectivityObserver
) :
    RecyclerView.Adapter<DrawingAdapter.NewDrawingGroupViewHolder>() {

    var listItems: ArrayList<DrawingV2> = ArrayList()

    var popupMenu: PopupMenu? = null
    private var isPopupMenuShowing = false
    var drawingFileClickListener: ((view: View, data: DrawingV2, absolutePath: String) -> Unit)? =
        null

    fun drawingFileClickListenerCallBack(itemClickListener: ((view: View, data: DrawingV2, absolutePath: String) -> Unit)?) {
        this.drawingFileClickListener = itemClickListener
    }

    var downloadFileClickListener: ((view: TextView, ivDownloadFile: AppCompatImageView, ivDownloaded: AppCompatImageView, data: DrawingV2, tag: String) -> Unit)? =
        null

    fun downloadFileCallBack(itemClickListener: ((view: TextView, ivDownload: AppCompatImageView, iv: AppCompatImageView, data: DrawingV2, tag: String) -> Unit)?) {
        this.downloadFileClickListener = itemClickListener
    }

    var requestPermissionClickListener: ((tag: String) -> Unit)? = null

    fun requestPermissionCallBack(requestPermissionClickListener: (tag: String) -> Unit) {
        this.requestPermissionClickListener = requestPermissionClickListener
    }

    var publicGroupClickListener: ((tag: String, CeibroGroupsV2?) -> Unit)? = null

    fun publicGroupCallBack(publicGroupClickListener: (tag: String, CeibroGroupsV2?) -> Unit) {
        this.publicGroupClickListener = publicGroupClickListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewDrawingGroupViewHolder {
        return NewDrawingGroupViewHolder(
            DrawingDetailItemListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NewDrawingGroupViewHolder, position: Int) {
        holder.bind(listItems[position], position)
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<DrawingV2>) {
        this.listItems.clear()
        if (list != null) {
            this.listItems.addAll(list.toMutableList())
        }
        notifyDataSetChanged()
    }


    fun updateItem(item: DrawingV2) {
        val allItems = this.listItems
        val foundItem = allItems.find { it._id == item._id }
        if (foundItem != null) {
            val index = allItems.indexOf(foundItem)
            allItems[index] = item
//            this.listItems.clear()
            this.listItems = allItems
            notifyDataSetChanged()
        }
    }

    fun deleteItem(groupId: String) {
        val allItems = this.listItems
        val foundItem = allItems.find { it._id == groupId }
        if (foundItem != null) {
            val index = allItems.indexOf(foundItem)
            allItems.removeAt(index)
//            this.listItems.clear()
            this.listItems = allItems
            notifyDataSetChanged()
        }
    }

    inner class NewDrawingGroupViewHolder(private val binding: DrawingDetailItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val context = binding.root.context

        fun bind(data: DrawingV2, position: Int) {


            binding.apply {

                tvSample.text = "${data.fileName}"
                tvFloor.text = "${data.floor.floorName} Floor"
                root.setOnClickListener { view ->
                    MainScope().launch {
                        val drawingObject =
                            downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(data._id)
                        drawingObject?.let {


                            val file = File(it.localUri)
                            if (file.exists()) {
                                drawingFileClickListener?.invoke(view, data, it.localUri)
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
                        downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(data._id)
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
                                        notifyItemChanged(position)

                                        if (filepath.isNotEmpty()) {
                                            ivDownloadFile.visibility =
                                                View.GONE
                                            tvDownloadProgress.visibility =
                                                View.GONE
                                            ivDownloaded.visibility =
                                                View.VISIBLE
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
                                                            notifyItemChanged(position)
                                                            if (filepath.isNotEmpty()) {
                                                                ivDownloadFile.visibility =
                                                                    View.GONE
                                                                tvDownloadProgress.visibility =
                                                                    View.GONE
                                                                ivDownloaded.visibility =
                                                                    View.VISIBLE
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

                // Add a delay before the next iteration
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
//
//
//        try {val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
//            val outputStream: FileOutputStream
//            // Create a file in the internal storage
//            val file = File(context.filesDir, fileName)
//            outputStream = FileOutputStream(file)
//
//            // Copy the content of the input stream to the output stream
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