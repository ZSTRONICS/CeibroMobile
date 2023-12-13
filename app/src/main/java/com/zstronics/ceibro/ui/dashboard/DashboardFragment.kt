package com.zstronics.ceibro.ui.dashboard

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.MainThread
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.onesignal.OneSignal
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.KEY_SOCKET_OBSERVER_SET
import com.zstronics.ceibro.base.extensions.launchActivityWithFinishAffinity
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BackNavigationResult
import com.zstronics.ceibro.base.navgraph.BackNavigationResultListener
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.models.tasks.LocalTaskDetail
import com.zstronics.ceibro.data.repos.auth.login.User
import com.zstronics.ceibro.data.repos.chat.messages.socket.SocketEventTypeResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.task.models.AllFilesUploadedSocketEventResponse
import com.zstronics.ceibro.data.repos.task.models.CommentsFilesUploadedSocketEventResponse
import com.zstronics.ceibro.data.repos.task.models.FileUploadedEventResponse
import com.zstronics.ceibro.data.repos.task.models.FileUploadingProgressEventResponse
import com.zstronics.ceibro.data.repos.task.models.TopicsV2DatabaseEntity
import com.zstronics.ceibro.databinding.FragmentDashboardBinding
import com.zstronics.ceibro.ui.dashboard.bottomSheet.UnSyncTaskBottomSheet
import com.zstronics.ceibro.ui.locationv2.LocationsV2Fragment
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawing.DrawingsV2Fragment
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import com.zstronics.ceibro.ui.projectv2.ProjectsV2Fragment
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.ui.tasks.v2.hidden_tasks.TaskHiddenFragment
import com.zstronics.ceibro.ui.tasks.v2.taskfromme.TaskFromMeFragment
import com.zstronics.ceibro.ui.tasks.v2.tasktome.TaskToMeFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@AndroidEntryPoint
class DashboardFragment :
    BaseNavViewModelFragment<FragmentDashboardBinding, IDashboard.State, DashboardVM>(),
    BackNavigationResultListener {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: DashboardVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_dashboard
    override fun toolBarVisibility(): Boolean = false
    private var taskToMeFragmentInstance: TaskToMeFragment? = null
    private var taskFromMeFragmentInstance: TaskFromMeFragment? = null
    private var taskHiddenFragmentInstance: TaskHiddenFragment? = null
    private var locationFragmentInstance: LocationsV2Fragment? = null
    private var drawingFragmentInstance: DrawingsV2Fragment? = null
    private var projectsV2FragmentInstance: ProjectsV2Fragment? = null
    private var socketEventsInitiated = false
    private var appStartWithInternet = true
    private var connectivityStatus = "Available"

    override fun onClick(id: Int) {

        when (id) {
            R.id.createNewTaskBtn -> {
                navigateForResult(R.id.newTaskV2Fragment, CREATE_NEW_TASK_CODE, bundleOf())
            }

            R.id.profileImg -> navigateToProfile()
            R.id.friendsReqBtn -> navigateToConnections()
            R.id.feedbackBtn -> showFeedbackDialog()
            R.id.toMeBtn -> {
                changeSelectedTab(R.id.toMeBtn, false)
            }

            R.id.fromMeBtn -> {
                changeSelectedTab(R.id.fromMeBtn, false)
            }

            R.id.hiddenBtn -> {
                changeSelectedTab(R.id.hiddenBtn, false)
            }

            R.id.locationBtn -> {
                changeSelectedTab(R.id.locationBtn, false)
            }

            R.id.projectsBtn -> {
                changeSelectedTab(R.id.projectsBtn, false)
            }

            R.id.draftTaskCounter -> {
                showUnSyncTaskBottomSheet(viewModel.sessionManager.getUser().value)
            }

            R.id.sync -> {
                if (mViewDataBinding.draftTaskCounter.visibility == View.VISIBLE) {
                    showUnSyncTaskBottomSheet(viewModel.sessionManager.getUser().value)
                }
            }
        }
    }

    private fun changeSelectedTab(btnID: Int, newTask: Boolean) {
        viewState.toMeSelected.value = false
        viewState.fromMeSelected.value = false
        viewState.hiddenSelected.value = false
        viewState.locationSelected.value = false
        viewState.projectsSelected.value = false
        mViewDataBinding.toMeLine.visibility = View.GONE
        mViewDataBinding.fromMeLine.visibility = View.GONE
        mViewDataBinding.hiddenLine.visibility = View.GONE
        mViewDataBinding.locationLine.visibility = View.GONE
        mViewDataBinding.projectsLine.visibility = View.GONE

        when (btnID) {
            R.id.toMeBtn -> {
                mViewDataBinding.createNewTaskBtn.visibility = View.VISIBLE
                viewState.toMeSelected.value = true
                if (taskToMeFragmentInstance == null) {
                    taskToMeFragmentInstance = TaskToMeFragment()
                }
                childFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, taskToMeFragmentInstance!!)
                    .commit()
                mViewDataBinding.toMeLine.visibility = View.VISIBLE
            }

            R.id.fromMeBtn -> {
                mViewDataBinding.createNewTaskBtn.visibility = View.VISIBLE
                viewState.fromMeSelected.value = true
                if (newTask) {
                    taskFromMeFragmentInstance = TaskFromMeFragment()
                } else {
                    if (taskFromMeFragmentInstance == null) {
                        taskFromMeFragmentInstance = TaskFromMeFragment()
                    }
                }
                childFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, taskFromMeFragmentInstance!!)
                    .commit()
                mViewDataBinding.fromMeLine.visibility = View.VISIBLE
            }

            R.id.hiddenBtn -> {
                mViewDataBinding.createNewTaskBtn.visibility = View.VISIBLE
                viewState.hiddenSelected.value = true
                if (taskHiddenFragmentInstance == null) {
                    taskHiddenFragmentInstance = TaskHiddenFragment()
                }
                childFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, taskHiddenFragmentInstance!!)
                    .commit()
                mViewDataBinding.hiddenLine.visibility = View.VISIBLE
            }

            R.id.locationBtn -> {
                viewState.projectsSelected.value = true
                mViewDataBinding.createNewTaskBtn.visibility = View.GONE

                if (locationFragmentInstance == null) {
                    locationFragmentInstance = LocationsV2Fragment()
                }
                childFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, locationFragmentInstance!!)
                    .commit()
