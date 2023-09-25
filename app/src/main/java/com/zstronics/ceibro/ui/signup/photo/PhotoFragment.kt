package com.zstronics.ceibro.ui.signup.photo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentPhotoBinding
import com.zstronics.ceibro.ui.pixiImagePicker.NavControllerSample
import com.zstronics.ceibro.ui.profile.ImagePickerOrCaptureDialogSheet
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
        /*
        //This method is used to open whatsapp style image picker with camera but not applicable on android 13
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            val intent = Intent(
                requireContext(),
                NavControllerSample::class.java
            )
            startActivityForResult(intent, NavControllerSample.PHOTO_PICK_RESULT_CODE)
        } else {
            pickPhoto()
        }*/

        val sheet = ImagePickerOrCaptureDialogSheet()
        sheet.onCameraBtnClick = {
            imagePickerOrCaptureLauncher.launch(
                ImagePicker.with(requireActivity())
                    .cropFreeStyle()
                    .cropSquare()
                    .setMultipleAllowed(false)
                    .provider(ImageProvider.CAMERA)
                    .createIntent()
            )
        }
        sheet.onGalleryBtnClick = {
            imagePickerOrCaptureLauncher.launch(
                ImagePicker.with(requireActivity())
                    .cropFreeStyle()
                    .cropSquare()
                    .setMultipleAllowed(false)
                    .provider(ImageProvider.GALLERY)
                    .galleryMimeTypes(
                        mimeTypes = arrayOf(
                            "image/jpeg",
                            "image/jpg",
                            "image/png",
                            "image/webp",
                            "image/bmp"
                        )
                    )
                    .createIntent()
            )
        }
        sheet.isCancelable = false
        sheet.show(childFragmentManager, "ImagePickerOrCaptureDialogSheet")
    }

    private val imagePickerOrCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.data?.let { uri ->
                    viewModel.selectedUri = uri
                    viewState.isPhotoPicked.postValue(true)
                    Glide.with(requireContext())
                        .load(uri)
                        .centerCrop()
                        .into(mViewDataBinding.userPhoto)
                }
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