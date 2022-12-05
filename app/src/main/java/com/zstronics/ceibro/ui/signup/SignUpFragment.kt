package com.zstronics.ceibro.ui.signup

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment :
    BaseNavViewModelFragment<FragmentSignUpBinding, ISignUp.State, SignUpVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: SignUpVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_sign_up
    override fun toolBarVisibility(): Boolean = false
    private var isPassShown1 = false
    private var isPassShown2 = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.signInTextBtn -> navigateBack()
            112 -> navigateBack()
            R.id.signUpPasswordEye1 -> {
                isPassShown1 = !isPassShown1
                showOrHidePassword1(isPassShown1)
            }
            R.id.signUpPasswordEye2 -> {
                isPassShown2 = !isPassShown2
                showOrHidePassword2(isPassShown2)
            }
        }
    }

    private fun showOrHidePassword1(passShown: Boolean) {
        if (passShown) {
            mViewDataBinding.etPassword.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            mViewDataBinding.signUpPasswordEye1.setImageResource(R.drawable.visibility_on)
        }
        else {
            mViewDataBinding.etPassword.transformationMethod =
                PasswordTransformationMethod.getInstance()
            mViewDataBinding.signUpPasswordEye1.setImageResource(R.drawable.visibility_off)
        }
        mViewDataBinding.etPassword.setSelection(mViewDataBinding.etPassword.text.toString().length)
    }

    private fun showOrHidePassword2(passShown: Boolean) {
        if (passShown) {
            mViewDataBinding.etConfirmPassword.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            mViewDataBinding.signUpPasswordEye2.setImageResource(R.drawable.visibility_on)
        }
        else {
            mViewDataBinding.etConfirmPassword.transformationMethod =
                PasswordTransformationMethod.getInstance()
            mViewDataBinding.signUpPasswordEye2.setImageResource(R.drawable.visibility_off)
        }
        mViewDataBinding.etConfirmPassword.setSelection(mViewDataBinding.etConfirmPassword.text.toString().length)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel.validator?.validate()
    }
}