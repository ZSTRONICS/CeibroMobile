package com.zstronics.ceibro.ui.contacts

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zstronics.ceibro.BuildConfig
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.base.interceptor.CookiesInterceptor
import com.zstronics.ceibro.data.base.interceptor.SessionValidator
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
import kotlinx.coroutines.coroutineScope
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

        val contacts =
            if (user?.autoContactSync == true)
                getLocalContacts(context)
            else
                sessionManager.getSyncedContacts()
        if (sessionManager.isLoggedIn() && contacts.isNotEmpty()) {
            val request = SyncContactsRequest(contacts = contacts)
            when (val response =
                dashboardRepository.syncContacts(sessionManager.getUserId(), request)) {
                is ApiResponse.Success -> {
                    EventBus.getDefault().post(LocalEvents.GetALlContactsFromAPI)
                    EventBus.getDefault().post(LocalEvents.ContactsSynced)
                    sessionManager.saveSyncedContacts(contacts)
                    Result.success()
                }

                is ApiResponse.Error -> {
                    Result.failure()
                }
            }
        } else {
            Result.failure()
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

    companion object {
        const val CONTACT_SYNC_WORKER_TAG: String = "ContactSyncWorker"
    }
}
