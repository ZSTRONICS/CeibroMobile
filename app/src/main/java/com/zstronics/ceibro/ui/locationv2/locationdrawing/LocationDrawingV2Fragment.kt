package com.zstronics.ceibro.ui.locationv2.locationdrawing

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
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
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.extensions.showKeyboard
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroDownloadDrawingV2
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.databinding.FragmentLocationDrawingsV2Binding
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.projectv2.newprojectv2.AddNewPhotoBottomSheet
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawing.DrawingsV2Fragment
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@AndroidEntryPoint
class LocationDrawingV2Fragment :
    BaseNavViewModelFragment<FragmentLocationDrawingsV2Binding, ILocationDrawingV2.State, LocationDrawingV2VM>() {
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: LocationDrawingV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_location_drawings_v2
    override fun toolBarVisibility(): Boolean = false

    //    private lateinit var downloadCompleteReceiver: DownloadCompleteReceiver
    private var manager: DownloadManager? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionList13 = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    private val permissionList12 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )


    var drawingFileClickListener: ((view: View, data: DrawingV2, tag: String) -> Unit)? =
        null

    private var fragmentManager: FragmentManager? = null

    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                EventBus.getDefault().post(LocalEvents.LoadLocationProjectFragmentInLocation())
            }

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


    lateinit var sectionedAdapter: LocationDrawingAdapterSectionRecycler
    private var sectionList: MutableList<LocationDrawingSectionHeader> = mutableListOf()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentManager = childFragmentManager

        mainActivityDownloader = mViewDataBinding.root.context

        manager =
            mViewDataBinding.root.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

//        downloadCompleteReceiver = DownloadCompleteReceiver()
//        mainActivityDownloader?.registerReceiver(
//            downloadCompleteReceiver,
//            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
//        )


        sectionList.add(
            0,
            LocationDrawingSectionHeader(
                mutableListOf(),
                getString(R.string.favorite_projects)
            )
        )
        sectionList.add(
            1,
            LocationDrawingSectionHeader(
                mutableListOf(),
                getString(R.string.all_groups)
            )
        )


        sectionedAdapter = LocationDrawingAdapterSectionRecycler(
            viewModel.downloadedDrawingV2Dao,
            requireContext(),
            sectionList
        )
        referenceSectionedAdapter = sectionedAdapter

        sectionedAdapter.setCallBack { view, data, tag ->
            CookiesManager.drawingFileNameForLocation = data.fileName
            CookiesManager.drawingFileForLocation.value = data
            CookiesManager.cameToLocationViewFromProject = false
            CookiesManager.openingNewLocationFile = true
            EventBus.getDefault().post(LocalEvents.LoadViewDrawingFragmentInLocation())
//            drawingFileClickListener?.invoke(view, data, tag)
//            checkDownloadFilePermission(data)
        }

        sectionedAdapter.downloadFileCallBack { view, data, tag ->
            checkDownloadFilePermission(data, viewModel.downloadedDrawingV2Dao)
        }


        val linearLayoutManager = LinearLayoutManager(requireContext())
