package com.zstronics.ceibro.ui.contacts.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactSyncWorkerFactory @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return ContactsSyncWorker(appContext, workerParameters, dashboardRepository)
    }
}
