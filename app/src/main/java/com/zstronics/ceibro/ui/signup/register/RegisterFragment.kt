package com.zstronics.ceibro.ui.signup.register

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.encryption.encryptDataToAesCbcInHex
import com.zstronics.ceibro.base.extensions.setupClearButtonWithAction
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.auth.login.Access
import com.zstronics.ceibro.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment :
    BaseNavViewModelFragment<FragmentRegisterBinding, IRegister.State, RegisterVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: RegisterVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_register
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.loginTextBtn -> navigateBack()
            R.id.registerContinueBtn -> {
                val phoneNumber = mViewDataBinding.ccp.fullNumberWithPlus               //getting unformatted number with prefix "+" i.e "+923001234567"
                val phoneCode = mViewDataBinding.ccp.selectedCountryCodeWithPlus        // +1, +92
                val nameCode = mViewDataBinding.ccp.selectedCountryNameCode             // US, PK

                try {
                    // Parsing the phone number with the selected country code
                    val phoneNumberUtil = PhoneNumberUtil.getInstance()
                    val parsedNumber = phoneNumberUtil.parse(phoneNumber, nameCode)

                    if (!phoneNumberUtil.isValidNumber(parsedNumber)) {
                        shortToastNow(resources.getString(R.string.error_message_phone_validation))
                    } else {
                        val formattedNumber = phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164)

                        val encryptedNumberAsClientId = encryptDataToAesCbcInHex(formattedNumber.toByteArray())
                        println("Encrypted PhoneNumber: ${encryptedNumberAsClientId} ::: ${formattedNumber}")

                        viewState.phoneNumber.value = formattedNumber
                        viewState.phoneCode.value = phoneCode

                        viewModel.getAuthTokenAndThenRegister(formattedNumber, encryptedNumberAsClientId) { authToken ->
                            navigateToVerifyNumber(encryptedNumberAsClientId, authToken)
                        }
                    }
                } catch (e: NumberParseException) {
                    shortToastNow("Error parsing phone number")
                }
            }
        }
    }


    private fun navigateToVerifyNumber(
        encryptedNumberAsClientId: String,
        authToken: Access
    ) {
        val bundle = Bundle()
        bundle.putString("fromFragment", "RegisterFragment")
        bundle.putString("phoneNumber", viewState.phoneNumber.value.toString())
        bundle.putString("phoneCode", viewState.phoneCode.value.toString())
        bundle.putString("clientId", encryptedNumberAsClientId)
        bundle.putString("authToken", authToken.token)
        bundle.putString("authTokenExpiry", authToken.expires)
        navigate(R.id.verifyNumberFragment, bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.ccp.registerCarrierNumberEditText(mViewDataBinding.editTextPhone)
        mViewDataBinding.editTextPhone.setupClearButtonWithAction()
    }

}