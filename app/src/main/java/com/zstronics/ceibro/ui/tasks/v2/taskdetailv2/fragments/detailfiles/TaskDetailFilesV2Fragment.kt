package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailfiles

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.ahmadullahpk.alldocumentreader.activity.All_Document_Reader_Activity
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroDownloadDrawingV2
import com.zstronics.ceibro.data.database.models.tasks.LocalTaskDetailFiles
import com.zstronics.ceibro.databinding.FragmentTaskDetailFilesV2Binding
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawings.DrawingsV2Fragment
import com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailfiles.adapter.TaskDetailFilesAdapter
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class TaskDetailFilesV2Fragment :
    BaseNavViewModelFragment<FragmentTaskDetailFilesV2Binding, ITaskDetailFilesV2.State, TaskDetailFilesV2VM>() {
    private var manager: DownloadManager? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionList13 = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    private val permissionList10 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskDetailFilesV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_detail_files_v2
    override fun toolBarVisibility(): Boolean = false
    private val filesAdapterList = arrayOf("All", "Photos", "Links", "Files")
    override fun onClick(id: Int) {
        when (id) {
            R.id.tvAll -> {
                changeTaBackgroundColor(1)
                viewModel.allDetailFiles.value?.let {
                    if (it.isNotEmpty()) {
                        detailFilesAdapter.setList(it)
                    } else {
                        detailFilesAdapter.setList(mutableListOf())
                    }
                } ?: kotlin.run {
                    detailFilesAdapter.setList(mutableListOf())
                }
            }

            R.id.tvPhotos -> {
                changeTaBackgroundColor(2)
                viewModel.photoFiles.value?.let {
                    if (it.isNotEmpty()) {
                        detailFilesAdapter.setList(it)
                    } else {
                        detailFilesAdapter.setList(mutableListOf())
                    }
                } ?: kotlin.run {
                    detailFilesAdapter.setList(mutableListOf())
                }

            }

            R.id.tvLinks -> {
                changeTaBackgroundColor(3)

            }

            R.id.tvDocuments -> {
                changeTaBackgroundColor(4)
                viewModel.documentFiles.value?.let {
                    if (it.isNotEmpty()) {
                        detailFilesAdapter.setList(it)
                    } else {
                        detailFilesAdapter.setList(mutableListOf())
                    }
                } ?: kotlin.run {
                    detailFilesAdapter.setList(mutableListOf())
                }
            }
        }
    }


    private lateinit var detailFilesAdapter: TaskDetailFilesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        manager =
            mViewDataBinding.root.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filesAdapterList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mViewDataBinding.filesSpinner.adapter = adapter


        mViewDataBinding.filesSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    shortToastNow(filesAdapterList[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        detailFilesAdapter = TaskDetailFilesAdapter(
            networkConnectivityObserver,
            requireContext(),
            viewModel.downloadedDrawingV2Dao
        )
        detailFilesAdapter.downloadFileCallBack { data, tag ->
            checkDownloadFilePermission(data, viewModel.downloadedDrawingV2Dao) {
                /*MainScope().launch {
                    if (it.trim().equals("100%", true)) {

                    } else if (it == "retry" || it == "failed") {

                    } else {

                    }
                }*/
            }
        }
        detailFilesAdapter.fileClickListener =
            { position: Int, drawingFile ->
                val bundle = Bundle()
//                bundle.putParcelable("eventFile", data)
                bundle.putParcelable("downloadedFile", drawingFile)

                val file = File(drawingFile.localUri)
                val fileUri = Uri.fromFile(file)
                val fileDetails = getPickedFileDetail(requireContext(), fileUri)
                if (fileDetails.attachmentType == AttachmentTypes.Pdf) {
                    navigate(R.id.fileViewerFragment, bundle)
                } else {
                    openFile(file, requireContext())
                    //    shortToastNow("File format not supported yet.")
                }
            }

        detailFilesAdapter.imageClickListener =
            { position: Int, item: LocalTaskDetailFiles ->
                var newPosition = position
                val allPhotos = viewModel.photoFiles.value
                if (allPhotos != null) {
                    val foundImage = allPhotos.find { it.fileId == item.fileId }
                    if (foundImage != null) {
                        val index = allPhotos.indexOf(foundImage)
                        newPosition = index

                        val bundle = Bundle()
                        bundle.putParcelableArray("images", allPhotos.toTypedArray())
                        bundle.putInt("position", newPosition)
                        bundle.putBoolean("fromDetailView", true)
                        navigate(R.id.imageViewerFragment, bundle)

                    }
                } else {
                    shortToastNow("Unable to open this image")
                }

            }

        detailFilesAdapter.requestPermissionCallBack {
            checkDownloadFilePermission()
        }

        mViewDataBinding.filesRV.adapter = detailFilesAdapter


        viewModel.allDetailFiles.observe(viewLifecycleOwner) {
            changeTaBackgroundColor(1)
            if (!it.isNullOrEmpty()) {
                detailFilesAdapter.setList(it)
            } else {
                detailFilesAdapter.setList(mutableListOf())
            }
        }

        viewModel.photoFiles.observe(viewLifecycleOwner) {

        }

        viewModel.documentFiles.observe(viewLifecycleOwner) {

        }

    }

    private fun changeTaBackgroundColor(index: Int) {
        val selectedBackground = R.drawable.signin_button_back
        val selectedTint = R.color.appBlue
        val selectedTextColor = R.color.white

        val unselectedBackground = R.drawable.signin_button_back
        val unselectedTint = R.color.appGrey1
        val unselectedTextColor = R.color.appGrey3

        when (index) {
            1 -> {

                applyBackgroundTintTextColors(
                    mViewDataBinding.tvAll,
                    selectedBackground,
                    selectedTint,
                    selectedTextColor
                )

                applyBackgroundTintTextColors(
                    mViewDataBinding.tvPhotos,
                    selectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvLinks,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvDocuments,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )

            }

            2 -> {
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvAll,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvPhotos,
                    selectedBackground,
                    selectedTint,
                    selectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvLinks,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvDocuments,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )

            }

            3 -> {
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvAll,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvPhotos,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )

                applyBackgroundTintTextColors(
                    mViewDataBinding.tvLinks,
                    selectedBackground,
                    selectedTint,
                    selectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvDocuments,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
            }

            4 -> {
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvAll,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvPhotos,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvLinks,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )

                applyBackgroundTintTextColors(
                    mViewDataBinding.tvDocuments,
                    selectedBackground,
                    selectedTint,
                    selectedTextColor
                )
            }
        }
    }

    private fun applyBackgroundTintTextColors(
        textView: TextView,
        backgroundRes: Int,
        tintRes: Int,
        textRes: Int
    ) {
        textView.setBackgroundResource(backgroundRes)
        textView.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), tintRes)
        )
        textView.setTextColor(ContextCompat.getColor(requireContext(), textRes))
    }

    private fun checkDownloadFilePermission(
        url: Triple<String, String, String>,
        downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
        itemClickListener: ((tag: String) -> Unit)?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkPermissions(permissionList13)) {
                downloadFile(url, downloadedDrawingV2Dao) {
                    itemClickListener?.invoke(it)
                }
            } else {
                requestPermissions(permissionList13)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {

            downloadFile(url, downloadedDrawingV2Dao) {
                itemClickListener?.invoke(it)
            }
        } else {
            if (checkPermissions(permissionList10)) {
                downloadFile(url, downloadedDrawingV2Dao) {
                    itemClickListener?.invoke(it)
                }
            } else {

                downloadFile(url, downloadedDrawingV2Dao) {
                    itemClickListener?.invoke(it)
                }
                requestPermissions(permissionList10)
            }
        }
    }

    private fun checkDownloadFilePermission(

    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(permissionList13)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {


        } else {
            requestPermissions(permissionList10)
        }
    }

    private fun checkPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(
            requireActivity(), permissions,
            DrawingsV2Fragment.permissionRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == DrawingsV2Fragment.permissionRequestCode) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted
            } else {
                handleDeniedPermissions(permissions, grantResults)
            }
        }
    }

    private fun handleDeniedPermissions(permissions: Array<out String>, grantResults: IntArray) {
        for (i in permissions.indices) {
            val permission = permissions[i]
            val result = grantResults[i]

            if (result == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    showToast("Permission denied: $permission")
                } else {
                    showToast("Permission denied: $permission. Please enable it in the app settings.")
                    navigateToAppSettings(context)
                    return
                }
            }
        }
    }

    private fun navigateToAppSettings(context: Context?) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", context?.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun downloadFile(
        triplet: Triple<String, String, String>,
        downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
        itemClickListener: ((tag: String) -> Unit)?
    ) {
        shortToastNow("Downloading file...")
        val uri = Uri.parse(triplet.third)
        val fileName = triplet.second
        val folder = File(
            context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            DrawingsV2Fragment.folderName
        )
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val destinationUri = Uri.fromFile(File(folder, fileName))

        val request: DownloadManager.Request? =
            DownloadManager
                .Request(uri)
                .setDestinationUri(destinationUri)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setVisibleInDownloadsUi(true)

        val downloadId = manager?.enqueue(request)

        println("progress id   $downloadId")
        println("progress   $manager")

        val ceibroDownloadDrawingV2 = downloadId?.let {
            CeibroDownloadDrawingV2(
                fileName = triplet.second,
                downloading = true,
                isDownloaded = false,
                downloadId = it,
                drawing = null,
                drawingId = triplet.first,
                groupId = "",
                localUri = ""
            )
        }


        GlobalScope.launch {
            ceibroDownloadDrawingV2?.let {
                downloadedDrawingV2Dao.insertDownloadDrawing(it)
                println("progress  object  $it")
            }

        }

        Handler(Looper.getMainLooper()).postDelayed({
            getDownloadProgress(context, downloadId!!) {
                GlobalScope.launch(Dispatchers.Main) {
                    if (it == "retry" || it == "failed") {
                        downloadedDrawingV2Dao.deleteByDrawingID(downloadId.toString())
                    } else if (it.trim().equals("100%", true)) {

                        shortToastNow("Downloaded")
                    }
                }
                itemClickListener?.invoke(it)
            }
        }, 1000)

        println("id: ${id} Folder name: ${folder} uri:${uri} destinationUri:${destinationUri}")

    }

    @SuppressLint("Range")
    private fun getDownloadProgress(
        context: Context?,
        downloadId: Long,
        itemClickListener: ((tag: String) -> Unit)?
    ) {
        GlobalScope.launch {
            while (true) {
                println("progress : checking")
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

                    println("progress status : $status")

                    if (status.toInt() == DownloadManager.STATUS_FAILED) {

                        println("Status failed: $status")
                        itemClickListener?.invoke("failed")
                        break
                    } else if (status.toInt() == DownloadManager.STATUS_SUCCESSFUL) {
                        itemClickListener?.invoke("100%")
                        break
                    }

                    val downloadedPercent = ((bytesDownloaded * 100L) / bytesTotal).toInt()

                    println("StatusProgress %: $downloadedPercent")
                    println("StatusDownloaded: $bytesDownloaded")
                    println("StatusTotal: $bytesTotal")

                    itemClickListener?.invoke("$downloadedPercent %")
                    if (bytesTotal > 0) {
                        println("Progress: " + ((bytesDownloaded * 100L) / bytesTotal).toInt())
                        println("progress downloaded" + ((bytesDownloaded * 100L) / bytesTotal).toInt())
                    }
                } else {
                    println("progress retry ")
                    itemClickListener?.invoke("retry")
                    break
                }
                cursor.close()

                delay(500)
            }
        }
    }

    private fun getPickedFileDetail(context: Context, fileUri: Uri?): PickedImages {
        val mimeType = FileUtils.getMimeType(context, fileUri)
        val fileName = FileUtils.getFileName(context, fileUri)
        val fileSize = FileUtils.getFileSizeInBytes(context, fileUri)
        val fileSizeReadAble = FileUtils.getReadableFileSize(fileSize)
        println("mimeTypeFound: ${mimeType} - ${fileName}")
        val attachmentType = when {
            mimeType == null -> {
                AttachmentTypes.Doc
            }

            mimeType == "application/pdf" -> {
                AttachmentTypes.Pdf
            }

            mimeType == "application/x-rar-compressed" || mimeType == "application/zip" -> {
                AttachmentTypes.Zip
            }

            mimeType.equals("text/plain", true) ||
                    mimeType.equals("text/csv", true) ||
                    mimeType.equals("application/rtf", true) ||
                    mimeType.equals("application/zip", true) ||
                    mimeType.equals("application/x-rar-compressed", true) ||
                    mimeType.equals("application/vnd.oasis.opendocument.text", true) ||
                    mimeType.equals("application/vnd.oasis.opendocument.spreadsheet", true) ||
                    mimeType.equals("application/vnd.oasis.opendocument.presentation", true) ||
                    mimeType.equals("application/vnd.android.package-archive", true) ||
                    mimeType.equals("application/msword", true) ||
                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        true
                    ) ||
                    mimeType.equals(
                        "application/vnd.ms-word.document.macroEnabled.12",
                        true
                    ) ||
                    mimeType.equals("application/vnd.ms-excel", true) ||
                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        true
                    ) ||
                    mimeType.equals(
                        "application/vnd.ms-excel.sheet.macroEnabled.12",
                        true
                    ) ||
                    mimeType.equals("application/vnd.ms-powerpoint", true) ||
                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        true
                    ) ||
                    mimeType.equals(
                        "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
                        true
                    ) ||
                    mimeType.equals(
                        "image/vnd.dwg",
                        true
                    ) ||
                    mimeType.equals(
                        "application/acad",
                        true
                    ) -> {
                AttachmentTypes.Doc
            }

            mimeType.contains("image/vnd") -> {
                AttachmentTypes.Doc
            }

            mimeType.startsWith("image") -> {
                AttachmentTypes.Image
            }

            mimeType.startsWith("video") -> {
                AttachmentTypes.Video
            }

            else -> AttachmentTypes.Doc
        }
        return PickedImages(
            fileUri = fileUri,
            attachmentType = attachmentType,
            fileName = fileName,
            fileSizeReadAble = fileSizeReadAble,
            file = FileUtils.getFile(requireContext(), fileUri)
        )
    }

    private fun openFile(file: File, context: Context) {

        val intent = Intent(context, All_Document_Reader_Activity::class.java)
        intent.putExtra("path", file.absolutePath)
        intent.putExtra("fromAppActivity", true)
        context.startActivity(intent)
        return

    }
}