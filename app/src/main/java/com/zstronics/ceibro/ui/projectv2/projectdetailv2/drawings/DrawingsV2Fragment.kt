package com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.view.View
import android.widget.SearchView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.extensions.isVisible
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.extensions.showKeyboard
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterVM
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.databinding.FragmentDrawingsV2Binding
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.projectv2.newprojectv2.AddNewPhotoBottomSheet
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@AndroidEntryPoint
class DrawingsV2Fragment :
    BaseNavViewModelFragment<FragmentDrawingsV2Binding, IDrawingV2.State, DrawingsV2VM>() {
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: DrawingsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_drawings_v2
    override fun toolBarVisibility(): Boolean = false
    private lateinit var downloadCompleteReceiver: DownloadCompleteReceiver
    private var manager: DownloadManager? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionList13 = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    private val permissionList10 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private lateinit var sectionedAdapter: AllDrawingsAdapterSectionRecycler

    private val cookiesViewModel: NavHostPresenterVM by viewModels()


    var drawingFileClickListener: ((view: View, data: DrawingV2, absolutePath: String) -> Unit)? =
        null

    private var fragmentManager: FragmentManager? = null

    override fun onClick(id: Int) {
        when (id) {
            R.id.projectFilterBtn -> {
            }

            R.id.addNewDrawingBtn -> {

                fragmentManager?.let {
                    chooseFile(it) { fromLocation ->
                        if (fromLocation.equals("local", true)) {

                            chooseDocuments(
                                mimeTypes = arrayOf(
                                    "application/pdf"
                                )
                            )
                        } else {
                            shortToastNow("Coming Soon")
                        }
                    }
                }
            }

            R.id.clAddNewDrawing -> {

                fragmentManager?.let {
                    chooseFile(it) { fromLocation ->
                        if (fromLocation.equals("local", true)) {

                            chooseDocuments(
                                mimeTypes = arrayOf(
                                    "application/pdf"
                                )
                            )
                        } else {
                            shortToastNow("Coming Soon")
                        }
                    }
                }
            }

            R.id.cancelSearch -> {
                mViewDataBinding.projectSearchBar.hideKeyboard()
                mViewDataBinding.projectsSearchCard.visibility = View.GONE
                mViewDataBinding.projectSearchBtn.visibility = View.VISIBLE
                mViewDataBinding.projectSearchBar.setQuery("", false)
                //   sharedViewModel?.projectSearchQuery?.postValue("")
            }

            R.id.projectSearchBtn -> {
                showKeyboard()
                mViewDataBinding.projectSearchBar.requestFocus()
                mViewDataBinding.projectsSearchCard.visibility = View.VISIBLE
                mViewDataBinding.projectSearchBtn.visibility = View.GONE

            }
        }
    }


    private var sectionList: MutableList<DrawingSectionHeader> = mutableListOf()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentManager = childFragmentManager

        mainActivityDownloader = mViewDataBinding.root.context

        manager =
            mViewDataBinding.root.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadCompleteReceiver = DownloadCompleteReceiver()
        mainActivityDownloader?.registerReceiver(
            downloadCompleteReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )


        sectionList.add(
            0,
            DrawingSectionHeader(
                mutableListOf(),
                getString(R.string.favorite_projects)
            )
        )
        sectionList.add(
            1,
            DrawingSectionHeader(
                mutableListOf(),
                getString(R.string.my_groups)
            )
        )
        sectionList.add(
            2,
            DrawingSectionHeader(
                mutableListOf(),
                getString(R.string.other_groups)
            )
        )


        sectionedAdapter = AllDrawingsAdapterSectionRecycler(
            requireContext(),
            sectionList,
            viewModel.downloadedDrawingV2Dao,
            networkConnectivityObserver
        )
        sectionedAdapter.deleteClickListener = { item ->
            viewModel.deleteGroupByID(item._id)
        }
        referenceSectionedAdapter = sectionedAdapter


        sectionedAdapter.drawingFileClickListenerCallBack { view, data, absolutePath ->
            println("data.uploaderLocalFilePath1: ${data.fileName}")
            drawingFileClickListener?.invoke(view, data, absolutePath)
            //   checkDownloadFilePermission(data.fileUrl)
        }
        sectionedAdapter.downloadFileCallBack { tv, ivDownloadFile, ivDownloaded, data, tag ->
            checkDownloadFilePermission(data, viewModel.downloadedDrawingV2Dao) {
                MainScope().launch {
                    if (it.trim().equals("100%", true)) {
                        //  sectionedAdapter.notifyDataSetChanged()
                        tv.visibility = View.GONE
                        ivDownloaded.visibility = View.VISIBLE
                        tv.text = it
                    } else if (it == "retry" || it == "failed") {
                        ivDownloaded.visibility = View.GONE
                        tv.visibility = View.GONE
                        ivDownloadFile.visibility = View.VISIBLE
                    } else {
                        tv.text = it
                    }
                }
            }
        }


        sectionedAdapter.requestPermissionCallBack {
            checkDownloadFilePermission()
        }

        sectionedAdapter.publicGroupCallBack { tag, group ->
            if (group != null) {
                viewModel.publicOrPrivateGroup(group)
            }
        }

        mViewDataBinding.projectSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {

                    viewModel.filterMyGroups(query.trim())
                    viewModel.filterFavouriteGroups(query.trim())
                    viewModel.filterOtherGroups(query.trim())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {

                    viewModel.filterMyGroups(newText.trim())
                    viewModel.filterFavouriteGroups(newText.trim())
                    viewModel.filterOtherGroups(newText.trim())

                }
                return true
            }
        })


        val linearLayoutManager = LinearLayoutManager(requireContext())
        mViewDataBinding.drawingsRV.layoutManager = linearLayoutManager
        mViewDataBinding.drawingsRV.setHasFixedSize(true)
        mViewDataBinding.drawingsRV.adapter = sectionedAdapter



        viewModel.favoriteGroups.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                sectionList.removeAt(0)
                sectionList.add(
                    0, DrawingSectionHeader(it, getString(R.string.favorite_projects))
                )
                sectionedAdapter.insertNewSection(
                    DrawingSectionHeader(
                        it,
                        getString(R.string.favorite_projects)
                    ), 0
                )
                sectionedAdapter.notifyDataSetChanged()

            } else {
                sectionList.removeAt(0)
                sectionList.add(
                    0, DrawingSectionHeader(mutableListOf(), getString(R.string.favorite_projects))
                )
                sectionedAdapter.insertNewSection(
                    DrawingSectionHeader(
                        mutableListOf(),
                        getString(R.string.favorite_projects)
                    ), 0
                )
                sectionedAdapter.notifyDataSetChanged()
            }
        }

        viewModel.myGroupData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                sectionList.removeAt(1)
                sectionList.add(
                    1, DrawingSectionHeader(it, getString(R.string.my_groups))
                )
                sectionedAdapter.insertNewSection(
                    DrawingSectionHeader(
                        it,
                        getString(R.string.my_groups)
                    ), 1
                )
                sectionedAdapter.notifyDataSetChanged()

            } else {
                sectionList.removeAt(1)
                sectionList.add(
                    1, DrawingSectionHeader(mutableListOf(), getString(R.string.my_groups))
                )
                sectionedAdapter.insertNewSection(
                    DrawingSectionHeader(
                        mutableListOf(),
                        getString(R.string.my_groups)
                    ), 1
                )
                sectionedAdapter.notifyDataSetChanged()
            }
        }

        viewModel.otherGroupsData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val groupedByCreatorId = it.groupBy { group -> group.creator.id }

                // Creating an array of arrays where each inner array contains groups with the same creator._id
                val result = groupedByCreatorId.values.toList()



                if (sectionList.size > 2) {
                    val iterator = sectionList.iterator()
                    var count = 0

                    while (iterator.hasNext()) {
                        if (count > 2) {
                            iterator.remove()
                        }

                        iterator.next()
                        count++
                    }
                }

                result.mapIndexed { index, creatorGroups ->
                    println("otherGroupsData: ${creatorGroups.size} groups: ${creatorGroups}")

                    sectionList.add(
                        index + 2,
                        DrawingSectionHeader(
                            creatorGroups.toMutableList(),
                            "Group shared by: ${creatorGroups[0].creator.firstName} ${creatorGroups[0].creator.surName}"
                        )
                    )
                    sectionedAdapter.insertNewSection(
                        DrawingSectionHeader(
                            creatorGroups.toMutableList(),
                            "Group shared by: ${creatorGroups[0].creator.firstName} ${creatorGroups[0].creator.surName}"
                        ), index + 2
                    )
                }
                sectionedAdapter.notifyDataSetChanged()


                /*sectionList.removeAt(2)
                sectionList.add(
                    2, DrawingSectionHeader(it, getString(R.string.my_groups))
                )
                sectionedAdapter.insertNewSection(
                    DrawingSectionHeader(
                        it,
                        getString(R.string.my_groups)
                    ), 2
                )
                sectionedAdapter.notifyDataSetChanged()*/

            } else {
                if (sectionList.size > 2) {
                    val iterator = sectionList.iterator()
                    var count = 0

                    while (iterator.hasNext()) {
                        if (count > 2) {
                            iterator.remove()
                        }

                        iterator.next()
                        count++
                    }
                }
                sectionList.add(
                    2, DrawingSectionHeader(mutableListOf(), getString(R.string.other_groups))
                )
                sectionedAdapter.insertNewSection(
                    DrawingSectionHeader(
                        mutableListOf(),
                        getString(R.string.other_groups)
                    ), 2
                )
                sectionedAdapter.notifyDataSetChanged()
            }
        }

        viewModel.originalGroups.observe(viewLifecycleOwner) {
            if (it.size > 1) {
                mViewDataBinding.clSearch.visibility = View.VISIBLE
            } else {
                mViewDataBinding.clSearch.visibility = View.GONE
            }
        }

    }

    override fun onDestroy() {
        mainActivityDownloader?.unregisterReceiver(downloadCompleteReceiver)
        mainActivityDownloader = null

        super.onDestroy()
    }


    companion object {
        const val folderName = "Ceibro"
        const val permissionRequestCode = 123
        var mainActivityDownloader: Context? = null

        var referenceSectionedAdapter: AllDrawingsAdapterSectionRecycler? = null

        fun updateAdapter() {
            referenceSectionedAdapter?.notifyDataSetChanged()

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

    private fun checkDownloadFilePermission(
        url: DrawingV2,
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

    private fun checkPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(requireActivity(), permissions, permissionRequestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
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
        drawing: DrawingV2,
        downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
        itemClickListener: ((tag: String) -> Unit)?
    ) {

        manager?.let {
            downloadDrawingFile(drawing, downloadedDrawingV2Dao, it)
        } ?: kotlin.run {

            manager =
                requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager?.let { downloadDrawingFile(drawing, downloadedDrawingV2Dao, it) }
        }


        /*

                val uri = Uri.parse(drawing.fileUrl)
                val fileName = drawing.fileName
                val folder = File(context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), folderName)
                val folder1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                println("DIRECTORY_DOWNLOADS: $folder1")
                if (!folder.exists()) {
                    folder.mkdirs()
                }
                val destinationUri = Uri.fromFile(File(folder1, fileName))
                // Set the MIME type
                val mimeType = getMimeTypeFromUrl(drawing.fileUrl)
                println("DIRECTORY_DOWNLOADS: mimeType: $mimeType")

                val request: DownloadManager.Request? =
                    DownloadManager
                        .Request(uri)
        //                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName)
                        .setDestinationUri(destinationUri)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setVisibleInDownloadsUi(true)

                if (mimeType != null){
                    request?.setMimeType(mimeType)
                }


                val downloadId = manager?.enqueue(request)

                val ceibroDownloadDrawingV2 = downloadId?.let {
                    CeibroDownloadDrawingV2(
                        fileName = drawing.fileName,
                        downloading = true,
                        isDownloaded = false,
                        downloadId = it,
                        drawing = drawing,
                        drawingId = drawing._id,
                        groupId = drawing.groupId,
                        localUri = ""
                    )
                }


                GlobalScope.launch {
                    ceibroDownloadDrawingV2?.let {
                        downloadedDrawingV2Dao.insertDownloadDrawing(it)
                    }
                }
        */

//        println("id: ${id} Folder name: ${folder} uri:${uri} destinationUri:${destinationUri}")

    }

    @SuppressLint("Range")
    private fun getDownloadProgress(
        context: Context?,
        downloadId: Long,
        itemClickListener: ((tag: String) -> Unit)?
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
                    }
                } else {
                    itemClickListener?.invoke("retry")
                    break
                }
                cursor.close()

                delay(500)
            }
        }
    }

    private fun chooseFile(fragmentManager: FragmentManager, callback: (String) -> Unit) {
        val sheet = AddNewPhotoBottomSheet {
            callback.invoke(it)
        }

        sheet.isCancelable = true
        sheet.setStyle(
            BottomSheetDialogFragment.STYLE_NORMAL,
            R.style.CustomBottomSheetDialogTheme
        )
        sheet.show(fragmentManager, "ImagePickerOrCaptureDialogSheet")
    }

    private fun chooseDocuments(mimeTypes: Array<String>) {
        requireActivity().openFilePicker(
            mimeTypes = mimeTypes,
            allowMultiple = false
        ) { resultCode, data ->
            val pickedDocuments = arrayListOf<PickedImages>()

            if (resultCode == Activity.RESULT_OK && data != null) {
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val fileUri = clipData.getItemAt(i).uri
                        var fileName = getFileNameFromUri(requireContext(), fileUri)
                        var fileExtension = getFileExtension(requireContext(), fileUri)
                        if (fileExtension.isNullOrEmpty()) {
                            fileExtension = "pdf"
                        }
                        fileName = if (fileName.isNullOrEmpty()) {
                            System.currentTimeMillis().toString() + ".$fileExtension"
                        } else {
                            if (!ensurePdfExtension(fileName).isNullOrEmpty()) {
                                fileName
                            } else {
                                "$fileName.$fileExtension"
                            }
                        }
                        val pdfFilePath = copyFileToInternalStorage(fileUri, fileName)
                        val pdfFileObj = pdfFilePath?.let { it1 -> File(it1) }
                        if (pdfFileObj?.let { it1 -> checkIfPDFHasMultiplePages(it1) } == true) {
                            shortToastNow("Multi page drawing file is not allowed yet")
                        } else {
                            println("pdfFilePath1 ${pdfFilePath} fileName1: $fileName")
                            val bundle = Bundle()
                            bundle.putString("pdfFilePath", pdfFilePath)
                            bundle.putString("pdfFileName", fileName)
                            bundle.putString("projectId", viewModel.projectData.value!!._id)
                            navigate(R.id.newDrawingV2Fragment, bundle)
                        }

                        break
                    }
                } else {
                    val fileUri = data.data
                    fileUri?.let {
                        var fileName = getFileNameFromUri(requireContext(), it)
                        var fileExtension = getFileExtension(requireContext(), it)
                        if (fileExtension.isNullOrEmpty()) {
                            fileExtension = "pdf"
                        }
                        fileName = if (fileName.isNullOrEmpty()) {
                            System.currentTimeMillis().toString() + ".$fileExtension"
                        } else {
                            if (!ensurePdfExtension(fileName!!).isNullOrEmpty()) {
                                fileName
                            } else {
                                "$fileName.$fileExtension"
                            }
                        }
//                        val selectedDocDetail = getPickedFileDetail(requireContext(), it)
                        val pdfFilePath = copyFileToInternalStorage(it, fileName!!)
                        val pdfFileObj = pdfFilePath?.let { it1 -> File(it1) }
                        if (pdfFileObj?.let { it1 -> checkIfPDFHasMultiplePages(it1) } == true) {
                            shortToastNow("Multi page drawing file is not allowed yet")
                        } else {
                            println("pdfFilePath ${pdfFilePath} fileName: $fileName")
                            val bundle = Bundle()
                            bundle.putString("pdfFilePath", pdfFilePath)
                            bundle.putString("pdfFileName", fileName)
                            bundle.putString("projectId", viewModel.projectData.value!!._id)
                            navigate(R.id.newDrawingV2Fragment, bundle)
                        }

//                        val pdfFileUri= pdfFilePath?.let { it1 -> getFileUri(it1) }
                    }
                }
            }

        }
    }

    private fun checkIfPDFHasMultiplePages(pdfFile: File): Boolean {
        try {
            val parcelFileDescriptor: ParcelFileDescriptor =
                ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)

            // Assuming the PDF file has at least one page
            val pageCount = pdfRenderer.pageCount

            pdfRenderer.close()
            parcelFileDescriptor.close()

            return pageCount > 1
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    private fun copyFileToInternalStorage(uri: Uri, fileName: String): String? {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val outputStream: FileOutputStream

        try {
            // Create a file in the internal storage
            val file = File(requireContext().filesDir, fileName)
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


    private fun getFileUri(filePath: String): Uri? {
        val file = File(filePath)

        // If the file is within your app's internal or external storage, use FileProvider
//        if (file.exists()) {
//            println("pdfFilePath1 file.exists")
//            return FileProvider.getUriForFile(
//                requireContext(),
//                "com.zstronics.ceibro.fileprovider",  // Use your app's authority
//                file
//            )
//        }

        // If the file is on external storage or not accessible to your app, use Uri.fromFile()
        return Uri.fromFile(file)
    }

    private fun getPickedFileDetail(context: Context, fileUri: Uri?): PickedImages {
        val mimeType = FileUtils.getMimeType(context, fileUri)
        val fileName = FileUtils.getFileName(context, fileUri)
        val fileSize = FileUtils.getFileSizeInBytes(context, fileUri)
        val fileSizeReadAble = FileUtils.getReadableFileSize(fileSize)
        val attachmentType = when {
            mimeType == null -> {
                AttachmentTypes.Doc
            }

            mimeType == "application/pdf" -> {
                AttachmentTypes.Pdf
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateGroupDrawings(event: LocalEvents.UpdateGroupDrawings?) {
        event?.let {
            viewModel.getGroupsByProjectID(it.projectID)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshGroupsData(event: LocalEvents.RefreshGroupsData?) {
        val projectID = event?.projectId
        val projectData = viewModel.projectData.value
        if (projectData != null) {
            if (projectData._id == projectID) {
//            println("floorList.onRefreshGroupsData")
                viewModel.getGroupsByProjectID(projectData._id)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshDeletedGroupData(event: LocalEvents.RefreshDeletedGroupData?) {
        val groupId = event?.groupId
        val allGroups = viewModel.originalGroups.value
        val projectData = viewModel.projectData.value

        if (allGroups != null) {
            val foundGroup = allGroups.find { it._id == groupId }
            if (foundGroup != null) {
                if (projectData != null) {
                    viewModel.getGroupsByProjectID(projectData._id)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        if (mViewDataBinding.projectsSearchCard.isVisible()) {
            showKeyboard()
            mViewDataBinding.projectSearchBar.requestFocus()
        }
    }
}