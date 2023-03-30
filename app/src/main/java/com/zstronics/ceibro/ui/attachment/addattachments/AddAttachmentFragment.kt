package com.zstronics.ceibro.ui.attachment.addattachments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentAddAttachmentBinding
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddAttachmentFragment :
    BaseNavViewModelFragment<FragmentAddAttachmentBinding, IAddAttachment.State, AddAttachmentVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: AddAttachmentVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_add_attachment
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn, R.id.addAttachmentCancelBtn, 1 -> navigateBack()
            R.id.addMoreFilesBtn -> pickAttachment(true)
            R.id.addAttachmentUploadBtn -> {
                val moduleName = arguments?.getString("moduleType")
                val moduleId = arguments?.getString("moduleId")
                moduleName?.let {
                    moduleId?.let { it1 ->
                        createNotification(
                            moduleId,
                            moduleName,
                            "Uploading files for $moduleName",
                            isOngoing = true,
                            indeterminate = true
                        )
                        viewModel.uploadFiles(
                            it,
                            it1,
                            requireContext()
                        )
                    }

                    navigateBack()
                }
            }
        }
    }

    @Inject
    lateinit var addAttachmentsAdapter: AddAttachmentsAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.addAttachmentRV.adapter = addAttachmentsAdapter
        viewModel.fileUriList.observe(viewLifecycleOwner) { list ->
            if (list != null) {
                addAttachmentsAdapter.setList(list)
            }
        }
        addAttachmentsAdapter.itemClickListener =
            { _: View, position: Int, data: SubtaskAttachment? ->
                viewModel.removeFile(position)
            }

        addAttachmentsAdapter.onEditPhoto =
            { _: View, position: Int, data: SubtaskAttachment? ->
                data?.attachmentUri?.let { uri ->
                    startEditor(uri) { updatedUri ->
                        if (updatedUri != null) {
                            viewModel.updateUri(position, updatedUri)
                        }
                    }
                }
            }
    }
}