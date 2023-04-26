package com.zstronics.ceibro.ui.forgotpassword

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentForgotPasswordBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordFragment :
    BaseNavViewModelFragment<FragmentForgotPasswordBinding, IForgotPassword.State, ForgotPasswordVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ForgotPasswordVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_forgot_password
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.loginTextBtn -> navigateBack()
            R.id.resetPasswordBtn -> {
//                val phoneNumber = mViewDataBinding.ccp.fullNumberWithPlus               //getting unformatted number with prefix "+" i.e "+923001234567"
//                val phoneCode = mViewDataBinding.ccp.selectedCountryCodeWithPlus        // +1, +92
//                val nameCode = mViewDataBinding.ccp.selectedCountryNameCode             // US, PK
//                try {
//                    // Parsing the phone number with the selected country code
//                    val phoneNumberUtil = PhoneNumberUtil.getInstance()
//                    val parsedNumber = phoneNumberUtil.parse(phoneNumber, nameCode)
//
//                    if (phoneNumberUtil.isValidNumber(parsedNumber)) {
//
//                        val formattedNumber = phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
//                        shortToastNow("Reset Password phone number: $formattedNumber")
//
//                    } else {
//                        shortToastNow("Invalid phone number")
//                    }
//                } catch (e: NumberParseException) {
//                    shortToastNow("Error parsing phone number")
//                }
                navigate(R.id.verifyNumberFragment)
            }
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.ccp.registerCarrierNumberEditText(mViewDataBinding.editTextPhone)
    }

}