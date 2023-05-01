package com.zstronics.ceibro.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zstronics.ceibro.BuildConfig
import com.zstronics.ceibro.BuildConfig.BASE_URL
import com.zstronics.ceibro.data.base.interceptor.CookiesInterceptor
import com.zstronics.ceibro.data.base.interceptor.SessionValidator
import com.zstronics.ceibro.data.database.CeibroDatabase
import com.zstronics.ceibro.data.remote.TaskRetroService
import com.zstronics.ceibro.data.repos.auth.AuthRepositoryService
import com.zstronics.ceibro.data.repos.chat.ChatRepositoryService
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepositoryService
import com.zstronics.ceibro.data.repos.projects.ProjectRepositoryService
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.data.sessions.SharedPreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun providesAuthRepoService(retrofit: Retrofit): AuthRepositoryService =
        retrofit.create(AuthRepositoryService::class.java)

    @Provides
    fun providesChatRepoService(retrofit: Retrofit): ChatRepositoryService =
        retrofit.create(ChatRepositoryService::class.java)

    @Provides
    fun providesProjectRepoService(retrofit: Retrofit): ProjectRepositoryService =
        retrofit.create(ProjectRepositoryService::class.java)

    @Provides
    fun providesDashboardRepoService(retrofit: Retrofit): DashboardRepositoryService =
        retrofit.create(DashboardRepositoryService::class.java)

    @Singleton
    @Provides
    fun providesSessionManager(
        sharedPreferenceManager: SharedPreferenceManager
    ) = SessionManager(sharedPreferenceManager)

    @Singleton
    @Provides
    fun provideSharedPreferenceManager(@ApplicationContext context: Context) =
        SharedPreferenceManager(context)

    @Provides
    fun provideTaskDao(database: CeibroDatabase) = database.getTasksDao()

    @Provides
    fun provideSubTaskDao(database: CeibroDatabase) = database.getSubTaskDao()

    @Provides
    fun provideFileAttachmentsDao(database: CeibroDatabase) = database.getFileAttachmentsDao()

    @Provides
    fun providesTaskRepoService(retrofit: Retrofit): TaskRetroService =
        retrofit.create(TaskRetroService::class.java)

    @Provides
    @Singleton
    internal fun providesAppDatabase(@ApplicationContext context: Context): CeibroDatabase {
        return Room.databaseBuilder(context, CeibroDatabase::class.java, CeibroDatabase.DB_NAME)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
//                    debug("DataBasePath>>" + db.path)
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
//                    debug("DataBasePath>>" + db.path)
                }
            })
            .fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun providesSessionValidator(
    ): SessionValidator {
        val validator = object : SessionValidator() {
            override fun invalidate() {
            }
        }
        return validator
    }

    @Provides
    fun provideRetrofitClient(
        okHttpClient: OkHttpClient,
        converterFactory: Converter.Factory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()
    }

    @Provides
    fun provideConverterFactory(): Converter.Factory {
        return GsonConverterFactory.create()
    }

    @Provides
    fun providesLoggingInterceptor(): HttpLoggingInterceptor {
        val logger = HttpLoggingInterceptor()
        logger.level =
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        return logger
    }

    @Provides
    fun cookiesInterceptor(): CookiesInterceptor = CookiesInterceptor()

    @Provides
    fun headerInterceptor(): Interceptor = Interceptor { chain ->
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

    @Provides
    fun provideOkHttpClient(
        @ApplicationContext appContext: Context,
        loggingInterceptor: HttpLoggingInterceptor,
        cookiesInterceptor: CookiesInterceptor,
        headerInterceptor: Interceptor,
        sessionValidator: SessionValidator,
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addInterceptor(headerInterceptor)
        okHttpBuilder.addInterceptor(loggingInterceptor)
        okHttpBuilder.addInterceptor(cookiesInterceptor)
        okHttpBuilder.addInterceptor(sessionValidator)
        okHttpBuilder.connectTimeout(timeoutConnect.toLong(), TimeUnit.SECONDS)
        okHttpBuilder.readTimeout(timeoutRead.toLong(), TimeUnit.SECONDS)
        return okHttpBuilder.build()
    }

    @Singleton
    @Provides
    fun provideWorkerDependency(service: DashboardRepositoryService): DashboardRepository {
        return DashboardRepository(service)
    }
}

private const val timeoutRead = 30   //In seconds
private const val contentType = "Content-Type"
private const val contentTypeValue = "application/json"
private const val timeoutConnect = 30   //In seconds