package com.zstronics.ceibro.ui.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.fragment.app.viewModels
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.isEmail
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

        mViewDataBinding.etNameField.addTextChangedListener(textWatcher)
        mViewDataBinding.etSurnameField.addTextChangedListener(textWatcher)
        mViewDataBinding.etEmailField.addTextChangedListener(textWatcher)
        mViewDataBinding.etCompanyField.addTextChangedListener(textWatcher)
        mViewDataBinding.etJobField.addTextChangedListener(textWatcher)
        mViewDataBinding.etPasswordField.addTextChangedListener(textWatcher)
        mViewDataBinding.etConfirmPasswordField.addTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // This method is called to notify you that the text has been changed and processed
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // This method is called to notify you that the text is about to change
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            mViewDataBinding.signupContinueBtn.isEnabled =
                        viewState.firstName.value.toString().isNotEmpty() &&
                        viewState.surname.value.toString().isNotEmpty() &&
                        viewState.email.value.toString().isEmail() &&
                        validatePassword(viewState.password.value.toString()) &&
                        validatePassword(viewState.confirmPassword.value.toString()) &&
                        viewState.password.value.toString() == viewState.confirmPassword.value.toString()

            mViewDataBinding.etNameField.error =
                if (viewState.firstName.value.toString().isEmpty()) {
                    resources.getString(R.string.error_message_name_validation)
                } else {
                    null
                }
            mViewDataBinding.etSurnameField.error =
                if (viewState.surname.value.toString().isEmpty()) {
                    resources.getString(R.string.error_message_name_validation)
                } else {
                    null
                }
            mViewDataBinding.etEmailField.error =
                if (!viewState.email.value.toString().isEmail()) {
                    resources.getString(R.string.error_message_email_validation)
                } else {
                    null
                }
        }
    }

    private fun validatePassword(password: String): Boolean {
        val regex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@\$%^&*-]).{8,}\$"
        return password.matches(Regex(regex))
                && password.length >= 8
    }

}