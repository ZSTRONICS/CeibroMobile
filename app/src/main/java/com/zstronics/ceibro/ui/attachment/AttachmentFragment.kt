package com.zstronics.ceibro.ui.attachment

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentAttachmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AttachmentFragment :
    BaseNavViewModelFragment<FragmentAttachmentBinding, IAttachment.State, AttachmentVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: AttachmentVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_attachment
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
        }
    }
}