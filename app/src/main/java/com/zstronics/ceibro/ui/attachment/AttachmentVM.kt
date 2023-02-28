package com.zstronics.ceibro.ui.attachment

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import com.zstronics.ceibro.data.local.FileAttachmentsDataSource
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AttachmentVM @Inject constructor(
    override val viewState: AttachmentState,
    private val fileAttachmentsDataSource: FileAttachmentsDataSource
) : HiltBaseViewModel<IAttachment.State>(), IAttachment.ViewModel {
    private val _attachments: MutableLiveData<List<FilesAttachments>> = MutableLiveData()
    val attachments = _attachments
    var allAttachments = listOf<FilesAttachments>()
    private val _selectedTab: MutableLiveData<String> = MutableLiveData("All")
    val selectedTab: LiveData<String> = _selectedTab
    override fun handleOnClick(id: Int) {
        super.handleOnClick(id)
        when (id) {
            R.id.attachmentDocsBtn -> {
                filterFiles(false)
                _selectedTab.postValue("doc")
            }

            R.id.attachmentMediaBtn -> {
                filterFiles(true)
                _selectedTab.postValue("media")
            }
            R.id.attachmentAll -> {
                showAll()
                _selectedTab.postValue("all")
            }
        }
    }

    override fun onFirsTimeUiCreate(bundle: Bundle?) {
        super.onFirsTimeUiCreate(bundle)
        val moduleId = bundle?.getString("moduleId")
        when (val moduleType = bundle?.getString("moduleType")) {
            AttachmentModules.SubTaskComments.name -> {
                if (bundle.getParcelable<SubTaskComments>("SubTaskComments") != null) {
                    allAttachments =
                        bundle.getParcelable<SubTaskComments>("SubTaskComments")?.files
                            ?: arrayListOf()
                    showAll()
                }
            }
            else -> {
                launch {
                    moduleType?.let {
                        moduleId?.let { it1 ->
                            allAttachments = fileAttachmentsDataSource.getAttachmentsById(
                                it,
                                it1
                            )
                            showAll()
                        }
                    }
                }
            }
        }
    }

    private fun filterFiles(showMedia: Boolean) {
        val filtered: List<FilesAttachments> = if (showMedia)
            allAttachments.filter { allMediaExtensions.contains(it.fileType) }
        else
            allAttachments.filter { allDocumentExtensions.contains(it.fileType) }
        _attachments.postValue(filtered.reversed())
    }

    private fun showAll() {
        _attachments.postValue(allAttachments.reversed())
    }
}