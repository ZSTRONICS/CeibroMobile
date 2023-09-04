package com.zstronics.ceibro.ui.dashboard

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.launchActivityWithFinishAffinity
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BackNavigationResult
import com.zstronics.ceibro.base.navgraph.BackNavigationResultListener
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.data.repos.chat.messages.socket.SocketEventTypeResponse
import com.zstronics.ceibro.data.repos.task.models.AllFilesUploadedSocketEventResponse
import com.zstronics.ceibro.data.repos.task.models.CommentsFilesUploadedSocketEventResponse
import com.zstronics.ceibro.data.repos.task.models.FileUploadedEventResponse
import com.zstronics.ceibro.data.repos.task.models.FileUploadingProgressEventResponse
import com.zstronics.ceibro.databinding.FragmentDashboardBinding
import com.zstronics.ceibro.ui.enums.EventType
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.ui.tasks.v2.hidden_tasks.TaskHiddenFragment
import com.zstronics.ceibro.ui.tasks.v2.taskfromme.TaskFromMeFragment
import com.zstronics.ceibro.ui.tasks.v2.tasktome.TaskToMeFragment
import dagger.hilt.android.AndroidEntryPoint
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
    override fun onClick(id: Int) {
        when (id) {
            R.id.createNewTaskBtn -> {
                navigateForResult(R.id.newTaskV2Fragment, CREATE_NEW_TASK_CODE, bundleOf())
            }

            R.id.profileImg -> navigateToProfile()
            R.id.friendsReqBtn -> navigateToConnections()
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

        when (btnID) {
            R.id.toMeBtn -> {
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
                viewState.locationSelected.value = true
            }

            R.id.projectsBtn -> {
                viewState.projectsSelected.value = true
            }
        }
    }
    private var socketEventsInitiated = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.updateRootUnread(requireActivity())
        if (!socketEventsInitiated) {
            socketEventsInitiating()
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

        SocketHandler.getSocket()?.on(SocketHandler.CHAT_EVENT_REP_OVER_SOCKET) { args ->
            val navHostFragment =
                activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment)
            val fragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
            when {
                args[0].toString().contains(EventType.RECEIVE_MESSAGE.name) -> {
                    println("RECEIVE_MESSAGE")
                }
            }

            if (fragment is DashboardFragment) {
//                val gson = Gson()
//                val messageType = object : TypeToken<SocketReceiveMessageResponse>() {}.type
//                val message: SocketReceiveMessageResponse = gson.fromJson(args[0].toString(), messageType)
            }
        }
        viewModel.notificationEvent.observe(viewLifecycleOwner, ::onCreateNotification)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        val handler = Handler()
        handler.postDelayed({
            changeSelectedTab(R.id.toMeBtn, false)
        }, 100)
    }
    private fun socketEventsInitiating(){
        if (SocketHandler.getSocket() == null || SocketHandler.getSocket()?.connected() == false) {
            println("Heartbeat, Dashboard")
            SocketHandler.setSocket()
            SocketHandler.establishConnection()
        }

//        viewModel.handleSocketEvents()
        handleFileUploaderSocketEvents()
        socketEventsInitiated = true
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

    companion object {
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
            createNotification(
                channelId = event.moduleId,
                chanelName = event.moduleName,
                notificationTitle = event.notificationTitle,
                isOngoing = event.isOngoing,
                indeterminate = event.indeterminate,
                notificationIcon = event.notificationIcon
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshTasksEvent(event: LocalEvents.RefreshTasksEvent?) {
        viewModel.updateRootUnread(requireActivity())
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
}