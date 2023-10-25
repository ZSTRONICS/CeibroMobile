package com.zstronics.ceibro.ui.login

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.fragment.app.viewModels
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.onesignal.OneSignal
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment :
    BaseNavViewModelFragment<FragmentLoginBinding, ILogin.State, LoginVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: LoginVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_login
    override fun toolBarVisibility(): Boolean = false
    private var isPassShown = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.signUpTextBtn -> {
                navigate(R.id.registerFragment)
            }

            R.id.forgotPasswordBtn -> navigate(R.id.forgotPasswordFragment)
            R.id.loginPasswordEye -> {
                isPassShown = !isPassShown
                showOrHidePassword(isPassShown)
            }

            R.id.loginBtn -> {
                val phoneNumber =
                    mViewDataBinding.ccp.fullNumberWithPlus               //getting unformatted number with prefix "+" i.e "+923001234567"
                val phoneCode = mViewDataBinding.ccp.selectedCountryCodeWithPlus        // +1, +92
                val nameCode = mViewDataBinding.ccp.selectedCountryNameCode             // US, PK
                val password = viewState.password.value.toString()
                val rememberMe = viewState.rememberMe.value

                try {
                    // Parsing the phone number with the selected country code
                    val phoneNumberUtil = PhoneNumberUtil.getInstance()
                    val parsedNumber = phoneNumberUtil.parse(phoneNumber, nameCode)

                    if (!phoneNumberUtil.isValidNumber(parsedNumber)) {
                        shortToastNow(resources.getString(R.string.error_message_phone_validation))
                    } else if (!validatePassword(password)) {
                        shortToastNow(resources.getString(R.string.error_message_password_length))
                    } else {
                        val formattedNumber = phoneNumberUtil.format(
                            parsedNumber,
                            PhoneNumberUtil.PhoneNumberFormat.E164
                        )
                        viewModel.doLogin(formattedNumber, password, rememberMe ?: false) {
                            navigateToAppLoadingScreen()
                        }
                    }
                } catch (e: NumberParseException) {
                    shortToastNow("Error parsing phone number")
                }
            }
        }
    }

    private fun validatePassword(password: String): Boolean {
        val regex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}\$"
        return password.length in 8..35
    }

    private fun showOrHidePassword(passShown: Boolean) {
        if (passShown) {
            mViewDataBinding.editTextPassword.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            mViewDataBinding.loginPasswordEye.setImageResource(R.drawable.icon_visibility_on)
        } else {
            mViewDataBinding.editTextPassword.transformationMethod =
                PasswordTransformationMethod.getInstance()
            mViewDataBinding.loginPasswordEye.setImageResource(R.drawable.icon_visibility_off)
        }
        mViewDataBinding.editTextPassword.setSelection(mViewDataBinding.editTextPassword.text.toString().length)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.ccp.registerCarrierNumberEditText(mViewDataBinding.editTextPhone)
        OneSignal.promptForPushNotifications()
    }
}