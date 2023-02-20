//package com.zstronics.fileuploader
//
//import android.content.Context
//import android.net.Uri
//import androidx.hilt.work.HiltWorker
//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//import com.zstronics.ceibro.data.base.ApiResponse
//import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
//import com.zstronics.ceibro.data.repos.dashboard.IDashboardRepository
//import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
//import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentUploadRequest
//import com.zstronics.ceibro.utils.FileUtils
//import dagger.assisted.Assisted
//import dagger.assisted.AssistedInject
//import kotlinx.coroutines.coroutineScope
//
//@HiltWorker
//class FileUploadWorker @AssistedInject constructor(
//    @Assisted val context: Context,
//    @Assisted workerParams: WorkerParameters,
//    private val dashboardRepository: IDashboardRepository,
//    private val fileAttachmentsDataSource: FileAttachmentsDataSource
//) :
////    CoroutineWorker(context, workerParams) {
////
////    override suspend fun doWork(): Result = coroutineScope {
////        val moduleId = inputData.getString("module") ?: ""
////        val id = inputData.getString("id") ?: ""
////        val moduleName = when (moduleId) {
////            "Task" -> AttachmentModules.Task
////            "SubTask" -> AttachmentModules.SubTask
////            "SubTaskComments" -> AttachmentModules.SubTaskComments
////            "Project" -> AttachmentModules.Project
////            else -> AttachmentModules.Task
////        }
////
////        // Get the file URIs
////        val fileUris = inputData.getStringArray("fileUris") ?: emptyArray()
////
////        // Convert the file URIs to file objects
////        val attachmentUriList = fileUris.map {
////            FileUtils.getFile(applicationContext, Uri.parse(it))
////        }
////
////        // Create the API request
////        val request = AttachmentUploadRequest(
////            _id = id,
////            moduleName = moduleName,
////            files = attachmentUriList
////        )
////
////        // Handle the API response
////        when (val response = dashboardRepository.uploadFiles(request)) {
////            is ApiResponse.Success -> {
////                val allFiles = response.data.results.files
////                val updatedFiles = allFiles.mapIndexed { index, file ->
////                    if (fileUris.size > index) {
////                        file.copy(fileUrl = fileUris[index])
////                    } else {
////                        file // return the original file if no URI is available at the corresponding index
////                    }
////                }
////                fileAttachmentsDataSource.insertAll(updatedFiles)
////                Result.success()
////            }
////            is ApiResponse.Error -> {
////                Result.failure()
////            }
////        }
////        Result.success()
////    }
//}
