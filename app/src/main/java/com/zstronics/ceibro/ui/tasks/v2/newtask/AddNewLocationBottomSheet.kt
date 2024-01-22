package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroDownloadDrawingV2
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.databinding.FragmentAddNewLocationSheetBinding
import com.zstronics.ceibro.ui.locationv2.locationdrawing.LocationDrawingAdapterSectionRecycler
import com.zstronics.ceibro.ui.locationv2.locationdrawing.LocationDrawingSectionHeader
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawings.DrawingsV2Fragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class AddNewLocationBottomSheet(
    originalAllGroups: MutableList<CeibroGroupsV2>,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
    val networkConnectivityObserver: NetworkConnectivityObserver
) : BottomSheetDialogFragment() {

    lateinit var binding: FragmentAddNewLocationSheetBinding
    var onDrawingTapped: (() -> Unit)? = null

    val allGroupsOriginalList = originalAllGroups
    val allGroupsList = originalAllGroups

    private val _myGroupData: MutableLiveData<MutableList<CeibroGroupsV2>> = MutableLiveData(mutableListOf())
    val myGroupData: LiveData<MutableList<CeibroGroupsV2>> = _myGroupData

    private val _otherGroupsData: MutableLiveData<MutableList<CeibroGroupsV2>> = MutableLiveData(mutableListOf())
    val otherGroupsData: LiveData<MutableList<CeibroGroupsV2>> = _otherGroupsData

    private val _favoriteGroups: MutableLiveData<MutableList<CeibroGroupsV2>> = MutableLiveData(mutableListOf())
    val favoriteGroups: LiveData<MutableList<CeibroGroupsV2>> = _favoriteGroups

    private var originalFavouriteGroups: MutableList<CeibroGroupsV2> = mutableListOf()
    private var originalOtherGroups: MutableList<CeibroGroupsV2> = mutableListOf()
    private var originalMyGroups: MutableList<CeibroGroupsV2> = mutableListOf()

    lateinit var sectionedAdapter: LocationDrawingAdapterSectionRecycler
    private var sectionList: MutableList<LocationDrawingSectionHeader> = mutableListOf()
    var favoriteGroupsOnceSet = false

    private var manager: DownloadManager? = null
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionList13 = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    private val permissionList10 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_new_location_sheet,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        separateGroups(allGroupsList)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manager = binding.root.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        if (allGroupsList.isEmpty()) {
            binding.emptyListText.visibility = View.VISIBLE
        } else {
            binding.emptyListText.visibility = View.GONE
        }

        binding.cancelBtn.setOnClick {
            dismiss()
        }

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
                getString(R.string.my_groups)
            )
        )
        sectionList.add(
            2,
            LocationDrawingSectionHeader(
                mutableListOf(),
                getString(R.string.other_groups)
            )
        )

        sectionedAdapter = LocationDrawingAdapterSectionRecycler(
            downloadedDrawingV2Dao,
            requireContext(),
            sectionList,
            networkConnectivityObserver
        )

        sectionedAdapter.drawingFileCallBack { view, data, absolutePath ->
            data.uploaderLocalFilePath = absolutePath
            CeibroApplication.CookiesManager.drawingFileForNewTask.value = data
            onDrawingTapped?.invoke()
        }
        sectionedAdapter.downloadFileCallBack { tv, ivDownloadFile, ivDownloaded, data, tag ->
            checkDownloadFilePermission(data, downloadedDrawingV2Dao) {
                MainScope().launch {
                    if (it.trim().equals("100%", true)) {
//                        sectionedAdapter.notifyDataSetChanged()
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


        val linearLayoutManager = LinearLayoutManager(requireContext())
//        mViewDataBinding.drawingsRV.removeAllViews()
        binding.newTaskGroupsRV.layoutManager = linearLayoutManager
        binding.newTaskGroupsRV.setHasFixedSize(true)
        binding.newTaskGroupsRV.adapter = sectionedAdapter



        favoriteGroups.observe(viewLifecycleOwner) {

            if (it.isNotEmpty()) {
                if (!favoriteGroupsOnceSet) {
                    favoriteGroupsOnceSet = true
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

        myGroupData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                sectionList.removeAt(1)
                sectionList.add(
                    1, LocationDrawingSectionHeader(it, getString(R.string.my_groups))
                )
                sectionedAdapter.insertNewSection(
                    LocationDrawingSectionHeader(
                        it,
                        getString(R.string.my_groups)
                    ), 1
                )
                sectionedAdapter.notifyDataSetChanged()

            } else {
                sectionList.removeAt(1)
                sectionList.add(
                    1, LocationDrawingSectionHeader(mutableListOf(), getString(R.string.my_groups))
                )
                sectionedAdapter.insertNewSection(
                    LocationDrawingSectionHeader(
                        mutableListOf(),
                        getString(R.string.my_groups)
                    ), 1
                )
                sectionedAdapter.notifyDataSetChanged()
            }
        }

        otherGroupsData.observe(viewLifecycleOwner) {
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

                    sectionList.add(
                        index + 2,
                        LocationDrawingSectionHeader(
                            creatorGroups.toMutableList(),
                            "Group shared by: ${creatorGroups[0].creator.firstName} ${creatorGroups[0].creator.surName}"
                        )
                    )
                    sectionedAdapter.insertNewSection(
                        LocationDrawingSectionHeader(
                            creatorGroups.toMutableList(),
                            "Group shared by: ${creatorGroups[0].creator.firstName} ${creatorGroups[0].creator.surName}"
                        ), index + 2
                    )
                }
                sectionedAdapter.notifyDataSetChanged()


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
                    2,
                    LocationDrawingSectionHeader(mutableListOf(), getString(R.string.other_groups))
                )
                sectionedAdapter.insertNewSection(
                    LocationDrawingSectionHeader(
                        mutableListOf(),
                        getString(R.string.other_groups)
                    ), 2
                )
                sectionedAdapter.notifyDataSetChanged()
            }
        }

    }



    private fun separateGroups(groupsList: MutableList<CeibroGroupsV2>) {
        if (groupsList.isNotEmpty()) {

            val favoriteGroups = groupsList.filter { it.isFavoriteByMe } ?: listOf()
            originalFavouriteGroups = favoriteGroups.toMutableList()

            val creatorGroups = groupsList.filter { (!it.isFavoriteByMe) && (it.isCreator) } ?: listOf()
            originalMyGroups = creatorGroups.toMutableList()

            val otherGroups = groupsList.filter { (!it.isFavoriteByMe) && (!it.isCreator) } ?: listOf()
            originalOtherGroups = otherGroups.toMutableList()


            _favoriteGroups.value = favoriteGroups.toMutableList()
            _myGroupData.value = creatorGroups.toMutableList()
            _otherGroupsData.value = otherGroups.toMutableList()
        }
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = false
            dialog.behavior.state = STATE_COLLAPSED
        }
        return dialog
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
                    shortToastNow("Permission denied: $permission")
                } else {
                    shortToastNow("Permission denied: $permission. Please enable it in the app settings.")
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
        val uri = Uri.parse(drawing.fileUrl)
        val fileName = drawing.fileName
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

       /* Handler(Looper.getMainLooper()).postDelayed({
            getDownloadProgress(context, downloadId!!) {
                itemClickListener?.invoke(it)
            }
        }, 1000)*/

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

}