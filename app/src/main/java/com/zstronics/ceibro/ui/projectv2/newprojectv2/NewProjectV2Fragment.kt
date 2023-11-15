package com.zstronics.ceibro.ui.projectv2.newprojectv2

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentNewProjectV2Binding
import com.zstronics.ceibro.ui.profile.ImagePickerOrCaptureDialogSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewProjectV2Fragment :
    BaseNavViewModelFragment<FragmentNewProjectV2Binding, INewProjectV2.State, NewProjectV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: NewProjectV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_new_project_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.cl_newPhoto -> {
//                showAddPhotoBottomSheet()
                if (isPermissionGranted(Manifest.permission.CAMERA)) {
                    choosePhoto()
                } else {
                    shortToastNow(getString(R.string.files_access_denied))
                }
            }

            R.id.closeBtn -> {
                navigateBack()
            }

            R.id.cancelBtn -> {
                navigateBack()
            }

            R.id.saveBtn -> {
                viewState.projectName.value?.let {
                    if (it.isEmpty()) {
                        showToast(getString(R.string.project_name_is_required))
                    } else {
                        viewModel.addNewProject(requireContext()) { isSuccess ->
                            if (isSuccess) {
                                navigateBack()
                            }
                        }
                    }
                }
            }

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun showAddPhotoBottomSheet() {
        val coroutineScope = viewLifecycleOwner.lifecycleScope
        coroutineScope.launch(Dispatchers.Main) {

            val sheet = AddNewPhotoBottomSheet {

                showToast(it)
            }
            sheet.isCancelable = true
            sheet.setStyle(
                BottomSheetDialogFragment.STYLE_NORMAL,
                R.style.CustomBottomSheetDialogTheme
            )
            sheet.show(childFragmentManager, "AddPhotoBottomSheet")
        }
    }


    private fun choosePhoto() {
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
                val uri = it.data?.data!!
                println("imagePickerOrCaptureLauncher11: ${uri}")
//                viewModel.updateProfilePhoto(uri.toString(), requireContext())
                viewState.projectPhoto.postValue(uri.toString())
                Glide.with(this)
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // Optional: Caching strategy
                    .into(mViewDataBinding.projectImg)

                mViewDataBinding.newPhotoText.visibility = View.GONE
                mViewDataBinding.projectImg.visibility = View.VISIBLE
            }
        }


}