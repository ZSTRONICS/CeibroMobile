package com.zstronics.ceibro.ui.signup.photo

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
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
                    choosePhoto()
                } else {
                    viewModel.updatePhoto(requireContext()) {
                        if (isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
                            navigate(R.id.contactsSelectionFragment)
                        } else {
                            navigateToAppLoadingScreen()
                        }
                    }
                }
            }

            R.id.changePhotoTV -> {
                choosePhoto()
            }

            R.id.skipBtn -> {
                if (isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
                    navigate(R.id.contactsSelectionFragment)
                } else {
                    navigateToAppLoadingScreen()
                }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackButtonDispatcher()
    }

    private fun choosePhoto() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            val intent = Intent(
                requireContext(),
                NavControllerSample::class.java
            )
            startActivityForResult(intent, NavControllerSample.PHOTO_PICK_RESULT_CODE)
        } else {
            pickPhoto()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NavControllerSample.PHOTO_PICK_RESULT_CODE) {
            val pickedImage = data?.data
            if (pickedImage == null || pickedImage.equals("")) {
                //Do nothing
            } else {
                viewModel.selectedUri = pickedImage
                viewState.isPhotoPicked.postValue(true)
                Glide.with(requireContext())
                    .load(pickedImage)
                    .centerCrop()
                    .into(mViewDataBinding.userPhoto)
            }
        }
    }
}