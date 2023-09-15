package com.zstronics.ceibro.ui.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.isEmail
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
                viewModel.doSignUp(
                    viewState.firstName.value.toString().trim(),
                    viewState.surname.value.toString().trim(),
                    viewState.email.value.toString(),
                    viewState.companyName.value.toString().trim(),
                    viewState.jobTitle.value.toString().trim(),
                    viewState.password.value.toString()
                ) {
                    navigate(R.id.photoFragment)
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackButtonDispatcher()
        mViewDataBinding.ccp.registerCarrierNumberEditText(mViewDataBinding.etPhone)

        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val parsedNumber = phoneNumberUtil.parse(
            viewState.phoneNumber.value.toString(),
            viewState.phoneCode.value.toString()
        )

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
            val firstName = viewState.firstName.value.toString().trim()
            val surname = viewState.surname.value.toString().trim()

            mViewDataBinding.signupContinueBtn.isEnabled =
                (firstName.isNotEmpty() && isUserNameValid(firstName)) &&
                        (surname.isNotEmpty() && isUserNameValid(surname)) &&
                        viewState.email.value.toString().isEmail() &&
                        validatePassword(viewState.password.value.toString()) &&
                        validatePassword(viewState.confirmPassword.value.toString()) &&
                        viewState.password.value.toString() == viewState.confirmPassword.value.toString()

            if (mViewDataBinding.etNameField.isFocused) {
                mViewDataBinding.etName.error =
                    if (firstName.isEmpty()) {
                        resources.getString(R.string.error_message_first_name_empty)
                    } else if (!startsWithAlphabet(firstName)) {
                        resources.getString(R.string.error_message_first_name_alphabet_required)
                    } else if (!isUserNameValid(firstName)) {
                        resources.getString(R.string.error_message_special_character_not_allowed_in_name)
                    } else {
                        null
                    }
                if (firstName.isNotEmpty() && isUserNameValid(firstName)) {
                    mViewDataBinding.etName.isErrorEnabled = false
                }

            } else if (mViewDataBinding.etSurnameField.isFocused) {
                mViewDataBinding.etSurname.error =
                    if (surname.isEmpty()) {
                        resources.getString(R.string.error_message_surname_name_empty)
                    } else if (!startsWithAlphabet(surname)) {
                        resources.getString(R.string.error_message_surname_alphabet_required)
                    } else if (!isUserNameValid(surname)) {
                        resources.getString(R.string.error_message_special_character_not_allowed_in_name)
                    } else {
                        null
                    }
                if (surname.isNotEmpty() && isUserNameValid(surname)) {
                    mViewDataBinding.etSurname.isErrorEnabled = false
                }

            } else if (mViewDataBinding.etEmailField.isFocused) {
                mViewDataBinding.etEmail.error =
                    if (!viewState.email.value.toString().isEmail()) {
                        resources.getString(R.string.error_message_email_validation)
                    } else {
                        null
                    }
                if (viewState.email.value.toString().isEmail()) {
                    mViewDataBinding.etEmail.isErrorEnabled = false
                }

            } else if (mViewDataBinding.etPasswordField.isFocused) {
                mViewDataBinding.etPassword.error =
                    if (!validatePassword(viewState.password.value.toString())) {
                        resources.getString(R.string.error_message_password_regex_validation)
                    } else {
                        null
                    }

                if (validatePassword(viewState.password.value.toString())) {
                    mViewDataBinding.etPassword.isErrorEnabled = false
                }
                if (viewState.password.value.toString() == viewState.confirmPassword.value.toString()) {
                    mViewDataBinding.etConfirmPassword.error = null
                    mViewDataBinding.etConfirmPassword.isErrorEnabled = false
                }
            } else if (mViewDataBinding.etConfirmPasswordField.isFocused) {
                mViewDataBinding.etConfirmPassword.error =
                    if (viewState.password.value.toString() != viewState.confirmPassword.value.toString()) {
                        resources.getString(R.string.error_message_not_equal_password)
                    } else {
                        null
                    }
                if (viewState.password.value.toString() == viewState.confirmPassword.value.toString()) {
                    mViewDataBinding.etConfirmPassword.isErrorEnabled = false
                }
            }

        }
    }

    /*private fun validateFields(fieldId: Int) {
        when (fieldId) {
            R.id.etNameField -> {
                mViewDataBinding.etNameField.error =
                    if (viewState.firstName.value.toString().isEmpty()) {
                        resources.getString(R.string.error_message_name_validation)
                    } else {
                        null
                    }
            }
            R.id.etSurnameField -> {
                mViewDataBinding.etSurnameField.error =
                    if (viewState.surname.value.toString().isEmpty()) {
                        resources.getString(R.string.error_message_name_validation)
                    } else {
                        null
                    }
            }
            R.id.etEmailField -> {
                mViewDataBinding.etEmailField.error =
                    if (!viewState.email.value.toString().isEmail()) {
                        resources.getString(R.string.error_message_email_validation)
                    } else {
                        null
                    }
            }
            R.id.etPasswordField -> {
                mViewDataBinding.etPassword.error =
                    if (!validatePassword(viewState.password.value.toString())) {
                        resources.getString(R.string.error_message_password_regex_validation)
                    } else {
                        null
                    }
            }
            R.id.etConfirmPasswordField -> {
//                if (!validatePassword(viewState.confirmPassword.value.toString())) {
//                    mViewDataBinding.etConfirmPassword.error =
//                        if (!validatePassword(viewState.confirmPassword.value.toString())) {
//                            resources.getString(R.string.error_message_invalid_confirm_password)
//                        } else {
//                            null
//                        }
//                }
//                else {
                mViewDataBinding.etConfirmPassword.error =
                    if (viewState.password.value.toString() != viewState.confirmPassword.value.toString()) {
                        resources.getString(R.string.error_message_not_equal_password)
                    } else {
                        null
                    }
//                }
            }
        }

        mViewDataBinding.signupContinueBtn.isEnabled =
            viewState.firstName.value.toString().isNotEmpty() &&
                    viewState.surname.value.toString().isNotEmpty() &&
                    viewState.email.value.toString().isEmail() &&
                    validatePassword(viewState.password.value.toString()) &&
                    validatePassword(viewState.confirmPassword.value.toString()) &&
                    viewState.password.value.toString() == viewState.confirmPassword.value.toString()
    }*/

    private fun validatePassword(password: String): Boolean {
        val regex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@\$%^&*-]).{8,}\$"
        return password.matches(Regex(regex))
                && password.length >= 8
    }

}