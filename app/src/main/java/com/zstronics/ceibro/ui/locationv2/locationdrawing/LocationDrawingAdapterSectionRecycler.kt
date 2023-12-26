package com.zstronics.ceibro.ui.locationv2.locationdrawing

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.cancelAndMakeToast
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.databinding.LayoutItemHeaderBinding
import com.zstronics.ceibro.databinding.LayoutlocationdrawingitemlistingBinding
import com.zstronics.ceibro.databinding.LayoutlocationdrawinglistBinding
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class LocationDrawingAdapterSectionRecycler constructor(
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
    val context: Context,
    sectionList: MutableList<LocationDrawingSectionHeader>,
    val networkConnectivityObserver: NetworkConnectivityObserver
) : SectionRecyclerViewAdapter<
        LocationDrawingSectionHeader,
        CeibroGroupsV2,
        LocationDrawingAdapterSectionRecycler.ConnectionsSectionViewHolder,
        LocationDrawingAdapterSectionRecycler.ConnectionsChildViewHolder>(
    context,
    sectionList
) {
    var popupMenu: PopupMenu? = null
    private var isPopupMenuShowing = false
    var drawingFileClickListener: ((view: View, data: DrawingV2, tag: String) -> Unit)? =
        null

    fun setCallBack(itemClickListener: ((view: View, data: DrawingV2, tag: String) -> Unit)?) {
        this.drawingFileClickListener = itemClickListener
    }

    var downloadFileClickListener: ((view: TextView, ivDownloadFile: AppCompatImageView, ivDownloaded: AppCompatImageView, data: DrawingV2, tag: String) -> Unit)? =
        null

    fun downloadFileCallBack(itemClickListener: ((view: TextView, ivDownload: AppCompatImageView, iv: AppCompatImageView, data: DrawingV2, tag: String) -> Unit)?) {
        this.downloadFileClickListener = itemClickListener
    }

    override fun onCreateSectionViewHolder(
        sectionViewGroup: ViewGroup?,
        viewType: Int
    ): ConnectionsSectionViewHolder {
        return ConnectionsSectionViewHolder(
            LayoutItemHeaderBinding.inflate(
                LayoutInflater.from(context),
                sectionViewGroup,
                false
            )
        )
    }

    override fun onBindSectionViewHolder(
        connectionsSectionViewHolder: ConnectionsSectionViewHolder?,
        sectionPosition: Int,
        connectionsSectionHeader: LocationDrawingSectionHeader?
    ) {
        connectionsSectionViewHolder?.bind(connectionsSectionHeader)
    }

    override fun onCreateChildViewHolder(
        childViewGroup: ViewGroup?,
        viewType: Int
    ): ConnectionsChildViewHolder {
        return ConnectionsChildViewHolder(
            LayoutlocationdrawinglistBinding.inflate(
                LayoutInflater.from(context),
                childViewGroup,
                false
            )
        )
    }

    override fun onBindChildViewHolder(
        holder: ConnectionsChildViewHolder?,
        sectionPosition: Int,
        childPostitoin: Int,
        p3: CeibroGroupsV2?
    ) {
        holder?.bind(p3)
    }


    inner class ConnectionsSectionViewHolder constructor(private val binding: LayoutItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LocationDrawingSectionHeader?) {
            binding.headerTitle.text = item?.getSectionText()
            binding.headerTitle.textSize = 14f
            binding.headerTitle.setTextColor(context.getColor(R.color.appGrey3))

            if (item?.childItems.isNullOrEmpty()) {
                binding.headerTitle.visibility = View.GONE
            } else {
                binding.headerTitle.visibility = View.VISIBLE
            }
        }
    }

    inner class ConnectionsChildViewHolder constructor(val binding: LayoutlocationdrawinglistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CeibroGroupsV2?) {


            binding.apply {
                ivOptions.visibility = View.GONE
                ivDownload.visibility = View.GONE
                ivFav.visibility = View.GONE
                viewOne.visibility = View.GONE



                groupLayout.setOnClickListener {
                    if (llParent.visibility == View.VISIBLE) {
                        ivDropDown.setImageResource(R.drawable.icon_drop_down)
                        llParent.visibility = View.GONE
                    } else {
                        ivDropDown.setImageResource(R.drawable.arrow_drop_up)
                        llParent.visibility = View.VISIBLE
                    }
                }

                tvGroupName.text = "${item?.groupName} (${item?.drawings?.size ?: "0"})"
                tvGroupBy.text = "From: ${item?.creator?.firstName} ${item?.creator?.surName}"



                llParent.removeAllViews()

                item?.drawings?.forEachIndexed { index, data ->


                    val itemViewBinding: LayoutlocationdrawingitemlistingBinding =
                        DataBindingUtil.inflate(
                            LayoutInflater.from(binding.root.context),
                            R.layout.layoutlocationdrawingitemlisting,
                            binding.llParent,
                            false
                        )

                    itemViewBinding.apply {

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
                            val file = File(data.fileName)
                            if (file.exists()) {

                            } else {
                                if (networkConnectivityObserver.isNetworkAvailable()) {
                                    if (data.fileUrl.isNotEmpty()) {
                                        it.visibility = View.GONE
                                        tvDownloadProgress.visibility = View.VISIBLE
                                        downloadFileClickListener?.invoke(
                                            tvDownloadProgress,
                                            ivDownloadFile,
                                            ivDownloaded,
                                            data,
                                            ""
                                        )
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

                        binding.llParent.addView(itemViewBinding.root)
                    }
                }
            }
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

    private fun popUpMenu(v: View): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_location_group_menu, null)

        //following code is to make popup at top if the view is at bottom
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view

        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        //ShowAsDropDown statement at bottom, according to the view visibilities
        //////////////////////
        popupWindow.showAsDropDown(v, 0, 5)

//        val editTask = view.findViewById<View>(R.id.editTask)
//        val deleteTask = view.findViewById<View>(R.id.deleteTask)


//        if (positionOfIcon > height) {
//            if (deleteTask.visibility == View.GONE) {
//                popupWindow.showAsDropDown(v, -135, -245)
//            }
//            else {
//                popupWindow.showAsDropDown(v, -170, -405)
//            }
//        } else {
//            popupWindow.showAsDropDown(v, 0, 5)
//        }
        return popupWindow
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

    private fun getFileNameFromUri(uri: Uri): String? {
        val file = File(uri.path ?: "")
        return file.name
    }
}