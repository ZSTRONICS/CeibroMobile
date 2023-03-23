package com.zstronics.ceibro.ui.profile.editprofile

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.fragment.app.viewModels
import com.ceibro.permissionx.PermissionX
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.finish
import com.zstronics.ceibro.base.extensions.toast
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentEditProfileBinding
import com.zstronics.ceibro.extensions.openFilePicker
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
    private var isPassShown1 = false
    private var isPassShown2 = false
    override fun onClick(id: Int) {
        when (id) {
            111 -> finish()     //when profile is updated
            R.id.backBtn -> finish()
            R.id.cancelBtn -> finish()
            R.id.userEditProfileImg -> checkPermission(
                immutableListOf(
                    Manifest.permission.CAMERA,
                )
            ) {
                chooseFile(
                    arrayOf(
                        "image/png",
                        "image/jpg",
                        "image/jpeg",
                        "image/*"
                    )
                )
            }
            R.id.profPasswordEye1 -> {
                isPassShown1 = !isPassShown1
                showOrHidePassword1(isPassShown1)
            }
            R.id.profPasswordEye2 -> {
                isPassShown2 = !isPassShown2
                showOrHidePassword2(isPassShown2)
            }
            R.id.downBtn -> {
                if (mViewDataBinding.editProfileLayout.measuredHeight == mViewDataBinding.editProfileScrollView.scrollY +
                    mViewDataBinding.editProfileScrollView.height          //If scrollview is fully at bottom then this condition becomes true otherwise it is on top or in between
                ){
                    mViewDataBinding.editProfileScrollView.fullScroll(View.FOCUS_UP)
                }
                else{
                    mViewDataBinding.editProfileScrollView.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }

    private fun showOrHidePassword1(passShown: Boolean) {
        if (passShown) {
            mViewDataBinding.etUserPassword.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            mViewDataBinding.profPasswordEye1.setImageResource(R.drawable.visibility_on)
        }
        else {
            mViewDataBinding.etUserPassword.transformationMethod =
                PasswordTransformationMethod.getInstance()
            mViewDataBinding.profPasswordEye1.setImageResource(R.drawable.visibility_off)
        }
        mViewDataBinding.etUserPassword.setSelection(mViewDataBinding.etUserPassword.text.toString().length)
    }

    private fun showOrHidePassword2(passShown: Boolean) {
        if (passShown) {
            mViewDataBinding.etUserConfirmPassword.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            mViewDataBinding.profPasswordEye2.setImageResource(R.drawable.visibility_on)
        }
        else {
            mViewDataBinding.etUserConfirmPassword.transformationMethod =
                PasswordTransformationMethod.getInstance()
            mViewDataBinding.profPasswordEye2.setImageResource(R.drawable.visibility_off)
        }
        mViewDataBinding.etUserConfirmPassword.setSelection(mViewDataBinding.etUserConfirmPassword.text.toString().length)
    }

    private fun chooseFile(mimeTypes: Array<String>) {
        requireActivity().openFilePicker(
            getString(R.string.screen_edit_profile_text_choose_file), mimeTypes,
            completionHandler = fileCompletionHandler
        )
    }

    var fileCompletionHandler: ((resultCode: Int, data: Intent?) -> Unit)? = { _, intent ->
        intent?.let { intentData ->
            intentData.dataString?.let { viewModel.updateProfilePhoto(it, requireContext()) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel.validator?.validate()
    }
}