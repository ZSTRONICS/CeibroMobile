package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.google.gson.Gson
import com.zstronics.ceibro.BuildConfig
import com.zstronics.ceibro.BuildConfig.BASE_URL
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepositoryService
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.dashboard.attachment.v2.AttachmentUploadV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Response
import com.zstronics.ceibro.di.contentType
import com.zstronics.ceibro.di.contentTypeValue
import com.zstronics.ceibro.ui.tasks.v2.newtask.CreateNewTaskService.ServiceGenerator.apiService
import com.zstronics.ceibro.ui.tasks.v2.newtask.NewTaskV2VM.Companion.taskList
import com.zstronics.ceibro.ui.tasks.v2.newtask.NewTaskV2VM.Companion.taskRequest
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.PickedImages
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query


class CreateNewTaskService : Service() {
    private lateinit var context: Context
    override fun onCreate() {
        super.onCreate()
        context = this
        // val sessionManager2 = getSessionManager(SharedPreferenceManager(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createTask()
        return START_STICKY
    }

    private fun createTask() {

        taskRequest?.let { taskRequestData ->
            taskList?.let { taskListData ->
                newTaskV2WithFiles(
                    taskRequestData, taskListData
                )
            }
        }

    }

    private fun newTaskV2WithFiles(
        newTask: NewTaskV2Entity,
        list: ArrayList<PickedImages>?
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
                    Toast.makeText(
                        this@CreateNewTaskService,
                        "Task Created Successfully",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    // Handle successful response here
                    val data = response.body()

                    // Do something with the data
                } else {
                    // Handle error
                    // For example, log the error or retry the call
                }
            }

            override fun onFailure(call: Call<NewTaskV2Response>, t: Throwable) {
                // Handle failure, for example, log the error
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    object ServiceGenerator {


        private fun providesDashboardRepoService(): DashboardRepositoryService =
            getRetrofitBuilder().create(DashboardRepositoryService::class.java)

        private fun getRetrofitBuilder(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build()
        }

        private val retrofitBuilder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService: ApiService by lazy {
            retrofitBuilder.create(ApiService::class.java)
        }
    }

    public fun provideConverterFactoryy(): Converter.Factory {
        return GsonConverterFactory.create()
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

    // private fun cookiesInterceptor(): CookiesInterceptor = CookiesInterceptor(sessionManager2)
    /*    private fun okHttpClient(): OkHttpClient {
            val okHttpBuilder = OkHttpClient.Builder()
            okHttpBuilder.addInterceptor(providesLoggingInterceptor())
            okHttpBuilder.addInterceptor(headerInterceptor())
            okHttpBuilder.addInterceptor(cookiesInterceptor())
    //        okHttpBuilder.addInterceptor(providesSessionValidator())
            okHttpBuilder.connectTimeout(timeoutConnect.toLong(), TimeUnit.SECONDS)
            okHttpBuilder.readTimeout(timeoutRead.toLong(), TimeUnit.SECONDS)
            return okHttpBuilder.build()
        }*/


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
}