//        mViewDataBinding.drawingsRV.removeAllViews()
        mViewDataBinding.drawingsRV.layoutManager = linearLayoutManager
        mViewDataBinding.drawingsRV.setHasFixedSize(true)
        mViewDataBinding.drawingsRV.adapter = sectionedAdapter



        viewModel.favoriteGroups.observe(viewLifecycleOwner) {

            if (it.isNotEmpty()) {
                if (!viewModel.favoriteGroupsOnceSet) {
                    viewModel.favoriteGroupsOnceSet = true
                    sectionList.removeAt(0)
                    sectionList.add(
                        0,
                        LocationDrawingSectionHeader(it, getString(R.string.favorite_projects))
                    )
//                sectionedAdapter.removeSection(0)
                    sectionedAdapter.insertNewSection(
                        LocationDrawingSectionHeader(
                            it,
                            getString(R.string.favorite_projects)
                        ), 0
                    )
                    sectionedAdapter.notifyDataSetChanged()
                }
            } else {
                sectionList.removeAt(0)
                sectionList.add(
                    0,
                    LocationDrawingSectionHeader(
                        mutableListOf(),
                        getString(R.string.favorite_projects)
                    )
                )
//                sectionedAdapter.removeSection(0)
                sectionedAdapter.insertNewSection(
                    LocationDrawingSectionHeader(
                        mutableListOf(),
                        getString(R.string.favorite_projects)
                    ), 0
                )
                sectionedAdapter.notifyDataSetChanged()
            }

        }

        viewModel.groupData.observe(viewLifecycleOwner) {

            if (it.isNotEmpty()) {
                if (!viewModel.allGroupsOnceSet) {
                    viewModel.allGroupsOnceSet = true
                    sectionList.removeAt(1)
                    sectionList.add(
                        1, LocationDrawingSectionHeader(it, getString(R.string.all_groups))
                    )
//                sectionedAdapter.removeSection(1)
                    sectionedAdapter.insertNewSection(
                        LocationDrawingSectionHeader(
                            it,
                            getString(R.string.all_groups)
                        ), 1
                    )
                    sectionedAdapter.notifyDataSetChanged()
                }
            } else {
                sectionList.removeAt(1)
                sectionList.add(
                    1,
                    LocationDrawingSectionHeader(
                        mutableListOf(),
                        getString(R.string.all_groups)
                    )
                )
//                sectionedAdapter.removeSection(1)
                sectionedAdapter.insertNewSection(
                    LocationDrawingSectionHeader(
                        mutableListOf(),
                        getString(R.string.all_groups)
                    ), 1
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

        viewModel.projectData.observe(viewLifecycleOwner) {
            if (it != null) {
                mViewDataBinding.tvProjectName.text = it.title
                mViewDataBinding.tvCreatorName.text =
                    "${resources.getString(R.string.creator_heading)}: ${it.creator.firstName} ${it.creator.surName}"
            }
        }

    }

    override fun onDestroy() {
//        mainActivityDownloader?.unregisterReceiver(downloadCompleteReceiver)
//        mainActivityDownloader = null

        super.onDestroy()
    }


    companion object {
        const val folderName = "Ceibro"
        const val permissionRequestCode = 123
        var mainActivityDownloader: Context? = null
        var referenceSectionedAdapter: LocationDrawingAdapterSectionRecycler? = null

        fun updateLocationDrawingAdapterSectionRecyclerAdapter() {
            referenceSectionedAdapter?.notifyDataSetChanged()

        }
    }

    private fun checkDownloadFilePermission(
        url: DrawingV2,
        downloadedDrawingV2Dao: DownloadedDrawingV2Dao
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkPermissions(permissionList13)) {
                downloadFile(url, downloadedDrawingV2Dao)
            } else {
                requestPermissions(permissionList13)
            }
        } else {
            if (checkPermissions(permissionList12)) {
                downloadFile(url, downloadedDrawingV2Dao)
            } else {
                requestPermissions(permissionList12)
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

    private fun downloadFile(drawing: DrawingV2, downloadedDrawingV2Dao: DownloadedDrawingV2Dao) {
        val uri = Uri.parse(drawing.fileUrl)
        val fileName = drawing.fileName
        val folder = File(
            context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            DrawingsV2Fragment.folderName
        )
        if (!folder.exists()) {
            folder.mkdirs()
        }
//        val existingFile = File(folder, fileName)
//        if (existingFile.exists()) {
//            existingFile.delete()
//        }
        val destinationUri = Uri.fromFile(File(folder, fileName))

        val request: DownloadManager.Request? =
            DownloadManager
                .Request(uri)
                .setDestinationUri(destinationUri)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setVisibleInDownloadsUi(true)

        val downloadId = manager?.enqueue(request)

        val ceibroDownloadDrawingV2 = downloadId?.let {
            CeibroDownloadDrawingV2(
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

        //   getDownloadProgress(context,downloadId!!)


        println("id: ${id} Folder name: ${folder} uri:${uri} destinationUri:${destinationUri}")

    }


    private fun downloadFile(fileUrl: String) {

        val url =
            "https://ceibro-development.s3.eu-north-1.amazonaws.com/task/task/2023-11-30/Chemistry_Full_Book_Punjab_-_Copy__2__1701341788337.pdf"


        val uri =
            Uri.parse(url)

        val fileName = "downloaded_file.pdf"
        val folder = File(context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), folderName)
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val request: DownloadManager.Request? =
            DownloadManager
                .Request(uri)
                .setDestinationUri(Uri.fromFile(File(folder, fileName)))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setVisibleInDownloadsUi(true)

        manager?.enqueue(request)
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
//                        val pdfFileUri = pdfFilePath?.let { it1 -> getFileUri(it1) }
                        println("pdfFilePath1 ${pdfFilePath}")
                        val bundle = Bundle()
                        bundle.putString("pdfFilePath", pdfFilePath)
                        bundle.putString("pdfFileName", fileName)
                        bundle.putString("projectId", viewModel.projectData.value!!._id)
                        navigate(R.id.newDrawingV2Fragment, bundle)
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
//                        val pdfFileUri= pdfFilePath?.let { it1 -> getFileUri(it1) }
                        println("pdfFilePath ${pdfFilePath} fileName: $fileName")
                        val bundle = Bundle()
                        bundle.putString("pdfFilePath", pdfFilePath)
                        bundle.putString("pdfFileName", fileName)
                        bundle.putString("projectId", viewModel.projectData.value!!._id)
                        navigate(R.id.newDrawingV2Fragment, bundle)
                    }
                }
            }

        }
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
//        event?.let {
//            viewModel.getGroupsByProjectID(it.projectID)
//        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


}

