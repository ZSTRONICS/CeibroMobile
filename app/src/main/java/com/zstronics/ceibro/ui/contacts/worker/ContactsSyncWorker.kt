package com.zstronics.ceibro.ui.contacts.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zstronics.ceibro.data.base.ApiResponse
import com.zstronics.ceibro.data.repos.dashboard.DashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.extensions.getLocalContacts
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope

@HiltWorker
class ContactsSyncWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dashboardRepository: DashboardRepository
) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        Log.d("ContactsSyncWorker", "Worker Started working")

        val contacts = getLocalContacts(context)
        val request = SyncContactsRequest(contacts = contacts)
        // Handle the API response
        when (val response =
            dashboardRepository.syncContacts("644e544de85afc8c725b6ad0", request)) {
            is ApiResponse.Success -> {
                Log.d("ContactsSyncWorker", "Worker Completed")
                Result.success()
            }
            is ApiResponse.Error -> {
                Log.d("ContactsSyncWorker", "Worker Failed")
                Result.failure()
            }
        }
    }

    companion object {
        val WORK_TAG: String = ContactsSyncWorker::class.java.name
        val WORK_NAME: String = ContactsSyncWorker::class.java.name
    }
}
