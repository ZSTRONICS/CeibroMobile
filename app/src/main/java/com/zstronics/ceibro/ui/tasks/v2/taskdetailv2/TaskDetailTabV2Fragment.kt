package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.finish
import com.zstronics.ceibro.base.extensions.launchActivityWithFinishAffinity
import com.zstronics.ceibro.base.extensions.longToastNow
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BackNavigationResult
import com.zstronics.ceibro.base.navgraph.BackNavigationResultListener
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.databinding.FragmentTaskDetailTabV2Binding
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawings.DrawingsV2Fragment
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.TaskInfoBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class TaskDetailTabV2Fragment :
    BaseNavViewModelFragment<FragmentTaskDetailTabV2Binding, ITaskDetailTabV2.State, TaskDetailTabV2VM>(),
    BackNavigationResultListener {
    val list = ArrayList<String>()
    var sharedViewModel: SharedViewModel? = null
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskDetailTabV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_detail_tab_v2
    override fun toolBarVisibility(): Boolean = false
    val FORWARD_TASK_REQUEST_CODE = 104
    val COMMENT_REQUEST_CODE = 106
    val DONE_REQUEST_CODE = 107

    private var manager: DownloadManager? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionList13 = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    private val permissionList10 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )


    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> {
                navigateBackFromDetailFragment {
                    navigateBack()
                }
            }

            R.id.taskInfoBtn -> showTaskInfoBottomSheet()
            R.id.drawingOpenBtn -> {
                val task = viewModel.taskDetail.value
                task?.let { taskData ->
                    if (taskData.pinData != null) {
                        GlobalScope.launch(Dispatchers.Main) {
                            var downloadedDrawingFile =
                                viewModel.downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(
                                    taskData.pinData!!.drawingId
                                )

                            var actualDrawingObj: DrawingV2? = null
                            val projectId = taskData.project?.id
                            if (projectId != null) {
                                val groups = viewModel.groupsV2Dao.getAllProjectGroups(projectId)
                                groups.map {
                                    it.drawings.map { drawing ->
                                        if (drawing._id == taskData.pinData!!.drawingId) {
                                            actualDrawingObj = drawing
                                        }
                                    }
                                }
                            }

                            actualDrawingObj?.let {

                                downloadedDrawingFile?.let { downloadedFile ->

                                    if (downloadedFile.isDownloaded) {

                                        actualDrawingObj?.uploaderLocalFilePath =
                                            downloadedFile.localUri
                                        CeibroApplication.CookiesManager.drawingFileForLocation.value =
                                            actualDrawingObj
                                        viewModel.sessionManagerInternal.saveCompleteDrawingObj(
                                            actualDrawingObj
                                        )

                                        CeibroApplication.CookiesManager.cameToLocationViewFromProject =
                                            true
                                        CeibroApplication.CookiesManager.openingNewLocationFile =
                                            true
                                        navigateBackFromDetailFragment {
                                            navigateBack()
                                            EventBus.getDefault()
                                                .postSticky(LocalEvents.LoadDrawingInLocation())
                                        }


                                    } else if (downloadedFile.downloading) {
                                        shortToastNow("Please wait, file is downloading")


                                        getDownloadProgressSeparately(
                                            mViewDataBinding.root.context,
                                            downloadedFile.downloadId
                                        ) { status ->
                                            MainScope().launch {
                                                if (status.equals("downloaded", true)) {
                                                    downloadedDrawingFile =
                                                        viewModel.downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(
                                                            taskData.pinData!!.drawingId
                                                        )
                                                    shortToastNow("File downloaded")
                                                } else if (status == "retry" || status == "failed") {
                                                    viewModel.downloadedDrawingV2Dao.deleteByDrawingID(
                                                        taskData.pinData!!.drawingId
                                                    )
                                                    downloadedDrawingFile = null

                                                }
                                            }
                                        }

                                    } else {
                                        shortToastNow("Cannot download file. Please download it from projects")
                                    }
                                } ?: kotlin.run {

                                    actualDrawingObj?.let {
                                        val triplet = Triple(it._id, it.fileName, it.fileUrl)

                                        checkDownloadFilePermission(
                                            triplet,
                                            viewModel.downloadedDrawingV2Dao
                                        ) {
                                            MainScope().launch {
                                                if (it.trim().equals("100%", true)) {


                                                    downloadedDrawingFile =
                                                        viewModel.downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(
                                                            taskData.pinData!!.drawingId
                                                        )

                                                    println("progress  File data $downloadedDrawingFile")
                                                    println("progress  File Downloaded")
                                                    shortToastNow("File Downloaded")
                                                } else if (it == "retry" || it == "failed") {
                                                    viewModel.downloadedDrawingV2Dao.deleteByDrawingID(
                                                        taskData.pinData!!.drawingId
                                                    )
                                                    println("progress  File failed to downloaded")
                                                    shortToastNow("Downloading Failed")
                                                }
                                            }
                                        }
                                    } ?: kotlin.run {
                                        shortToastNow("Drawing/Group is not accessible, please check it in projects.")
                                    }
                                }

                            } ?: kotlin.run {
                                longToastNow("Drawing/Group is not accessible because it might be private now")
                            }

                        }
                    }
                }
            }

            R.id.taskCommentBtn -> {
                mViewDataBinding.viewPager.setCurrentItem(1, true)

                Handler(Looper.getMainLooper()).postDelayed({
                    EventBus.getDefault().post(LocalEvents.OpenKeyboard())
                }, 100)

            }

            R.id.doneBtn -> {
                if (viewModel.taskDetail.value?.doneCommentsRequired == true || viewModel.taskDetail.value?.doneImageRequired == true) {
                    val bundle = Bundle()
                    val taskData = viewModel.taskDetail.value
                    bundle.putBoolean(
                        "doneCommentsRequired",
                        taskData?.doneCommentsRequired ?: false
                    )
                    bundle.putBoolean("doneImageRequired", taskData?.doneImageRequired ?: false)
                    bundle.putString("taskId", taskData?.id)
                    bundle.putString("action", TaskDetailEvents.DoneTask.eventValue)
                    navigateForResult(R.id.commentFragment, DONE_REQUEST_CODE, bundle)
                } else {
                    viewModel.doneTask(viewModel.taskDetail.value?.id ?: "") {
//                        if (task != null) {
//                            viewModel.originalTask.postValue(task)
//                            viewModel._taskDetail.postValue(task)
//                        }
                    }
                }
            }

            R.id.taskForwardBtn -> {
                val taskData = viewModel.taskDetail.value
                val assignTo = taskData?.assignedToState?.map { it.phoneNumber }
                val invited = taskData?.invitedNumbers?.map { it.phoneNumber }
                val viewers = taskData?.viewer?.let { taskData.viewer.map { it.phoneNumber } }
                val confirmer = taskData?.confirmer?.phoneNumber
                val combinedList = arrayListOf<String>()
                if (assignTo != null) {
                    combinedList.addAll(assignTo)
                }
                if (invited != null) {
                    combinedList.addAll(invited)
                }

                viewers?.forEach { listOfViewer ->
                    listOfViewer?.let { viewerItem ->
                        combinedList.add(viewerItem)
                    }
                }

                if (confirmer != null) {
                    combinedList.add(confirmer)
                }

                val bundle = Bundle()
                bundle.putStringArrayList(
                    "assignToContacts",
                    combinedList
                )
                bundle.putString("taskId", taskData?.id)
                navigateForResult(R.id.forwardTaskFragment, FORWARD_TASK_REQUEST_CODE, bundle)
            }


        }
    }

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val instances = countActivitiesInBackStack(requireContext())
            if (instances <= 1) {
                launchActivityWithFinishAffinity<NavHostPresenterActivity>(
                    options = Bundle(),
                    clearPrevious = true
                ) {
                    putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
                    putExtra(
                        NAVIGATION_Graph_START_DESTINATION_ID,
                        R.id.homeFragment
                    )
                }
            } else {
                //finish is called so that second instance of app will be closed and only one last instance will remain
                finish()
            }
        }
    }

    lateinit var tabAdapter: TaskDetailV2TabLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.notificationTaskData.observe(viewLifecycleOwner) { notificationData ->
            if (notificationData != null) {
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
                if (viewModel.notificationType == 1) {
                    mViewDataBinding.viewPager.setCurrentItem(1, true)
                }

            }
        }
        manager =
            mViewDataBinding.root.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        list.add(getString(R.string.details))
        list.add(getString(R.string.comments))
        list.add(getString(R.string.files_heading))
        tabAdapter = TaskDetailV2TabLayout(requireActivity())
        tabAdapter.pinnedCommentClickListener = {
            mViewDataBinding.viewPager.setCurrentItem(1, true)
            Handler(Looper.getMainLooper()).postDelayed({
                EventBus.getDefault().postSticky(LocalEvents.ScrollToPosition(it))
            }, 100)

        }
        tabAdapter.goToItemClickListener = {
            if (it.isTaskFile) {
                mViewDataBinding.viewPager.setCurrentItem(0, true)
            } else {
                mViewDataBinding.viewPager.setCurrentItem(1, true)
                Handler(Looper.getMainLooper()).postDelayed({
                    EventBus.getDefault().post(LocalEvents.ScrollToPositionFromTaskFiles(it))
                }, 500)
            }
        }

        mViewDataBinding.viewPager.adapter = tabAdapter

        mViewDataBinding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        mViewDataBinding.bottomFooterLayout.visibility = View.VISIBLE
                    }

                    1 -> {
                        mViewDataBinding.bottomFooterLayout.visibility = View.GONE
                    }

                    2 -> {
                        mViewDataBinding.bottomFooterLayout.visibility = View.GONE
                    }
                }
            }
        })



        TabLayoutMediator(mViewDataBinding.tabLayout, mViewDataBinding.viewPager) { tab, position ->
            tab.text = list[position]
        }.attach()

        val tabTextColors = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf()),
            intArrayOf(resources.getColor(R.color.black), resources.getColor(R.color.appBlue))
        )
        mViewDataBinding.tabLayout.tabTextColors = tabTextColors
        mViewDataBinding.tabLayout.setSelectedTabIndicatorColor(Color.BLACK)


        viewModel.taskEvents.observe(viewLifecycleOwner) { eventsList ->
            GlobalScope.launch {
//                val localTaskDetailFiles = mutableListOf<LocalTaskDetailFiles>()
//                val task = viewModel.originalTask.value
//                task?.let { myTask ->
//                    val taskFilesToList = myTask.files.map {
//                        LocalTaskDetailFiles(
//                            fileID = it.id,
//                            fileComment = it.comment,
//                            userComment = null,
//                            fileName = it.fileName,
//                            fileTag = it.fileTag,
//                            fileUrl = it.fileUrl,
//                            hasComment = it.hasComment,
//                            moduleId = it.moduleId,
//                            moduleType = it.moduleType,
//                            createdAt = it.createdAt,
//                            uploadedBy = it.uploadedBy,
//                            isTaskFile = true
//                        )
//                    }
//                    localTaskDetailFiles.addAll(taskFilesToList)
//                }
//
//                if (!eventsList.isNullOrEmpty()) {
//                    CeibroApplication.CookiesManager.taskDetailEvents = eventsList
//
//                    eventsList.map { event ->
//                        val eventFilesToList = event.commentData?.files?.map { file ->
//                            LocalTaskDetailFiles(
//                                fileID = file.id,
//                                fileComment = file.comment,
//                                userComment = event.commentData.message,
//                                fileName = file.fileName,
//                                fileTag = file.fileTag,
//                                fileUrl = file.fileUrl,
//                                hasComment = file.hasComment,
//                                moduleId = file.moduleId,
//                                moduleType = file.moduleType,
//                                createdAt = event.createdAt,
//                                uploadedBy = event.initiator,
//                                isTaskFile = false
//                            )
//                        }
//                        if (eventFilesToList != null) {
//                            localTaskDetailFiles.addAll(eventFilesToList)
//                        }
//                    }
//                }
//                withContext(Dispatchers.Main) {
//                    CeibroApplication.CookiesManager.taskDetailFiles = localTaskDetailFiles
//                }
            }
        }

        viewModel.taskDetail.observe(viewLifecycleOwner) { item ->
            if (item?.hasPinData == true) {
                mViewDataBinding.drawingOpenBtn.visibility = View.VISIBLE
            } else {
                mViewDataBinding.drawingOpenBtn.visibility = View.GONE
            }

            if (item != null) {
                val isViewer = item.viewer?.find { it.id == viewModel.user?.id }

                if (item.creatorState.equals(
                        TaskStatus.DONE.name,
                        true
                    ) || item.creatorState.equals(
                        TaskStatus.CANCELED.name,
                        true
                    ) ||
                    viewModel.rootState.equals(TaskRootStateTags.InReview.tagValue, true) ||
                    viewModel.rootState.equals(TaskRootStateTags.ToReview.tagValue, true) ||
                    item.isTaskViewer ||
                    item.isTaskInApproval ||
                    (viewModel.rootState.equals(TaskRootStateTags.ToMe.tagValue, true) &&
                            (item.assignedToState.find { it.userId == viewModel.user?.id }?.state).equals(
                                TaskStatus.NEW.name,
                                true
                            )
                            ) ||
                    (viewModel.rootState.equals(TaskRootStateTags.All.tagValue, true) &&
                            (item.assignedToState.find { it.userId == viewModel.user?.id }?.state).equals(
                                TaskStatus.NEW.name,
                                true
                            )
                            )
                ) {
                    mViewDataBinding.doneBtn.isEnabled = false
                    mViewDataBinding.doneBtn.isClickable = false
                    mViewDataBinding.doneBtn.alpha = 0.6f
                    mViewDataBinding.taskForwardBtn.isEnabled = false
                    mViewDataBinding.taskForwardBtn.isClickable = false
                    mViewDataBinding.taskForwardBtn.alpha = 0.6f
                } else {
                    if (viewModel.isTaskBeingDone.value == false) {
                        mViewDataBinding.doneBtn.isEnabled = true
                        mViewDataBinding.doneBtn.isClickable = true
                        mViewDataBinding.doneBtn.alpha = 1f
                        mViewDataBinding.taskForwardBtn.isEnabled = true
                        mViewDataBinding.taskForwardBtn.isClickable = true
                        mViewDataBinding.taskForwardBtn.alpha = 1f
                    }
                }

                if (item.creatorState.equals(
                        TaskStatus.DONE.name,
                        true
                    ) || item.creatorState.equals(
                        TaskStatus.CANCELED.name,
                        true
                    )
                ) {
                    mViewDataBinding.doneRequirementBadge.visibility = View.GONE
                } else {
                    if (item.doneCommentsRequired || item.doneImageRequired) {
                        mViewDataBinding.doneRequirementBadge.visibility = View.VISIBLE
                    } else {
                        mViewDataBinding.doneRequirementBadge.visibility = View.GONE
                    }
                }

                mViewDataBinding.detailViewHeading.text = item.title?.trim() ?: "Task Details"
            } else {
                mViewDataBinding.doneBtn.isEnabled = false
                mViewDataBinding.doneBtn.isClickable = false
                mViewDataBinding.doneBtn.alpha = 0.6f
                mViewDataBinding.taskForwardBtn.isEnabled = false
                mViewDataBinding.taskForwardBtn.isClickable = false
                mViewDataBinding.taskForwardBtn.alpha = 0.6f
            }

        }

    }

    private fun navigateBackFromDetailFragment(callBack: () -> Unit) {
        val instances = countActivitiesInBackStack(requireContext())
        if (viewModel.notificationTaskData.value != null) {
            if (instances <= 1) {
                launchActivityWithFinishAffinity<NavHostPresenterActivity>(
                    options = Bundle(),
                    clearPrevious = true
                ) {
                    putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
                    putExtra(
                        NAVIGATION_Graph_START_DESTINATION_ID,
                        R.id.homeFragment
                    )
                }
            } else {
                //finish is called so that second instance of app will be closed and only one last instance will remain
                finish()
            }
        } else {
            callBack.invoke()
        }
    }

    private fun showTaskInfoBottomSheet() {
        val sheet = TaskInfoBottomSheet(
            _rootState = viewModel.rootState,
            _selectedState = viewModel.selectedState,
            _userId = viewModel.user?.id ?: "",
            _taskDetail = viewModel.taskDetail.value
        )
//        sheet.dialog?.window?.setSoftInputMode(
//            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
//                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
//        );

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "TaskInfoBottomSheet")
    }

    @SuppressLint("Range")
    private fun getDownloadProgressSeparately(
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

    private fun countActivitiesInBackStack(context: Context): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.appTasks
        var activityCount = 0

        for (task in runningTasks) {
            val taskInfo = task.taskInfo
            activityCount += taskInfo.numActivities
        }

        return activityCount
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
        requestPermissions(
            permissions,
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

        manager?.let {
            downloadGenericFile(triplet, downloadedDrawingV2Dao, it) { downloadId ->
                Handler(Looper.getMainLooper()).postDelayed({
                    getDownloadProgress(context, downloadId) { tag ->
                        GlobalScope.launch(Dispatchers.Main) {
                            if (tag == "retry" || tag == "failed") {
                                downloadedDrawingV2Dao.deleteByDrawingID(downloadId.toString())
                            } else if (tag.trim().equals("100%", true)) {

                                shortToastNow("Downloaded")
                            }
                        }
                        itemClickListener?.invoke(tag)
                    }
                }, 1000)
            }
        } ?: kotlin.run {

            manager =
                requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager?.let {
                downloadGenericFile(triplet, downloadedDrawingV2Dao, it) { downloadId ->
                    Handler(Looper.getMainLooper()).postDelayed({
                        getDownloadProgress(context, downloadId) { tag ->
                            GlobalScope.launch(Dispatchers.Main) {
                                if (tag == "retry" || tag == "failed") {
                                    downloadedDrawingV2Dao.deleteByDrawingID(downloadId.toString())
                                } else if (tag.trim().equals("100%", true)) {

                                    shortToastNow("Downloaded")
                                }
                            }
                            itemClickListener?.invoke(tag)
                        }
                    }, 1000)
                }
            }
        }

        /*
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
*/
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


    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        tabAdapter.onParentDestroyed()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskFailedToDone(event: LocalEvents.TaskFailedToDone?) {
        mViewDataBinding.doneBtn.isEnabled = true
        mViewDataBinding.doneBtn.isClickable = true
        mViewDataBinding.doneBtn.alpha = 1f
        mViewDataBinding.taskForwardBtn.isEnabled = true
        mViewDataBinding.taskForwardBtn.isClickable = true
        mViewDataBinding.taskForwardBtn.alpha = 1f
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskSeenEvent(event: LocalEvents.TaskSeenEvent?) {
        val task = event?.task
        if (task != null) {
            viewModel.taskDetail.value?.let { taskDetail ->
                if (task.id == taskDetail.id) {
                    task.let { it1 ->
                        viewModel.originalTask.postValue(it1)
                        viewModel._taskDetail.postValue(it1)
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateTaskInDetails(event: LocalEvents.UpdateTaskInDetails?) {
        val task = event?.task
        if (task != null) {
            viewModel.taskDetail.value?.let { taskDetail ->
                if (task.id == taskDetail.id) {
                    task.let { it1 ->
                        viewModel.originalTask.postValue(it1)
                        viewModel._taskDetail.postValue(it1)
                    }
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskDoneEvent(event: LocalEvents.TaskDoneEvent?) {
        val task = event?.task
        if (task != null) {
            viewModel.taskDetail.value?.let { taskDetail ->
                if (task.id == taskDetail.id) {
                    task.let { it1 ->
                        viewModel.originalTask.postValue(it1)
                        viewModel._taskDetail.postValue(it1)
                    }
                }
            }
        }
    }


    override fun onNavigationResult(result: BackNavigationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            when (result.requestCode) {
                DONE_REQUEST_CODE -> {
                    val isBeingDone = result.data?.getBoolean("isBeingDone")
                    if (isBeingDone == true) {
                        shortToastNow("Marking task as done...")
                        mViewDataBinding.doneBtn.isEnabled = false
                        mViewDataBinding.doneBtn.isClickable = false
                        mViewDataBinding.doneBtn.alpha = 0.6f
                        mViewDataBinding.taskForwardBtn.isEnabled = false
                        mViewDataBinding.taskForwardBtn.isClickable = false
                        mViewDataBinding.taskForwardBtn.alpha = 0.6f
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun ImageObject(event: LocalEvents.ImageFile) {
        mViewDataBinding.viewPager.setCurrentItem(1, true)
        Handler(Looper.getMainLooper()).postDelayed({
            EventBus.getDefault().postSticky(LocalEvents.OpenKeyboardWithFile(event.item,event.type))
        }, 300)
    }
}