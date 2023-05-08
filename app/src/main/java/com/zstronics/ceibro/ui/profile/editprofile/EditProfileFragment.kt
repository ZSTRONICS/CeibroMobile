package com.zstronics.ceibro.ui.profile.editprofile

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
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
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.immutableListOf

@AndroidEntryPoint
class EditProfileFragment :
    BaseNavViewModelFragment<FragmentEditProfileBinding, IEditProfile.State, EditProfileVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: EditProfileVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_edit_profile
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> finish()
            R.id.userEditProfileImg -> checkPermission(
                immutableListOf(
                    Manifest.permission.CAMERA,
                )
            ) {
                choosePhoto()
            }
            R.id.saveProfileBtn -> {
                viewModel.updateProfile(
                    viewState.userFirstName.value.toString(),
                    viewState.userSurname.value.toString(),
                    viewState.userEmail.value.toString(),
                    viewState.userPhoneNumber.value.toString(),
                    viewState.userCompanyName.value.toString(),
                    viewState.userJobTitle.value.toString()
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

    private fun pickPhoto() {
        chooseImage { pickedImage ->
            viewModel.updateProfilePhoto(pickedImage.toString(), requireContext())
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
            mViewDataBinding.saveProfileBtn.isEnabled =
                (viewState.userFirstName.value.toString() != viewModel.user?.firstName ||
                        viewState.userSurname.value.toString() != viewModel.user?.surName ||
                        viewState.userEmail.value.toString() != viewModel.user?.email ||
                        viewState.userPhoneNumber.value.toString() != viewModel.user?.phoneNumber ||
                        viewState.userCompanyName.value.toString() != viewModel.user?.companyName ||
                        viewState.userJobTitle.value.toString() != viewModel.user?.jobTitle) &&
                        viewState.userFirstName.value.toString().length >= 2 &&
                        viewState.userSurname.value.toString().length >= 2 &&
                        viewState.userEmail.value.toString().isEmail()

            mViewDataBinding.etNameField.error =
                if (viewState.userFirstName.value.toString().length < 2) {
                    resources.getString(R.string.error_message_name_validation)
                } else {
                    null
                }
            mViewDataBinding.etSurnameField.error =
                if (viewState.userSurname.value.toString().length < 2) {
                    resources.getString(R.string.error_message_name_validation)
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
        viewModel.endUserSession()
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
            if (pickedImage == null || pickedImage.equals("")) {
                //Do nothing
            } else {
                viewModel.updateProfilePhoto(pickedImage.toString(), requireContext())
            }

        }
    }
}