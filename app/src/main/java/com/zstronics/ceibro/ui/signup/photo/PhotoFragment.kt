package com.zstronics.ceibro.ui.signup.photo

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.databinding.FragmentPhotoBinding
import com.zstronics.ceibro.ui.pixiImagePicker.NavControllerSample
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
        when (id) {
            R.id.chooseOrContinueBtn -> {
                if (viewState.isPhotoPicked.value == false) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        val intent = Intent(
                            requireContext(),
                            NavControllerSample::class.java
                        )
                        startActivity(intent)
                    } else {
                        pickPhoto()
                    }
                } else {
                    viewModel.updatePhoto(requireContext()) {
                        navigateToDashboard()
                    }
                }
            }
            R.id.changePhotoTV -> {
                pickPhoto()
            }
            R.id.skipBtn -> {
                navigateToDashboard()
            }
        }
    }

    private fun pickPhoto() {
        chooseImage { pickedImage ->
            viewModel.selectedUri = pickedImage
            viewState.isPhotoPicked.postValue(true)
            Glide.with(requireContext())
                .load(pickedImage)
                .centerCrop()
                .into(mViewDataBinding.userPhoto)
        }
    }

    private fun navigateToDashboard() {
        launchActivity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = true
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.homeFragment
            )
        }
    }
}