package com.zstronics.ceibro.ui.signup

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.fragment.app.viewModels
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.setupClearButtonWithAction
import com.zstronics.ceibro.base.extensions.shortToastNow
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
            R.id.signupContinueBtn -> {
                if (viewState.firstName.value.toString().length < 3) {
                    shortToastNow(resources.getString(R.string.error_message_first_name_validation))
                } else if (viewState.surname.value.toString().length < 2) {
                    shortToastNow(resources.getString(R.string.error_message_sur_name_validation))
                } else {
                    viewModel.doSignUp(viewState.firstName.value.toString(), viewState.surname.value.toString(), viewState.email.value.toString(),
                        viewState.companyName.value.toString(), viewState.jobTitle.value.toString(), viewState.password.value.toString()) {
                        navigate(R.id.photoFragment)
                    }
                }
            }
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackButtonDispatcher()
        mViewDataBinding.ccp.registerCarrierNumberEditText(mViewDataBinding.etPhone)

        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val parsedNumber = phoneNumberUtil.parse(viewState.phoneNumber.value.toString(), viewState.phoneCode.value.toString())

        val countryCode = parsedNumber.countryCode
        val nationalSignificantNumber = parsedNumber.nationalNumber

        mViewDataBinding.ccp.setCountryForPhoneCode(countryCode)
        mViewDataBinding.etPhone.setText(nationalSignificantNumber.toString())
    }
}