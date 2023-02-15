package com.zstronics.ceibro.ui.dashboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.data.repos.chat.messages.socket.SocketEventTypeResponse
import com.zstronics.ceibro.data.repos.task.models.AllFilesUploadedSocketEventResponse
import com.zstronics.ceibro.data.repos.task.models.FileUploadedEventResponse
import com.zstronics.ceibro.data.repos.task.models.FileUploadingProgressEventResponse
import com.zstronics.ceibro.data.repos.task.models.SocketTaskCreatedResponse
import com.zstronics.ceibro.databinding.FragmentDashboardBinding
import com.zstronics.ceibro.ui.chat.ChatFragment
import com.zstronics.ceibro.ui.enums.EventType
import com.zstronics.ceibro.ui.home.HomeFragment
import com.zstronics.ceibro.ui.projects.ProjectsFragment
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.ui.tasks.MainTasksFragment
import com.zstronics.ceibro.ui.works.WorksFragment
import dagger.hilt.android.AndroidEntryPoint


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
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*Set socket and establish connection*/
        SocketHandler.setSocket()
        SocketHandler.establishConnection()
        viewModel.handleSocketEvents()
        handleFileUploaderSocketEvents()
//        setBadgeOnChat(R.id.nav_chat, 4)

        mViewDataBinding.bottomNavigation.setOnNavigationItemSelectedListener(navListener)
//        mViewDataBinding.bottomNavigation.selectedItemId = R.id.nav_home
//        childFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment())
//            .commit()

        viewState.selectedItem.observe(viewLifecycleOwner) {
            mViewDataBinding.bottomNavigation.selectedItemId = selectedItem
        }


        SocketHandler.getSocket().on(SocketHandler.CHAT_EVENT_REP_OVER_SOCKET) { args ->
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
    }

    private fun handleFileUploaderSocketEvents() {

        SocketHandler.getSocket().on(SocketHandler.CEIBRO_LIVE_EVENT_BY_SERVER) { args ->
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
                            "${socketData.module}${fileProgress?.fileId}"
                        )

                        val totalSize = fileProgress?.totalSize ?: 100
                        val progress = ((fileProgress?.uploadedSize?.div(totalSize) ?: 1) * 100)
                        builder.setProgress(100, progress, false)
                        notificationManager.notify(0, builder.build())
                    }
                    SocketHandler.FileAttachmentEvents.FILE_UPLOADED.name -> {
                        val fileUploaded = gson.fromJson<FileUploadedEventResponse>(
                            arguments,
                            object : TypeToken<FileUploadedEventResponse>() {}.type
                        ).data

                        val (notificationManager, builder) = createNotification(
                            fileUploaded?.id,
                            "${socketData.module}${fileUploaded?.id}",
                            "File uploaded",
                            false
                        )
                        viewModel.launch {
                            fileUploaded?.let {
                                viewModel.fileAttachmentsDataSource.insertFile(
                                    it
                                )
                            }
                        }
                    }
                    SocketHandler.FileAttachmentEvents.FILES_UPLOAD_COMPLETED.name -> {
                        val allFilesUploaded = gson.fromJson<AllFilesUploadedSocketEventResponse>(
                            arguments,
                            object : TypeToken<AllFilesUploadedSocketEventResponse>() {}.type
                        ).data

                        val (notificationManager, builder) = createNotification(
                            allFilesUploaded.moduleId,
                            "${socketData.module}${allFilesUploaded.moduleId}",
                            "All Files uploaded",
                            false
                        )
                        viewModel.launch {
                            viewModel.fileAttachmentsDataSource.insertAll(
                                allFilesUploaded.files
                            )
                        }
                    }
                }
            }
        }
    }

    private fun createNotification(
        channelId: String?,
        chanelName: String,
        notificationTitle: String = "Uploading file",
        isOngoing: Boolean = true
    ): Pair<NotificationManager, NotificationCompat.Builder> {
        // Create a notification channel (for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                requireActivity().getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                chanelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        // Create a notification builder
        val builder = NotificationCompat.Builder(requireContext(), channelId ?: "channel_id")
            .setSmallIcon(R.drawable.ic_upload)
            .setContentTitle(notificationTitle)
            .setOngoing(isOngoing)
            .setOnlyAlertOnce(true)
        if (isOngoing) {
            builder.setProgress(100, 1, false)
        }
        // Show the notification
        val notificationManager =
            requireActivity().getSystemService(NotificationManager::class.java)
        notificationManager.notify(0, builder.build())
        return Pair(notificationManager, builder)
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
        val badge = mViewDataBinding.bottomNavigation.getOrCreateBadge(menuItemId)
        badge.isVisible = true
        badge.number = number
        badge.backgroundColor = resources.getColor(R.color.appRed)
    }

    private fun navigateToProfile() {
        launchActivity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = false
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.profileFragment
            )
        }
    }

    private fun navigateToConnections() {
        launchActivity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = false
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.connectionsFragment
            )
        }
    }

    companion object {
        var selectedItem: Int = R.id.nav_home
    }
}