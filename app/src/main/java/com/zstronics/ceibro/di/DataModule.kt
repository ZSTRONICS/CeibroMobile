package com.zstronics.ceibro.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zstronics.ceibro.data.database.CeibroDatabase
import com.zstronics.ceibro.data.repos.auth.AuthRepository
import com.zstronics.ceibro.data.repos.auth.IAuthRepository
import com.zstronics.ceibro.data.repos.chat.ChatRepository
import com.zstronics.ceibro.data.repos.chat.IChatRepository
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.projects.IProjectRepository
import com.zstronics.ceibro.data.repos.projects.ProjectRepository
import com.zstronics.ceibro.data.repos.task.ITaskRepository
import com.zstronics.ceibro.data.repos.task.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun provideAuthRepository(authRepository: AuthRepository): IAuthRepository

    @Binds
    @Singleton
    abstract fun provideChatRepository(chatRepository: ChatRepository): IChatRepository

    @Binds
    @Singleton
    abstract fun provideProjectRepository(projectRepository: ProjectRepository): IProjectRepository

    @Binds
    @Singleton
    abstract fun provideDashboardRepository(dashboardRepository: DashboardRepository): IDashboardRepository
    @Binds
    @Singleton
    abstract fun provideTaskRepository(taskRepository: TaskRepository): ITaskRepository
}