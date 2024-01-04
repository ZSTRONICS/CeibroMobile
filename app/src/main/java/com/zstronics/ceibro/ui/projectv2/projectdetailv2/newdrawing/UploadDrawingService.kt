package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.dao.FloorsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroDownloadDrawingV2
import com.zstronics.ceibro.data.repos.projects.ProjectRepository
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class UploadDrawingService : Service() {
    private val CHANNEL_ID = "drawing_upload_channel"
    private val CHANNEL_NAME = "Drawing Progress Channel"
    private var apiCounter = 0
    private var mContext: Context? = null
    private val uploadingDrawingNotificationID = 3

    @Inject
    lateinit var projectRepository: ProjectRepository

    @Inject
    lateinit var groupsV2Dao: GroupsV2Dao

    @Inject
    lateinit var floorsV2Dao: FloorsV2Dao

    @Inject
    lateinit var downloadedDrawingV2Dao: DownloadedDrawingV2Dao

    @Inject
    lateinit var networkConnectivityObserver: NetworkConnectivityObserver

    override fun onCreate() {

        super.onCreate()
        println("Service Status .. onCreate state...")
        mContext = this

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val request = intent?.getStringExtra("uploadRequest")
        when (request) {
            "uploadRequest" -> {
                val data = NewDrawingV2VM.uploadDataClass
                val dataParams = NewDrawingV2VM.uploadDataParams
                startForeground(
                    uploadingDrawingNotificationID,
                    createIndeterminateNotificationForFileUpload(
                        context = this,
                        channelId = CHANNEL_ID,
                        channelName = CHANNEL_NAME,
                        notificationTitle = "uploading drawing",
                        notificationID = uploadingDrawingNotificationID
                    )
                )
                uploadDrawing(data, dataParams)
            }
        }
        return START_STICKY
    }

    private fun createIndeterminateNotificationForFileUpload(
        context: UploadDrawingService,
        channelId: String,
        channelName: String,
        notificationTitle: String,
        isOngoing: Boolean = true,
        indeterminate: Boolean = true,
        notificationIcon: Int = R.drawable.icon_upload,
        notificationID: Int
    ): Notification {
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(context, channelId).setSmallIcon(notificationIcon)
            .setContentTitle(notificationTitle).setOngoing(isOngoing)
            .setProgress(0, 0, indeterminate)

        notificationManager.notify(notificationID, builder.build())
        return builder.build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun hideIndeterminateNotifications(activity: Context, notificationID: Int) {
        val notificationManager = activity.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(notificationID)
    }


    private fun uploadDrawing(
        data: NewDrawingV2VM.Companion.UploadData?,
        dataParams: NewDrawingV2VM.Companion.UploadDataParams?,
    ) {
        apiCounter++
        if (networkConnectivityObserver.isNetworkAvailable()) {
            GlobalScope.launch {
                data?.let {
                    when (val response = projectRepository.uploadDrawing(
                        projectId = data.projectId,
                        floorId = data.floorId,
                        groupId = data.groupId,
                        metadata = data.metadata,
                        files = data.files
                    )) {
                        is ApiResponse.Success -> {

                            dataParams?.let { dataParams ->
                                val newDrawingList = response.data.drawings

                                if (newDrawingList.isNotEmpty()) {
                                    val ceibroDownloadDrawingV2 =
                                        CeibroDownloadDrawingV2(
                                            fileName = response.data.drawings[0].fileName,
                                            downloading = false,
                                            isDownloaded = true,
                                            downloadId = 0L,
                                            drawing = response.data.drawings[0],
                                            drawingId = response.data.drawings[0]._id,
                                            groupId = response.data.drawings[0].groupId,
                                            localUri = dataParams.filePath
                                        )

                                    downloadedDrawingV2Dao.insertDownloadDrawing(
                                        ceibroDownloadDrawingV2
                                    )
                                }


                                val group = groupsV2Dao.getGroupByGroupId(dataParams.groupId)
                                if (group != null) {
                                    val allDrawings = group.drawings.toMutableList()
                                    allDrawings.addAll(newDrawingList)
                                    group.drawings = allDrawings
                                    group.updatedAt = response.data.groupUpdatedAt
                                    groupsV2Dao.insertGroup(group)
                                }
                                val newDrawingIdsList = newDrawingList.map { it._id }
                                val floor = floorsV2Dao.getFloorByFloorId(dataParams.floorId)
                                floor.updatedAt = response.data.floorUpdatedAt
                                val allDrawingsIDs = floor.drawings.toMutableList()
                                allDrawingsIDs.addAll(newDrawingIdsList)
                                floor.drawings = allDrawingsIDs
                                floorsV2Dao.insertFloor(floor)

                                println("Service Status...:On Destroyed called...drawing Uploaded")
                                EventBus.getDefault()
                                    .post(LocalEvents.UpdateGroupDrawings(projectID = dataParams.projectId))

                            }

                            apiCounter--
                            if (apiCounter <= 0) {
                                stopServiceAndClearNotification()
                            }
                        }

                        is ApiResponse.Error -> {
                            println("Service Status...:${response.error}")
                            apiCounter--
                            if (apiCounter <= 0) {
                                stopServiceAndClearNotification()
                            }
                        }
                    }
                }
            }
        } else {
            apiCounter--
            if (apiCounter <= 0) {
                stopServiceAndClearNotification()
            }
        }
    }

    private fun stopServiceAndClearNotification() {
        hideIndeterminateNotifications(
            this@UploadDrawingService,
            uploadingDrawingNotificationID
        )
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Service Status...:On Destroyed called...")
    }
}