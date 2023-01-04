package com.zstronics.ceibro.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zstronics.ceibro.data.base.RetroNetwork
import com.zstronics.ceibro.data.database.CeibroDatabase
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.data.sessions.SharedPreferenceManager
import com.zstronics.ceibro.data.repos.auth.AuthRepositoryService
import com.zstronics.ceibro.data.repos.chat.ChatRepositoryService
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepositoryService
import com.zstronics.ceibro.data.repos.projects.ProjectRepositoryService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun providesAuthRepoService(): AuthRepositoryService =
        RetroNetwork().createService(AuthRepositoryService::class.java)

    @Provides
    fun providesChatRepoService(): ChatRepositoryService =
        RetroNetwork().createService(ChatRepositoryService::class.java)

    @Provides
    fun providesProjectRepoService(): ProjectRepositoryService =
        RetroNetwork().createService(ProjectRepositoryService::class.java)

    @Provides
    fun providesDashboardRepoService(): DashboardRepositoryService =
        RetroNetwork().createService(DashboardRepositoryService::class.java)

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
    fun provideProjectTaskDao(database: CeibroDatabase) = database.getTasksDao()

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
}