package com.zstronics.ceibro.ui.contacts

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.hilt.work.HiltWorker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zstronics.ceibro.BuildConfig
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.interceptor.CookiesInterceptor
import com.zstronics.ceibro.data.base.interceptor.SessionValidator
import com.zstronics.ceibro.data.database.CeibroDatabase
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepositoryService
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.data.sessions.SharedPreferenceManager
import com.zstronics.ceibro.di.contentType
import com.zstronics.ceibro.di.contentTypeValue
import com.zstronics.ceibro.di.timeoutConnect
import com.zstronics.ceibro.di.timeoutRead
import com.zstronics.ceibro.extensions.getLocalContacts
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@HiltWorker
class ContactSyncWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters
) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        val dashboardRepository = DashboardRepository(providesDashboardRepoService())

        println("PhoneNumber-SyncWorkerRunning")
        val sessionManager = getSessionManager(SharedPreferenceManager(context))
        val user = sessionManager.getUser().value

        val room = providesAppDatabase(context)
        val roomContacts = room.getConnectionsV2Dao().getAll()

        val phoneContacts = getLocalContacts(context)

        val contacts: MutableList<SyncContactsRequest.CeibroContactLight> =
            if (user?.autoContactSync == true) {
                phoneContacts
            } else {
                roomContacts.toLightContacts().toMutableList()
            }

        val deletedContacts = findDeletedContacts(roomContacts, phoneContacts).toLightContacts()
        contacts.removeAll(deletedContacts)

        val updatedContacts =
            compareContactsAndUpdateList(roomContacts, phoneContacts)

        val updatedAndNewContacts = mutableListOf<SyncContactsRequest.CeibroContactLight>()
        updatedAndNewContacts.addAll(updatedContacts)

        if (user?.autoContactSync == true) {
            val newContacts = findNewContacts(roomContacts, phoneContacts)
            updatedAndNewContacts.addAll(newContacts)
        }

        // Delete contacts API call
        if (deletedContacts.isNotEmpty()) {
            val request = SyncContactsRequest(contacts = deletedContacts)
            when (val response = dashboardRepository.syncDeletedContacts(request)) {
                is ApiResponse.Success -> {
                    EventBus.getDefault().post(LocalEvents.GetALlContactsFromAPI)
                    EventBus.getDefault().post(LocalEvents.ContactsSynced)
                    EventBus.getDefault().post(LocalEvents.UpdateConnections)
                    updateLocalContacts(dashboardRepository, room, user?.id ?: "", sessionManager)
                    Result.success()
                }

                is ApiResponse.Error -> {
                    Result.failure()
                }
            }
        }
        EventBus.getDefault().post(LocalEvents.UpdateConnections)

        /// No Change in contacts
        if (sessionManager.isLoggedIn() && updatedAndNewContacts.isNotEmpty() && user?.autoContactSync == true) {
            val request = SyncContactsRequest(contacts = updatedAndNewContacts)
            when (val response =
                dashboardRepository.syncContacts(sessionManager.getUserId(), request)) {
                is ApiResponse.Success -> {
                    EventBus.getDefault().post(LocalEvents.GetALlContactsFromAPI)
                    EventBus.getDefault().post(LocalEvents.ContactsSynced)
                    updateLocalContacts(dashboardRepository, room, user?.id ?: "", sessionManager)
                    Result.success()
                }

                is ApiResponse.Error -> {
                    Result.failure()
                }
            }
        } else {
            Result.success()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun updateLocalContacts(
        dashboardRepository: DashboardRepository,
        room: CeibroDatabase,
        userId: String,
        sessionManager: SessionManager
    ) {
        when (val response = dashboardRepository.getAllConnectionsV2(userId)) {
            is ApiResponse.Success -> {
                room.getConnectionsV2Dao().insertAll(response.data.contacts)
                EventBus.getDefault().post(LocalEvents.UpdateConnections)
                GlobalScope.launch(Dispatchers.IO) {
                    Looper.prepare()
                    val handler = Handler()
                    handler.postDelayed({
                        EventBus.getDefault().post(LocalEvents.ContactsSynced)
                    }, 50)
                    Looper.loop()
                }
            }
            is ApiResponse.Error -> {
            }
        }
    }

    private fun providesDashboardRepoService(): DashboardRepositoryService =
        getRetrofitBuilder().create(DashboardRepositoryService::class.java)

    private fun getRetrofitBuilder(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient())
            .addConverterFactory(provideConverterFactory())
            .build()
    }

    private fun okHttpClient(): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addInterceptor(providesLoggingInterceptor())
        okHttpBuilder.addInterceptor(headerInterceptor())
        okHttpBuilder.addInterceptor(cookiesInterceptor())
//        okHttpBuilder.addInterceptor(providesSessionValidator())
        okHttpBuilder.connectTimeout(timeoutConnect.toLong(), TimeUnit.SECONDS)
        okHttpBuilder.readTimeout(timeoutRead.toLong(), TimeUnit.SECONDS)
        return okHttpBuilder.build()
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

    private fun cookiesInterceptor(): CookiesInterceptor = CookiesInterceptor()
    private fun providesSessionValidator(
    ): SessionValidator {
        val validator = object : SessionValidator() {
            override fun invalidate() {
            }
        }
        return validator
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

    private fun provideConverterFactory(): Converter.Factory {
        return GsonConverterFactory.create()
    }

    private fun getSessionManager(
        sharedPreferenceManager: SharedPreferenceManager
    ) = SessionManager(sharedPreferenceManager)

    private fun provideConnectionsV2Dao(database: CeibroDatabase) = database.getConnectionsV2Dao()
    private fun providesAppDatabase(context: Context): CeibroDatabase {
        return Room.databaseBuilder(context, CeibroDatabase::class.java, CeibroDatabase.DB_NAME)
            .addCallback(object : RoomDatabase.Callback() {

            })
            .fallbackToDestructiveMigration().build()
    }

    companion object {
        const val CONTACT_SYNC_WORKER_TAG: String = "ContactSyncWorker"
    }
}
