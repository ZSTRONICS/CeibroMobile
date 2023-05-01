package com.zstronics.ceibro.ui.signup.photo

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentPhotoBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoFragment :
    BaseNavViewModelFragment<FragmentPhotoBinding, IPhoto.State, PhotoVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: PhotoVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_photo
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }
}