package com.zstronics.ceibro.ui.profile.editprofile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.finish
import com.zstronics.ceibro.base.extensions.isEmail
import com.zstronics.ceibro.base.extensions.launchActivityWithFinishAffinity
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.databinding.FragmentEditProfileBinding
import com.zstronics.ceibro.ui.pixiImagePicker.NavControllerSample
import com.zstronics.ceibro.ui.profile.ImagePickerOrCaptureDialogSheet
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawings.DrawingsV2Fragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditProfileFragment :
    BaseNavViewModelFragment<FragmentEditProfileBinding, IEditProfile.State, EditProfileVM>() {

    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: EditProfileVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_edit_profile
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> finish()
            R.id.userEditProfileImg -> {
//                checkPermission(
//                    immutableListOf(
//                        Manifest.permission.CAMERA,
//                    )
//                ) {
//                    choosePhoto()
//                }
                if (isPermissionGranted(Manifest.permission.CAMERA)) {
                    choosePhoto()
                } else {
//                    navigateToAppSettings(requireContext())
                     requestCameraPermission()
//                    shortToastNow(getString(R.string.files_access_denied))
                }
            }

            R.id.saveProfileBtn -> {
                viewModel.updateProfile(
                    viewState.userFirstName.value.toString().trim(),
                    viewState.userSurname.value.toString().trim(),
                    viewState.userEmail.value.toString(),
                    viewState.userPhoneNumber.value.toString(),
                    viewState.userCompanyName.value.toString().trim(),
                    viewState.userJobTitle.value.toString().trim()
                ) {
                    finish()
                }
            }

            R.id.changePasswordBtn -> {
                showChangePasswordBottomSheet()
            }

            R.id.changePhoneNumberBtn -> {
                showChangePhoneNumberBottomSheet()
            }
        }
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


//        ImagePicker.with(requireActivity())
//            .cropFreeStyle()
//            .cropSquare()
//            .setMultipleAllowed(false)
//            .provider(ImageProvider.BOTH)
//            .galleryMimeTypes(
//                mimeTypes = arrayOf(
//                    "image/jpeg",
//                    "image/jpg",
//                    "image/png",
//                    "image/webp",
//                    "image/bmp"
//                )
//            )
//            .createIntentFromDialog { imagePickerOrCaptureLauncher.launch(it) }

    }

    private fun pickPhoto() {
        chooseImage { pickedImage ->
            viewModel.updateProfilePhoto(pickedImage.toString(), requireContext())
        }
    }

    private val imagePickerOrCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                println("imagePickerOrCaptureLauncher11: ${uri}")
                viewModel.updateProfilePhoto(uri.toString(), requireContext())

                // Use following, the uri to load the image. Only if you are not using crop feature:
