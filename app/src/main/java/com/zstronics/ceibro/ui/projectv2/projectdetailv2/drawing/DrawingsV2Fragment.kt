package com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawing

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.zstronics.ceibro.databinding.FragmentDrawingsV2Binding
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.projectv2.newprojectv2.AddNewPhotoBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@AndroidEntryPoint
class DrawingsV2Fragment :
    BaseNavViewModelFragment<FragmentDrawingsV2Binding, IDrawingV2.State, DrawingsV2VM>() {

    private lateinit var downloadCompleteReceiver: DownloadCompleteReceiver
    private var manager: DownloadManager? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionList13 = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    private val permissionList12 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: DrawingsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_drawings_v2
    override fun toolBarVisibility(): Boolean = false
    private var sectionList: MutableList<DrawingSectionHeader> = mutableListOf()

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


        val stringListData =
            StringListData(listOf("Group 1", "Group 2", "Group 3", "Group 4", "Group 5"))

        sectionList.add(
            0,
            DrawingSectionHeader(
                listOf(stringListData, stringListData, stringListData, stringListData),
                getString(R.string.favorite_projects)
            )
        )
        sectionList.add(
            1,
            DrawingSectionHeader(
                listOf(stringListData, stringListData, stringListData, stringListData),
                getString(R.string.recently_used)
            )
        )


        sectionedAdapter = AllDrawingsAdapterSectionRecycler(requireContext(), sectionList)
        retrieveFilesFromCeibroFolder()
        sectionedAdapter?.setCallBack { view, postitoin, data, tag ->
            checkDownloadFilePermission(data)
        }
        val linearLayoutManager = LinearLayoutManager(requireContext())
        mViewDataBinding.drawingsRV.layoutManager = linearLayoutManager
        mViewDataBinding.drawingsRV.setHasFixedSize(true)
        mViewDataBinding.drawingsRV.adapter = sectionedAdapter


    }

    override fun onDestroy() {
        mainActivityDownloader?.unregisterReceiver(downloadCompleteReceiver)
        mainActivityDownloader = null
        sectionedAdapter = null

        super.onDestroy()
    }


    companion object {
        const val folderName = "Ceibro"
        const val permissionRequestCode = 123
        var mainActivityDownloader: Context? = null

        var sectionedAdapter: AllDrawingsAdapterSectionRecycler? = null
        fun retrieveFilesFromCeibroFolder() {
            val folder = File(
                mainActivityDownloader?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                folderName
            )
            if (folder.exists()) {
                val files: Array<File> = folder.listFiles() ?: arrayOf()
                for (file in files) {
                    println("File Path: $file")
                }

                sectionedAdapter?.notifyDataSetChanged()
            }
        }
    }


    private fun checkDownloadFilePermission(url: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkPermissions(permissionList13)) {
                downloadFile(url)
            } else {
                requestPermissions(permissionList13)
            }
        } else {
            if (checkPermissions(permissionList12)) {
                downloadFile(url)
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
                        if (fileExtension.isNullOrEmpty()){
                            fileExtension="pdf"
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
                        navigate(R.id.newDrawingV2Fragment, bundle)
                        break
                    }
                } else {
                    val fileUri = data.data
                    fileUri?.let {
                        var fileName = getFileNameFromUri(requireContext(), it)
                        var fileExtension = getFileExtension(requireContext(), it)
                        if (fileExtension.isNullOrEmpty()){
                            fileExtension="pdf"
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
}

data class StringListData(
    val stringList: List<String>
)

