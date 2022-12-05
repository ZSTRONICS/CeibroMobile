package com.zstronics.ceibro.ui.chat.media

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentMediaBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaFragment :
    BaseNavViewModelFragment<FragmentMediaBinding, IMedia.State, MediaVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: MediaVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_media
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }
}