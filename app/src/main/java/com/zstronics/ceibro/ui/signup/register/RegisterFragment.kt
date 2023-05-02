package com.zstronics.ceibro.ui.signup.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.setupClearButtonWithAction
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.databinding.FragmentRegisterBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
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
            102 -> navigateToVerifyNumber("RegisterFragment")
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
                        viewState.phoneNumber.value = formattedNumber
                        viewModel.registerNumber(formattedNumber)
                    }
                } catch (e: NumberParseException) {
                    shortToastNow("Error parsing phone number")
                }
            }
        }
    }


    private fun navigateToVerifyNumber(currentFragment: String) {
        val bundle = Bundle()
        bundle.putString("fromFragment", currentFragment)
        bundle.putString("phoneNumber", viewState.phoneNumber.value.toString())
        navigate(R.id.verifyNumberFragment, bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.ccp.registerCarrierNumberEditText(mViewDataBinding.editTextPhone)
        mViewDataBinding.editTextPhone.setupClearButtonWithAction()
    }

}