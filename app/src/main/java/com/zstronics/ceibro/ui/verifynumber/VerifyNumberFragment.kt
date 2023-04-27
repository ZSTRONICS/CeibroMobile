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
import com.zstronics.ceibro.ui.projects.newproject.members.memberprofile.EditProjectMemberSheet
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
                if (viewState.previousFragment.value.equals("RegisterFragment", true)) {
                    navigate(R.id.termsFragment)
                } else if (viewState.previousFragment.value.equals("ForgotPasswordFragment", true)) {
                    showPasswordBottomSheet()
                }
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
    private var timerInProgress: Boolean = false
    private val COUNTDOWN_INTERVAL: Long = 1000 // 1 second
    private val TOTAL_TIME_IN_MILLIS: Long = 60000 // 60 seconds


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!timerInProgress) {
            startTimer()
        }
        else {
            mViewDataBinding.sendCodeAgainBtn.isEnabled = false
            mViewDataBinding.sendCodeAgainBtn.setTextColor(resources.getColor(R.color.appTextGrey))
            mViewDataBinding.sendCodeAgainBtn.text = "${timeLeftInMillis / 1000}s"
        }

    }


    fun showPasswordBottomSheet() {
        val sheet = CreateNewPasswordSheet()

        sheet.onNewPasswordCreation = { password ->
//            viewModel.editMember(projectLive.value?.id ?: "", member.id, body) {
//                projectStateHandler.onMemberAdd()
//                sheet.dismiss()
//            }
            shortToastNow(password)
        }
        sheet.onNewPasswordCreationDismiss = {
            navigateBack()
        }
        sheet.isCancelable = false
        sheet.show(childFragmentManager, "CreateNewPasswordSheet")
    }


    private fun startTimer() {
        timerInProgress = true
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
                timerInProgress = false
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
            timerInProgress = false
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