package com.zstronics.ceibro.ui.dashboard

import android.app.NotificationManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.launchActivityWithFinishAffinity
import com.zstronics.ceibro.base.extensions.shortToastNow
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
import com.zstronics.ceibro.ui.admin.admins.AdminsFragment
import com.zstronics.ceibro.ui.chat.ChatFragment
import com.zstronics.ceibro.ui.enums.EventType
import com.zstronics.ceibro.ui.home.HomeFragment
import com.zstronics.ceibro.ui.projects.ProjectsFragment
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.ui.tasks.MainTasksFragment
import com.zstronics.ceibro.ui.tasks.v2.tasktome.TaskToMeFragment
import com.zstronics.ceibro.ui.works.WorksFragment
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@AndroidEntryPoint
class DashboardFragment :
    BaseNavViewModelFragment<FragmentDashboardBinding, IDashboard.State, DashboardVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: DashboardVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_dashboard
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
//            R.id.profileIcon -> navigate(R.id.profileFragment)
            R.id.profileImg -> navigateToProfile()
            R.id.friendsReqBtn -> navigateToConnections()
            R.id.toMeBtn -> {
                changeSelectedTab(R.id.toMeBtn)
            }
            R.id.fromMeBtn -> {
                changeSelectedTab(R.id.fromMeBtn)
            }
            R.id.canceledBtn -> {
                changeSelectedTab(R.id.canceledBtn)
            }
            R.id.locationBtn -> {
                changeSelectedTab(R.id.locationBtn)
            }
            R.id.projectsBtn -> {
                changeSelectedTab(R.id.projectsBtn)
            }
        }
    }

    private fun changeSelectedTab(btnID: Int) {
        viewState.toMeSelected.value = false
        viewState.fromMeSelected.value = false
        viewState.canceledSelected.value = false
        viewState.locationSelected.value = false
        viewState.projectsSelected.value = false

        when (btnID) {
            R.id.toMeBtn -> {
                viewState.toMeSelected.value = true
                childFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TaskToMeFragment())
                    .commit()
            }
            R.id.fromMeBtn -> {
                viewState.fromMeSelected.value = true
            }
            R.id.canceledBtn -> {
                viewState.canceledSelected.value = true
            }
            R.id.locationBtn -> {
                viewState.locationSelected.value = true
            }
            R.id.projectsBtn -> {
                viewState.projectsSelected.value = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*Set socket and establish connection*/
        SocketHandler.setSocket()
        SocketHandler.establishConnection()
        viewModel.handleSocketEvents()
        handleFileUploaderSocketEvents()

        val handler = Handler()
        handler.postDelayed(Runnable {
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TaskToMeFragment())
                .commit()
        }, 20)


        mViewDataBinding.bottomNavigation1.setOnNavigationItemSelectedListener(navListener)
//        mViewDataBinding.bottomNavigation.selectedItemId = R.id.nav_home
//        childFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment())
//            .commit()

        viewState.selectedItem.observe(viewLifecycleOwner) {
            mViewDataBinding.bottomNavigation1.selectedItemId = selectedItem
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

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        selectedItem = item.itemId
        val selectedFragment: Fragment = when (item.itemId) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_chat -> ChatFragment()
            R.id.nav_tasks -> MainTasksFragment()
            R.id.nav_projects -> ProjectsFragment()
            else -> WorksFragment()
        }

        childFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment)
            .commit()
        true
    }

    private fun setBadgeOnChat(menuItemId: Int, number: Int) {
        val badge = mViewDataBinding.bottomNavigation1.getOrCreateBadge(menuItemId)
        badge.isVisible = true
        badge.number = number
        badge.backgroundColor = resources.getColor(R.color.appRed)
    }

    private fun navigateToProfile() {
        navigate(R.id.profileFragment)
    }

    private fun navigateToConnections() {
        navigate(R.id.MyConnectionV2Fragment)
    }

    companion object {
        var selectedItem: Int = R.id.nav_home
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCreateNotification(event: LocalEvents.CreateNotification) {
        createNotification(
            event.moduleId,
            "${event.moduleName}${event.moduleId}",
            notificationTitle = event.notificationTitle,
            isOngoing = event.isOngoing,
            indeterminate = event.indeterminate,
            notificationIcon = event.notificationIcon
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutUserEvent(event: LocalEvents.LogoutUserEvent) {
        viewModel.endUserSession()
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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}