//                uri.let { galleryUri ->
//                    context?.contentResolver?.takePersistableUriPermission(
//                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
//                    )
//                }
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.ccp.registerCarrierNumberEditText(mViewDataBinding.etPhone)

        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val parsedNumber = phoneNumberUtil.parse(viewState.userPhoneNumber.value.toString(), null)

        val countryCode = parsedNumber.countryCode
        val nationalSignificantNumber = parsedNumber.nationalNumber

        mViewDataBinding.ccp.setCountryForPhoneCode(countryCode)
        mViewDataBinding.etPhone.setText(nationalSignificantNumber.toString())

        mViewDataBinding.etNameField.addTextChangedListener(textWatcher)
        mViewDataBinding.etSurnameField.addTextChangedListener(textWatcher)
        mViewDataBinding.etEmailField.addTextChangedListener(textWatcher)
        mViewDataBinding.etCompanyField.addTextChangedListener(textWatcher)
        mViewDataBinding.etJobField.addTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // This method is called to notify you that the text has been changed and processed
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // This method is called to notify you that the text is about to change
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val firstName = viewState.userFirstName.value.toString().trim()
            val surname = viewState.userSurname.value.toString().trim()

            mViewDataBinding.saveProfileBtn.isEnabled =
                (viewState.userFirstName.value.toString() != viewModel.user?.firstName ||
                        viewState.userSurname.value.toString() != viewModel.user?.surName ||
                        viewState.userEmail.value.toString() != viewModel.user?.email ||
                        viewState.userPhoneNumber.value.toString() != viewModel.user?.phoneNumber ||
                        viewState.userCompanyName.value.toString() != viewModel.user?.companyName ||
                        viewState.userJobTitle.value.toString() != viewModel.user?.jobTitle) &&
                        (firstName.isNotEmpty() && isUserNameValid(firstName)) &&
                        (surname.isNotEmpty() && isUserNameValid(surname)) &&
                        viewState.userEmail.value.toString().isEmail()

            mViewDataBinding.etNameField.error =
                if (firstName.isEmpty()) {
                    resources.getString(R.string.error_message_first_name_empty)
                } else if (!startsWithAlphabet(firstName)) {
                    resources.getString(R.string.error_message_first_name_alphabet_required)
                } else if (!isUserNameValid(firstName)) {
                    resources.getString(R.string.error_message_special_character_not_allowed_in_name)
                } else {
                    null
                }
            mViewDataBinding.etSurnameField.error =
                if (surname.isEmpty()) {
                    resources.getString(R.string.error_message_surname_name_empty)
                } else if (!startsWithAlphabet(surname)) {
                    resources.getString(R.string.error_message_surname_alphabet_required)
                } else if (!isUserNameValid(surname)) {
                    resources.getString(R.string.error_message_special_character_not_allowed_in_name)
                } else {
                    null
                }
            mViewDataBinding.etEmailField.error =
                if (!viewState.userEmail.value.toString().isEmail()) {
                    resources.getString(R.string.error_message_email_validation)
                } else {
                    null
                }
        }
    }


    private fun showChangePasswordBottomSheet() {
        val sheet = ChangePasswordSheet()
        sheet.dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );
        sheet.onChangePassword = { oldPassword, newPassword ->
            viewModel.changePassword(oldPassword, newPassword) {
                logoutUser()
            }
        }
        sheet.onChangePasswordDismiss = {

        }
        sheet.isCancelable = false
        sheet.show(childFragmentManager, "ChangePasswordSheet")
    }

    private fun showChangePhoneNumberBottomSheet() {
        val sheet = ChangePhoneNumberSheet()

        sheet.onChangeNumber = { newNumber, phoneCode, password ->
            viewModel.changePhoneNumber(newNumber, phoneCode, password) {
                sheet.dismiss()
                showChangePhoneNumberVerificationBottomSheet(newNumber)
            }
        }
        sheet.onChangeNumberDismiss = {
        }
        sheet.isCancelable = false
        sheet.show(childFragmentManager, "ChangePhoneNumberSheet")
    }

    private fun showChangePhoneNumberVerificationBottomSheet(newNumber: String) {
        val sheet = ChangePhoneNumberVerifyOtpSheet()

        sheet.onVerificationDone = { otp ->
            viewModel.changePhoneNumberVerifyOtp(newNumber, otp) {
                logoutUser()
            }
        }
        sheet.onVerificationResendCode = {
            viewModel.resendOtp(newNumber) { }
        }
        sheet.onVerificationDismiss = {
            //TODO
        }
        sheet.isCancelable = false
        sheet.show(childFragmentManager, "ChangePhoneNumberVerifyOtpSheet")
    }


    private fun logoutUser() {
        viewModel.endUserSession(requireContext())
        launchActivityWithFinishAffinity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = true
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.onboarding_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.loginFragment
            )
        }
        Thread { activity?.let { Glide.get(it).clearDiskCache() } }.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NavControllerSample.PHOTO_PICK_RESULT_CODE) {
            val pickedImage = data?.data
            if (pickedImage != null && !pickedImage.equals("")) {
                viewModel.updateProfilePhoto(pickedImage.toString(), requireContext())
            }

        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                choosePhoto()
            } else {
                handleDeniedPermissions(permissions,grantResults)
            }
        }
    }

    private fun handleDeniedPermissions(permissions: Array<out String>, grantResults: IntArray) {
        for (i in permissions.indices) {
            val permission = permissions[i]
            val result = grantResults[i]

            if (result == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    showToast("Permission denied: $permission")
                } else {
                    showToast("Permission denied: $permission. Please enable it in the app settings.")
                    navigateToAppSettings(context)
                    return
                }
            }
        }
    }

    private fun navigateToAppSettings(context: Context?) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", context?.packageName, null)
        intent.data = uri
        startActivity(intent)
    }



    private fun requestCameraPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }


}