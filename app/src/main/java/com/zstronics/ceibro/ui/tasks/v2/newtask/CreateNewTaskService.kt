package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.gson.Gson
import com.zstronics.ceibro.BuildConfig
import com.zstronics.ceibro.BuildConfig.BASE_URL
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.data.base.BaseNetworkRepository
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.base.error.ApiError
import com.zstronics.ceibro.data.base.interceptor.CookiesInterceptor
import com.zstronics.ceibro.data.database.CeibroDatabase
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.dashboard.attachment.v2.AttachmentUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Response
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.data.sessions.SharedPreferenceManager
import com.zstronics.ceibro.di.contentType
import com.zstronics.ceibro.di.contentTypeValue
import com.zstronics.ceibro.di.timeoutConnect
import com.zstronics.ceibro.di.timeoutRead
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.newtask.CreateNewTaskService.NotificationUtils.CHANNEL_ID
import com.zstronics.ceibro.ui.tasks.v2.newtask.CreateNewTaskService.NotificationUtils.CHANNEL_NAME
import com.zstronics.ceibro.ui.tasks.v2.newtask.NewTaskV2VM.Companion.taskList
import com.zstronics.ceibro.ui.tasks.v2.newtask.NewTaskV2VM.Companion.taskRequest
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.PickedImages
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


class CreateNewTaskService : Service() {

    private var taskObjectData: NewTaskV2Entity? = null
    private var taskListData: ArrayList<PickedImages>? = null
    var sessionManager: SessionManager? = null
    private lateinit var context: Context
    private val indeterminateNotificationID = 1

    override fun onCreate() {
        super.onCreate()
        taskObjectData = taskRequest
        taskListData = taskList
        context = this

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Handler(Looper.getMainLooper()).postDelayed({
            sessionManager = getSessionManager(SharedPreferenceManager(this))


            startForeground(
                indeterminateNotificationID, createIndeterminateNotificationForFileUpload(
                    activity = this,
                    channelId = CHANNEL_ID,
                    channelName = CHANNEL_NAME,
                    notificationTitle = "Creating task with files"
                )
            )
            createTask(apiService(), sessionManager)
        }, 100)
        return START_STICKY
    }

    private fun createTask(apiService: ApiService, sessionManager: SessionManager?) {

        taskObjectData?.let { taskRequestData ->
            taskListData?.let { taskListData ->
                newTaskV2WithFiles(
                    taskRequestData, taskListData, apiService, this, sessionManager
                )
            }
        }

    }

