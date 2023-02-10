package com.zstronics.ceibro.ui.attachment.addattachments

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentAddAttachmentBinding
import com.zstronics.ceibro.databinding.FragmentAttachmentBinding
import dagger.hilt.android.AndroidEntryPoint

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
            R.id.backBtn -> navigateBack()
        }
    }
}