package com.zstronics.ceibro.ui.signup

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.setupClearButtonWithAction
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
    override fun onClick(id: Int) {
        when (id) {
            112 -> navigateBack()
            113 -> navigate(R.id.photoFragment)

        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.ccp.registerCarrierNumberEditText(mViewDataBinding.etPhone)
        mViewDataBinding.ccp.setCountryForPhoneCode(+1)
        mViewDataBinding.etPhone.setText("53441413")
    }
}