    private fun newTaskV2WithFiles(
        newTask: NewTaskV2Entity,
        list: ArrayList<PickedImages>?,
        apiService: ApiService,
        context: Context,
        sessionManager: SessionManager?
    ) {

        val assignedToStateString = Gson().toJson(newTask.assignedToState)
        val assignedToStateString2 = Gson().toJson(assignedToStateString)

        val invitedNumbersString = Gson().toJson(newTask.invitedNumbers)
        val invitedNumbersString2 = Gson().toJson(invitedNumbersString)

        val dueDate = newTask.dueDate.toRequestBody("text/plain".toMediaTypeOrNull())
        val topic = newTask.topic.toRequestBody("text/plain".toMediaTypeOrNull())
        val project = newTask.project.toRequestBody("text/plain".toMediaTypeOrNull())
        val assignedToState = assignedToStateString2.toRequestBody("text/plain".toMediaTypeOrNull())
        val creator = newTask.creator.toRequestBody("text/plain".toMediaTypeOrNull())
        val description = newTask.description.toRequestBody("text/plain".toMediaTypeOrNull())
        val doneImageRequired =
            newTask.doneImageRequired.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val doneCommentsRequired =
            newTask.doneCommentsRequired.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val invitedNumbers = invitedNumbersString2.toRequestBody("text/plain".toMediaTypeOrNull())

        val parts = list?.map { file ->
            val reqFile =
                file.file.asRequestBody(("image/" + file.file.extension).toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files", file.file.name, reqFile)
        }
        val metaData = list?.map { file ->
            var tag = ""
            if (file.attachmentType == AttachmentTypes.Image) {
                tag = if (file.comment.isNotEmpty()) {
                    AttachmentTags.ImageWithComment.tagValue
                } else {
                    AttachmentTags.Image.tagValue
                }
            } else if (file.attachmentType == AttachmentTypes.Pdf || file.attachmentType == AttachmentTypes.Doc) {
                tag = AttachmentTags.File.tagValue
            }

            AttachmentUploadV2Request.AttachmentMetaData(
                fileName = file.fileName,
                orignalFileName = file.fileName,
                tag = tag,
                comment = file.comment.trim()
            )
        }
        val metadataString = Gson().toJson(metaData)
        val metadataString2 = Gson().toJson(metadataString)

        val metadataString2RequestBody =
            metadataString2.toRequestBody("text/plain".toMediaTypeOrNull())

        val call: Call<NewTaskV2Response> = apiService.newTaskV2WithFiles(
            hasFiles = true,
            dueDate = dueDate,
            topic = topic,
            project = project,
            assignedToState = assignedToState,
            creator = creator,
            description = description,
            doneImageRequired = doneImageRequired,
            doneCommentsRequired = doneCommentsRequired,
            invitedNumbers = invitedNumbers,
            files = parts,
            metadata = metadataString2RequestBody
        )

        call.enqueue(object : Callback<NewTaskV2Response> {
            override fun onResponse(
                call: Call<NewTaskV2Response>,
                response: Response<NewTaskV2Response>
            ) {
                if (response.isSuccessful) {
                    val room = providesAppDatabase(context)
                    val taskDao = room.getTaskV2sDao()
                    sessionManager?.let {
                        updateCreatedTaskInLocal(response.body()?.newTask, taskDao, sessionManager)
                    }

                    Toast.makeText(
                        this@CreateNewTaskService,
                        "Task Created Successfully",
                        Toast.LENGTH_LONG
                    )
                        .show()

                    hideIndeterminateNotificationForFileUpload(context)
                    stopSelf()

                } else {
                    val error = detectError(response)

                    Toast.makeText(
                        this@CreateNewTaskService,
                        error.message,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                hideIndeterminateNotificationForFileUpload(context)
                stopSelf()
            }

            override fun onFailure(call: Call<NewTaskV2Response>, t: Throwable) {
                Toast.makeText(
                    this@CreateNewTaskService,
                    t.message,
                    Toast.LENGTH_LONG
                )
                    .show()

                hideIndeterminateNotificationForFileUpload(context)
                stopSelf()
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun providesLoggingInterceptor(): HttpLoggingInterceptor {
        val logger = HttpLoggingInterceptor()
        logger.level =
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        return logger
    }

    private fun headerInterceptor(): Interceptor = Interceptor { chain ->
        val original = chain.request()
        val url: HttpUrl = original.url.newBuilder()
            .build()

        val request = original.newBuilder()
            .header(contentType, contentTypeValue)
            .method(original.method, original.body)
            .url(url)
            .build()

        chain.proceed(request)
    }

    private fun cookiesInterceptor(): CookiesInterceptor? =
        sessionManager?.let { CookiesInterceptor(it) }

    private fun okHttpClient(): OkHttpClient {

        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addInterceptor(providesLoggingInterceptor())
        okHttpBuilder.addInterceptor(headerInterceptor())
        cookiesInterceptor()?.let { okHttpBuilder.addInterceptor(it) }
        //        okHttpBuilder.addInterceptor(providesSessionValidator())
        okHttpBuilder.connectTimeout(timeoutConnect.toLong(), TimeUnit.SECONDS)
        okHttpBuilder.readTimeout(timeoutRead.toLong(), TimeUnit.SECONDS)
        return okHttpBuilder.build()
    }


    private fun getRetrofitBuilder(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun apiService() = getRetrofitBuilder().create(ApiService::class.java)

    interface ApiService {
        @Multipart
        @POST("v2/task/files")
        fun newTaskV2WithFiles(
            @Query("hasFiles") hasFiles: Boolean,
            @Part("dueDate") dueDate: RequestBody,
            @Part("topic") topic: RequestBody,
            @Part("project") project: RequestBody,
            @Part("assignedToState") assignedToState: RequestBody,
            @Part("creator") creator: RequestBody,
            @Part("description") description: RequestBody,
            @Part("doneImageRequired") doneImageRequired: RequestBody,
            @Part("doneCommentsRequired") doneCommentsRequired: RequestBody,
            @Part("invitedNumbers") invitedNumbers: RequestBody,
            @Part files: List<MultipartBody.Part>?,
            @Part("metadata") metadata: RequestBody
        ): Call<NewTaskV2Response>
    }


    fun updateCreatedTaskInLocal(
        task: CeibroTaskV2?, taskDao: TaskV2Dao, sessionManager: SessionManager
    ) {
        val sharedViewModel = NavHostPresenterActivity.activityInstance?.let {
            ViewModelProvider(it).get(SharedViewModel::class.java)
        }

        task?.let { newTask ->
            GlobalScope.launch {
                sessionManager.saveUpdatedAtTimeStamp(newTask.updatedAt)
                taskDao.insertTaskData(newTask)

                if (newTask.isCreator) {
                    when (newTask.fromMeState) {
                        TaskStatus.UNREAD.name.lowercase() -> {
                            val allFromMeUnreadTasks =
                                CookiesManager.fromMeUnreadTasks.value ?: mutableListOf()
                            val foundTask = allFromMeUnreadTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allFromMeUnreadTasks.indexOf(foundTask)
                                allFromMeUnreadTasks.removeAt(index)
                            }
                            allFromMeUnreadTasks.add(newTask)
                            val unreadTasks =
                                allFromMeUnreadTasks.sortedByDescending { it.updatedAt }
                                    .toMutableList()
                            CookiesManager.fromMeUnreadTasks.postValue(unreadTasks)
                        }

                        TaskStatus.ONGOING.name.lowercase() -> {
                            val allFromMeOngoingTasks =
                                CookiesManager.fromMeOngoingTasks.value ?: mutableListOf()
                            val foundTask = allFromMeOngoingTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allFromMeOngoingTasks.indexOf(foundTask)
                                allFromMeOngoingTasks.removeAt(index)
                            }
                            allFromMeOngoingTasks.add(newTask)
                            val ongoingTasks =
                                allFromMeOngoingTasks.sortedByDescending { it.updatedAt }
                                    .toMutableList()
                            CookiesManager.fromMeOngoingTasks.postValue(ongoingTasks)
                        }

                        TaskStatus.DONE.name.lowercase() -> {
                            val allFromMeDoneTasks =
                                CookiesManager.fromMeDoneTasks.value ?: mutableListOf()
                            val foundTask = allFromMeDoneTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allFromMeDoneTasks.indexOf(foundTask)
                                allFromMeDoneTasks.removeAt(index)
                            }
                            allFromMeDoneTasks.add(newTask)
                            val doneTasks = allFromMeDoneTasks.sortedByDescending { it.updatedAt }
                                .toMutableList()
                            CookiesManager.fromMeDoneTasks.postValue(doneTasks)
                        }
                    }
                }

                if (newTask.isAssignedToMe) {
                    when (newTask.toMeState) {
                        TaskStatus.NEW.name.lowercase() -> {
                            val allToMeNewTasks =
                                CookiesManager.toMeNewTasks.value ?: mutableListOf()
                            val foundTask = allToMeNewTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allToMeNewTasks.indexOf(foundTask)
                                allToMeNewTasks.removeAt(index)
                            }
                            allToMeNewTasks.add(newTask)
                            val newTasks =
                                allToMeNewTasks.sortedByDescending { it.updatedAt }.toMutableList()
                            CookiesManager.toMeNewTasks.postValue(newTasks)
                        }

                        TaskStatus.ONGOING.name.lowercase() -> {
                            val allToMeOngoingTasks =
                                CookiesManager.toMeOngoingTasks.value ?: mutableListOf()
                            val foundTask = allToMeOngoingTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allToMeOngoingTasks.indexOf(foundTask)
                                allToMeOngoingTasks.removeAt(index)
                            }
                            allToMeOngoingTasks.add(newTask)
                            val ongoingTasks =
                                allToMeOngoingTasks.sortedByDescending { it.updatedAt }
                                    .toMutableList()
                            CookiesManager.toMeOngoingTasks.postValue(ongoingTasks)
                        }

                        TaskStatus.DONE.name.lowercase() -> {
                            val allToMeDoneTasks =
                                CookiesManager.toMeDoneTasks.value ?: mutableListOf()
                            val foundTask = allToMeDoneTasks.find { it.id == newTask.id }
                            if (foundTask != null) {
                                val index = allToMeDoneTasks.indexOf(foundTask)
                                allToMeDoneTasks.removeAt(index)
                            }
                            allToMeDoneTasks.add(newTask)
                            val doneTasks =
                                allToMeDoneTasks.sortedByDescending { it.updatedAt }.toMutableList()
                            CookiesManager.toMeDoneTasks.postValue(doneTasks)
                        }
                    }
                    sharedViewModel?.isToMeUnread?.value = true
                    sessionManager.saveToMeUnread(true)
                }

                EventBus.getDefault().post(LocalEvents.RefreshTasksData())

            }
        }

    }

    private fun providesAppDatabase(context: Context): CeibroDatabase {
        return Room.databaseBuilder(context, CeibroDatabase::class.java, CeibroDatabase.DB_NAME)
            .addCallback(object : RoomDatabase.Callback() {
            })
            .fallbackToDestructiveMigration().build()
    }

    private fun getSessionManager(
        sharedPreferenceManager: SharedPreferenceManager
    ) = SessionManager(sharedPreferenceManager)

    fun hideIndeterminateNotificationForFileUpload(activity: Context) {
        val notificationManager = activity.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(indeterminateNotificationID) // Remove the notification with ID
    }


    object NotificationUtils {
        const val CHANNEL_ID = "file_upload_channel"
        const val CHANNEL_NAME = "Create Progress Channel"
    }

    private fun <T : BaseResponse> detectError(response: Response<T>): ApiError {
        val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
        return when (response.code()) {
            401 -> getApiError(
                mapError(
                    BaseNetworkRepository.NetworkErrors.Unauthorized,
                    response.code(),
                    jsonObj.getString("message")
                )
            )

            403 -> getApiError(
                mapError(
                    BaseNetworkRepository.NetworkErrors.Forbidden,
                    response.code(),
                    response.message()
                )
            )

            404 -> getApiError(
                mapError(
                    BaseNetworkRepository.NetworkErrors.NotFound,
                    response.code(),
                    response.message()
                )
            )

            502 -> getApiError(
                mapError(
                    BaseNetworkRepository.NetworkErrors.BadGateway,
                    response.code(),
                    "No response from server"
                )
            )

            504 -> getApiError(
                mapError(
                    BaseNetworkRepository.NetworkErrors.NoInternet,
                    response.code(),
                    response.message()
                )
            )

            in 400..500 -> getApiError(
                mapError(
                    BaseNetworkRepository.NetworkErrors.InternalServerError,
                    response.code(),
                    jsonObj.getString("message")
                )
            )

            -1009 -> getApiError(
                mapError(
                    BaseNetworkRepository.NetworkErrors.NoInternet,
                    response.code(),
                    response.message()
                )
            )

            -1001 -> getApiError(
                mapError(
                    BaseNetworkRepository.NetworkErrors.RequestTimedOut,
                    response.code(),
                    response.message()
                )
            )

            else -> {
                getApiError(
                    mapError(
                        BaseNetworkRepository.NetworkErrors.UnknownError(),
                        response.code(),
                        response.message()
                    )
                )
            }
        }
    }

    private fun getApiError(error: BaseNetworkRepository.ServerError): ApiError {
        return ApiError(
            error.code ?: getDefaultCode(),
            error.message ?: getDefaultMessage()
        )
    }

    private fun mapError(
        error: BaseNetworkRepository.NetworkErrors,
        code: Int = 0,
        message: String?
    ): BaseNetworkRepository.ServerError {
        return when (error) {

            is BaseNetworkRepository.NetworkErrors.NoInternet -> BaseNetworkRepository.ServerError(
                code,
                "It seems you're offline. Please try to reconnect and refresh to continue"
            )

            is BaseNetworkRepository.NetworkErrors.RequestTimedOut -> BaseNetworkRepository.ServerError(
                code,
                "It seems you're offline. Please try to reconnect and refresh to continue"
            )

            is BaseNetworkRepository.NetworkErrors.BadGateway -> BaseNetworkRepository.ServerError(
                code,
                "Bad Gateway"
            )

            is BaseNetworkRepository.NetworkErrors.NotFound -> BaseNetworkRepository.ServerError(
                code,
                "Not Found"
            )

            is BaseNetworkRepository.NetworkErrors.Forbidden -> BaseNetworkRepository.ServerError(
                code,
                "You don't have access to this information"
            )

            is BaseNetworkRepository.NetworkErrors.InternalServerError -> BaseNetworkRepository.ServerError(
                code,
                message
            )

            is BaseNetworkRepository.NetworkErrors.UnknownError -> BaseNetworkRepository.ServerError(
                code,
                getDefaultMessage()
            )

            is BaseNetworkRepository.NetworkErrors.Unauthorized -> BaseNetworkRepository.ServerError(
                code,
                message
            )
        }
    }

    private fun getDefaultMessage(): String {
        return "Something went wrong."
    }

    private fun getDefaultCode(): Int {
        return 0
    }

    private fun createIndeterminateNotificationForFileUpload(
        activity: CreateNewTaskService,
        channelId: String,
        channelName: String,
        notificationTitle: String,
        isOngoing: Boolean = true,
        indeterminate: Boolean = true,
        notificationIcon: Int = R.drawable.icon_upload
    ): Notification {
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = activity.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(activity, channelId).setSmallIcon(notificationIcon)
            .setContentTitle(notificationTitle).setOngoing(isOngoing)
            .setProgress(0, 0, indeterminate)

        notificationManager.notify(indeterminateNotificationID, builder.build())
        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Service is destroyed...")
    }
}