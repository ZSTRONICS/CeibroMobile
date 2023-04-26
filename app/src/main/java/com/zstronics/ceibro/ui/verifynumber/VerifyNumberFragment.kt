package com.zstronics.ceibro.ui.verifynumber

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.fragment.app.viewModels
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.databinding.FragmentForgotPasswordBinding
import com.zstronics.ceibro.databinding.FragmentVerifyNumberBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerifyNumberFragment :
    BaseNavViewModelFragment<FragmentVerifyNumberBinding, IVerifyNumber.State, VerifyNumberVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: VerifyNumberVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_verify_number
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.loginTextBtn -> {
                launchActivity<NavHostPresenterActivity>(
                    options = Bundle(),
                    clearPrevious = true
                ) {
                    putExtra(NAVIGATION_Graph_ID, R.navigation.onboarding_nav_graph)
                    putExtra(
                        NAVIGATION_Graph_START_DESTINATION_ID,
                        R.id.loginFragment
                    )
                }
            }
            R.id.closeBtn -> {
                mViewDataBinding.codeSentLayout.visibility = View.GONE
            }
            R.id.confirmBtn -> {

            }
            R.id.sendCodeAgainBtn -> {
                if (timeLeftInMillis <= 0) {
                    startTimer()
                }
                mViewDataBinding.codeSentLayout.visibility = View.VISIBLE
                //now resend verification code again, hit API
            }
        }
    }

    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 0
    private val COUNTDOWN_INTERVAL: Long = 1000 // 1 second
    private val TOTAL_TIME_IN_MILLIS: Long = 60000 // 60 seconds


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startTimer()
//        mViewDataBinding.ccp.registerCarrierNumberEditText(mViewDataBinding.editTextPhone)
    }

    private fun startTimer() {
        mViewDataBinding.sendCodeAgainBtn.text = ""
        mViewDataBinding.sendCodeAgainBtn.isEnabled = false
        mViewDataBinding.sendCodeAgainBtn.setTextColor(resources.getColor(R.color.appTextGrey))

        timeLeftInMillis = TOTAL_TIME_IN_MILLIS

        // Create a countdown timer
        countDownTimer = object : CountDownTimer(timeLeftInMillis, COUNTDOWN_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }
            override fun onFinish() {
                timeLeftInMillis = 0
                updateTimerText()
            }
        }
        countDownTimer.start()
    }

    private fun updateTimerText() {
        val seconds = timeLeftInMillis / 1000
        mViewDataBinding.sendCodeAgainBtn.text = "${seconds}s"

        // If timer is finished, update the text on the "Resend OTP" button
        if (timeLeftInMillis <= 0) {
            mViewDataBinding.sendCodeAgainBtn.text = resources.getString(R.string.send_again_text)
            mViewDataBinding.sendCodeAgainBtn.isEnabled = true
            mViewDataBinding.sendCodeAgainBtn.setTextColor(resources.getColor(R.color.appBlue))
        } else {
            mViewDataBinding.sendCodeAgainBtn.isEnabled = false
            mViewDataBinding.sendCodeAgainBtn.setTextColor(resources.getColor(R.color.appTextGrey))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }
}