//
//                childFragmentManager.beginTransaction()
//                    .replace(R.id.fragment_container, DrawingsV2Fragment())
//                    .commit()


                mViewDataBinding.locationLine.visibility = View.VISIBLE


            }

            R.id.projectsBtn -> {
                viewState.projectsSelected.value = true
                mViewDataBinding.createNewTaskBtn.visibility = View.GONE

                if (projectsV2FragmentInstance == null) {
                    projectsV2FragmentInstance = ProjectsV2Fragment()
                }
                childFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, projectsV2FragmentInstance!!)
                    .commit()
                mViewDataBinding.projectsLine.visibility = View.VISIBLE

            }
        }
    }

    private fun showUnSyncTaskBottomSheet(user: User?) {

        val coroutineScope = viewLifecycleOwner.lifecycleScope
        coroutineScope.launch(Dispatchers.Main) {
            val topic: TopicsV2DatabaseEntity? = viewModel.getTopicList()
            val contactsFromDatabase: List<AllCeibroConnections.CeibroConnection> =
                viewModel.getContactsList()
            val list = viewModel.getDraftTasks()


            val offlineTaskData = ArrayList<LocalTaskDetail>()

            list.forEach { item ->

                val contactList = mutableListOf<String>() // Create a new contactList for each item

                if (item.assignedToState.isNotEmpty()) {
                    item.assignedToState.forEach {


                        user?.let { user ->
                            if (it.phoneNumber == user.phoneNumber) {
                                contactList.add(user.firstName)
                            }
                        }


                        contactsFromDatabase.forEach { contact ->
                            if (contact.phoneNumber == it.phoneNumber) {
                                contactList.add(contact.contactFullName ?: it.phoneNumber)
                            }
                        }
                    }
                }

                if (item.invitedNumbers.isNotEmpty()) {
                    item.invitedNumbers.forEach {
                        contactsFromDatabase.forEach { contact ->
                            if (contact.phoneNumber == it) {
                                contactList.add(contact.contactFullName ?: it)
                            }
                        }
                    }
                }

                var topicName = topic?.topicsData?.recentTopics?.find { it.id == item.topic }?.topic
                if (topicName.isNullOrEmpty()) {
                    topicName = topic?.topicsData?.allTopics?.find { it.id == item.topic }?.topic
                }

                offlineTaskData.add(
                    LocalTaskDetail(
                        topicName,
                        contactList,
                        item.dueDate,
                        item.filesData?.size ?: 0,
                        item.isDraftTaskCreationFailed,
                        item.taskCreationFailedError
                    )
                )
            }

            val sheet = UnSyncTaskBottomSheet(offlineTaskData)
            sheet.isCancelable = true
            sheet.show(childFragmentManager, "UnSyncTaskBottomSheet")
        }


    }


    @MainThread
    private fun updateDraftRecord(unSyncedTasks: Int) {


        if (unSyncedTasks > 0) {
            mViewDataBinding.sync.visibility = View.VISIBLE
            mViewDataBinding.draftTaskCounter.visibility = View.VISIBLE
            if (unSyncedTasks > 99) {
                mViewDataBinding.draftTaskCounter.post {
                    mViewDataBinding.draftTaskCounter.text = "99+"
                }
            } else {
                mViewDataBinding.draftTaskCounter.post {
                    mViewDataBinding.draftTaskCounter.text = unSyncedTasks.toString()
                }
            }
        } else {
            mViewDataBinding.sync.visibility = View.GONE
            mViewDataBinding.draftTaskCounter.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        val coroutineScope = viewLifecycleOwner.lifecycleScope
        coroutineScope.launch(Dispatchers.IO) {
            val unSyncedTasks = viewModel.getDraftTasks()
            withContext(Dispatchers.Main) {
                updateDraftRecord(unSyncedTasks.size)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
//        val callback = object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                showCloseAppDialog()
//            }
//        }
//
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        viewModel.updateRootUnread(requireActivity())



        HiltBaseViewModel.syncDraftRecords.observe(viewLifecycleOwner) {
            updateDraftRecord(it)
        }


        when (connectivityStatus) {     // this needs to be here to check internet last state, because if user navigate from dashboard to any other screen
            // then comes back to this UI, then this object will keep the sync icon state visible to other users
            "Available" -> {
                mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_good_connection)
            }

            "Lost" -> {
                mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_no_connection)
            }

            "Losing" -> {
                mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_poor_connection)
            }

            "Unavailable" -> {
                mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_no_connection)
            }
        }

        if (!socketEventsInitiated) {
            OneSignal.promptForPushNotifications()
            socketEventsInitiating()
            changeSelectedTab(R.id.toMeBtn, false)
        }

        SearchDataSingleton.searchString = MutableLiveData("")
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        sharedViewModel.isToMeUnread.observe(viewLifecycleOwner) { isUnread ->
            if (isUnread) {
                mViewDataBinding.toMeUnreadBadge.visibility = View.VISIBLE
            } else {
                mViewDataBinding.toMeUnreadBadge.visibility = View.GONE
            }
        }
        sharedViewModel.isFromMeUnread.observe(viewLifecycleOwner) { isUnread ->
            if (isUnread) {
                mViewDataBinding.fromMeUnreadBadge.visibility = View.VISIBLE
            } else {
                mViewDataBinding.fromMeUnreadBadge.visibility = View.GONE
            }
        }
        sharedViewModel.isHiddenUnread.observe(viewLifecycleOwner) { isUnread ->
            if (isUnread) {
                mViewDataBinding.hiddenUnreadBadge.visibility = View.VISIBLE
            } else {
                mViewDataBinding.hiddenUnreadBadge.visibility = View.GONE
            }
        }
        sharedViewModel.isConnectedToServer.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                /*when (connectivityStatus) {
                    "Available" -> {
                        mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_good_connection)
                    }

                    "Lost" -> {
                        mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_no_connection)
                    }

                    "Losing" -> {
                        mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_poor_connection)
                    }

                    "Unavailable" -> {
                        mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_no_connection)
                    }
                }*/
