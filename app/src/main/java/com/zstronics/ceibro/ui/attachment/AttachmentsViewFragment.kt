package com.zstronics.ceibro.ui.attachment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.databinding.FragmentAttachmentBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AttachmentsViewFragment :
    BaseNavViewModelFragment<FragmentAttachmentBinding, IAttachment.State, AttachmentVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: AttachmentVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_attachment
    override fun toolBarVisibility(): Boolean = false

    @Inject
    lateinit var attachmentAdapter: AttachmentViewAdapter

    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
            R.id.attachmentAddBtn -> navigate(R.id.addAttachmentFragment, arguments)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.attachmentRecyclerView.adapter = attachmentAdapter

        viewModel.attachments.observe(viewLifecycleOwner) { list ->
            attachmentAdapter.setList(list)
        }

        viewModel.showMedia.observe(viewLifecycleOwner) { showMedia ->
            if (showMedia) {
                mViewDataBinding.attachmentDocsBtn.setTextColor(requireContext().getColor(R.color.black))
                mViewDataBinding.attachmentMediaBtn.setTextColor(requireContext().getColor(R.color.appYellow))
            } else {
                mViewDataBinding.attachmentDocsBtn.setTextColor(requireContext().getColor(R.color.appYellow))
                mViewDataBinding.attachmentMediaBtn.setTextColor(requireContext().getColor(R.color.black))
            }
        }
        attachmentAdapter.itemClickListener =
            { _: View, position: Int, data: FilesAttachments? ->
                viewModel.removeFile(position)
            }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onFirsTimeUiCreate(arguments)
    }
}