//                changeSyncIcon(networkConnectivityObserver.isNetworkAvailable(), isConnected)
            } else {
//                changeSyncIcon(networkConnectivityObserver.isNetworkAvailable(), isConnected)
            }
        }


//        SocketHandler.getSocket()?.on(SocketHandler.CHAT_EVENT_REP_OVER_SOCKET) { args ->
//            val navHostFragment =
//                activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment)
//            val fragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
//            when {
//                args[0].toString().contains(EventType.RECEIVE_MESSAGE.name) -> {
//                    println("RECEIVE_MESSAGE")
//                }
//            }
//
//            if (fragment is DashboardFragment) {
////                val gson = Gson()
////                val messageType = object : TypeToken<SocketReceiveMessageResponse>() {}.type
////                val message: SocketReceiveMessageResponse = gson.fromJson(args[0].toString(), messageType)
//            }
//        }
        viewModel.notificationEvent.observe(viewLifecycleOwner, ::onCreateNotification)

        val socketObserversSet = viewModel.sessionManager.getBooleanValue(KEY_SOCKET_OBSERVER_SET)
        if (!socketObserversSet) {
            viewModel.handleSocketEvents()
            handleSocketReSyncDataEvent()
        }
    }

    private fun changeSyncIcon(networkAvailable: Boolean, socketConnected: Boolean?, size: Int) {
        if (networkAvailable) {
            if (size > 0) {
                mViewDataBinding.sync.visibility = View.VISIBLE
            } else {
                mViewDataBinding.sync.visibility = View.GONE
            }
            mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_good_connection)
        } else {
            mViewDataBinding.sync.visibility = View.VISIBLE
            mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_no_connection)
        }
    }

    private fun setConnectivityIcon() {
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        lifecycleScope.launch {
            networkConnectivityObserver.observe().collect { connectionStatus ->
                println("Heartbeat, $connectionStatus")
                when (connectionStatus) {

                    NetworkConnectivityObserver.Status.Losing -> {
                        connectivityStatus = "Losing"
//                        mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_poor_connection)
                        changeSyncIcon(
                            false,
                            SocketHandler.getSocket()?.connected(),
                            viewModel.getDraftTasks().size
                        )
                    }

                    NetworkConnectivityObserver.Status.Available -> {
                        println(
                            "Heartbeat, Internet observer SocketConnected: ${
                                SocketHandler.getSocket()?.connected()
                            }"
                        )
                        println("Heartbeat, Internet observer Socket object == ${SocketHandler.getSocket()}")
                        if (SocketHandler.getSocket() == null || SocketHandler.getSocket()
                                ?.connected() == null || SocketHandler.getSocket()
                                ?.connected() == false
                        ) {
                            println("Heartbeat, Internet observer")
                            if (SocketHandler.getSocket() == null || SocketHandler.getSocket()
                                    ?.connected() == null ||
                                !appStartWithInternet || CookiesManager.socketOnceConnected.value == false
                            ) {
                                println("Heartbeat, Internet observer Socket setting new")
                                if (CookiesManager.jwtToken.isNullOrEmpty()) {
                                    viewModel.sessionManager.setUser()
                                    viewModel.sessionManager.setToken()
                                }
                                SocketHandler.setActivityContext(requireActivity())
                                SocketHandler.closeConnectionAndRemoveObservers()
                                SocketHandler.setSocket()
                                appStartWithInternet = true
                            }
                            SocketHandler.establishConnection()
                        }
                        connectivityStatus = "Available"
                        changeSyncIcon(
                            true,
                            SocketHandler.getSocket()?.connected(),
                            viewModel.getDraftTasks().size
                        )
//                        mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_good_connection)
                    }

                    NetworkConnectivityObserver.Status.Lost -> {
                        connectivityStatus = "Lost"
//                        mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_no_connection)
                        changeSyncIcon(
                            false,
                            SocketHandler.getSocket()?.connected(),
                            viewModel.getDraftTasks().size
                        )
                    }

                    NetworkConnectivityObserver.Status.Unavailable -> {
                        connectivityStatus = "Unavailable"
//                        mViewDataBinding.sync.setImageResource(R.drawable.icon_sync_no_connection)
                        changeSyncIcon(
                            false,
                            SocketHandler.getSocket()?.connected(),
                            viewModel.getDraftTasks().size
                        )
                    }

                }
            }
        }
    }


    private fun socketEventsInitiating() {
//        if (SocketHandler.getSocket() == null || SocketHandler.getSocket()?.connected() == false) {
//            println("Heartbeat, Dashboard")
//            SocketHandler.setActivityContext(requireActivity())
//            SocketHandler.setSocket()
//            SocketHandler.establishConnection()
//        }
        if (networkConnectivityObserver.isNetworkAvailable().not()) {
            appStartWithInternet = false
        }
        viewModel.sessionManager.saveBooleanValue(KEY_SOCKET_OBSERVER_SET, true)
        viewModel.handleSocketEvents()
        handleSocketReSyncDataEvent()
//        handleFileUploaderSocketEvents()
        if (networkConnectivityObserver.isNetworkAvailable()) {
            viewModel.launch {
                viewModel.syncDraftTask(requireContext())
            }
        }
        setConnectivityIcon()
        socketEventsInitiated = true

        SocketHandler.emitIsSyncRequired()
        startPeriodicContactSyncWorker(requireContext())
    }

    private fun handleFileUploaderSocketEvents() {

        SocketHandler.getSocket()?.on(SocketHandler.CEIBRO_LIVE_EVENT_BY_SERVER) { args ->
            val gson = Gson()
            val arguments = args[0].toString()
            val socketData: SocketEventTypeResponse = gson.fromJson(
                arguments,
                object : TypeToken<SocketEventTypeResponse>() {}.type
            )
            if (socketData.module == "task") {
                when (socketData.eventType) {
                    SocketHandler.FileAttachmentEvents.FILE_UPLOAD_PROGRESS.name -> {
                        val fileProgress = gson.fromJson<FileUploadingProgressEventResponse>(
                            arguments,
                            object : TypeToken<FileUploadingProgressEventResponse>() {}.type
                        ).data

                        val (notificationManager, builder) = createNotification(
                            fileProgress?.fileId,
                            "${socketData.module}${fileProgress?.fileId}",
                            notificationTitle = "${fileProgress?.file?.fileName} uploading"
                        )

                        val totalSize = fileProgress?.totalSize ?: 100
                        val progress = ((fileProgress?.uploadedSize?.div(totalSize) ?: 1) * 100)
                        builder.setProgress(100, progress, false)
                        notificationManager.notify(fileProgress?.fileId.hashCode(), builder.build())
                    }

                    SocketHandler.FileAttachmentEvents.FILE_UPLOADED.name -> {
                        val fileUploaded = gson.fromJson<FileUploadedEventResponse>(
                            arguments,
                            object : TypeToken<FileUploadedEventResponse>() {}.type
                        ).data

                        EventBus.getDefault().post(
                            fileUploaded?.id?.let {
                                LocalEvents.CreateNotification(
                                    moduleName = socketData.module,
                                    moduleId = it,
                                    notificationTitle = "${fileUploaded.fileName} uploaded",
                                    isOngoing = false,
                                    indeterminate = false
                                )
                            }
                        )
                        viewModel.launch {
                            fileUploaded?.let {
                                viewModel.fileAttachmentsDataSource.insertFile(
                                    it
                                )
                                EventBus.getDefault().post(LocalEvents.AllFilesUploaded)
                            }
                        }
                    }

                    SocketHandler.FileAttachmentEvents.FILES_UPLOAD_COMPLETED.name -> {

                        requireActivity().getSystemService(NotificationManager::class.java)
                            ?.cancelAll()
                        val allFilesUploaded = gson.fromJson<AllFilesUploadedSocketEventResponse>(
                            arguments,
                            object : TypeToken<AllFilesUploadedSocketEventResponse>() {}.type
                        ).data

                        //// post local event to show notification
                        EventBus.getDefault().post(
                            LocalEvents.CreateNotification(
                                moduleName = socketData.module,
                                moduleId = allFilesUploaded.moduleId,
                                notificationTitle = "All Files uploaded for ${socketData.module}",
                                isOngoing = false,
                                indeterminate = false
                            )
                        )

                        viewModel.launch {
                            viewModel.fileAttachmentsDataSource.insertAll(
                                allFilesUploaded.files
                            )
                            EventBus.getDefault().post(LocalEvents.AllFilesUploaded)
                        }
                    }
                }
            } else if (socketData.module == "SubTaskComments") {
                if (socketData.eventType == SocketHandler.TaskEvent.COMMENT_WITH_FILES.name) {
                    val commentWithFile =
                        gson.fromJson<CommentsFilesUploadedSocketEventResponse>(
                            arguments,
                            object : TypeToken<CommentsFilesUploadedSocketEventResponse>() {}.type
                        ).data
                    viewModel.launch {
                        viewModel.localSubTask.addFilesUnderComment(
                            commentWithFile.subTaskId,
                            commentWithFile,
                            commentWithFile.id
                        )
                    }
                    EventBus.getDefault()
                        .post(LocalEvents.NewSubTaskComment(commentWithFile, commentWithFile.id))
                    requireActivity().getSystemService(NotificationManager::class.java)
                        ?.cancelAll()
                }
            }
        }
    }


    private fun navigateToProfile() {
        navigate(R.id.profileFragment)
    }

    private fun navigateToConnections() {
        navigate(R.id.MyConnectionV2Fragment)
    }

    private fun showFeedbackDialog() {
        val sheet = FeedbackDialogSheet()
        sheet.onEstonianFormBtn = {
            val url = "https://forms.gle/DohcCaEzU6iY8bqy5"     //Estonian form link
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        sheet.onEnglishFormBtn = {
            val url = "https://forms.gle/nvdQU7RbKofmPP3LA"     //English form link
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        sheet.isCancelable = false
        sheet.show(childFragmentManager, "FeedbackDialogSheet")
    }

    companion object {
        var index = 0
        var selectedItem: Int = R.id.nav_home
        val CREATE_NEW_TASK_CODE = 1122
    }

    private fun onCreateNotification(event: LocalEvents.CreateNotification?) {
        if (event != null) {
            createNotification(
                event.moduleId,
                "${event.moduleName}${event.moduleId}",
                notificationTitle = event.notificationTitle,
                isOngoing = event.isOngoing,
                indeterminate = event.indeterminate,
                notificationIcon = event.notificationIcon
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutUserEvent(event: LocalEvents.LogoutUserEvent) {
        viewModel.endUserSession(requireContext())
        shortToastNow("Session expired, please login")
        launchActivityWithFinishAffinity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = true
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.onboarding_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.loginFragment
            )
        }
        Thread { activity?.let { Glide.get(it).clearDiskCache() } }.start()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCreateSimpleNotification(event: LocalEvents.CreateSimpleNotification?) {
        event?.let {
            if (it.isTaskCreated) {
                run {
//                    val notificationHelper = NotificationHelper.getInstance(requireContext())
//                    val newData = NotificationTaskData(
//                        avatar = "",
//                        creator = "ABC",
//                        description = "",
//                        taskId = event.moduleId,
//                        topic = event.notificationTitle
//                    )
//                    notificationHelper.createNotification(
//                        newData,
//                        notificationId = index++,
//                        notificationType = "newTask",
//                        title = event.notificationTitle,
//                        message = "",
//                        context = requireContext()
//                    )
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshTasksData(event: LocalEvents.RefreshTasksData?) {
        viewModel.updateRootUnread(requireActivity())
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onInitSocketEventCallBack(event: LocalEvents.InitSocketEventCallBack?) {
        viewModel.handleSocketEvents()
        handleSocketReSyncDataEvent()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    override fun onNavigationResult(result: BackNavigationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            when (result.requestCode) {
                CREATE_NEW_TASK_CODE -> {
                    changeSelectedTab(R.id.fromMeBtn, true)
                }
            }
        }
    }


    private fun showCloseAppDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.close_app))
            .setMessage(getString(R.string.are_you_sure_you_want_to_close_the_app))
            .setPositiveButton(getString(R.string.yes)) { dialog: DialogInterface, _: Int ->
                finishActivity()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .show()
    }
}