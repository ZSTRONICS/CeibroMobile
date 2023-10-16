package com.zstronics.ceibro.ui.profile.editprofile

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.extensions.gone
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.extensions.visible
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.data.repos.projects.member.EditProjectMemberRequest
import com.zstronics.ceibro.data.repos.projects.member.GetProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.databinding.FragmentChangePasswordBinding
import com.zstronics.ceibro.databinding.FragmentChangePhoneNumberBinding
import com.zstronics.ceibro.databinding.FragmentChangePhoneNumberVerifyOtpBinding
import com.zstronics.ceibro.databinding.FragmentCreateNewPasswordBinding
import com.zstronics.ceibro.databinding.FragmentEditProjectMemberBinding

class ChangePhoneNumberVerifyOtpSheet constructor() : BottomSheetDialogFragment() {
    lateinit var binding: FragmentChangePhoneNumberVerifyOtpBinding
    var onVerificationDone: ((otp: String) -> Unit)? = null
    var onVerificationResendCode: (() -> Unit)? = null
    var onVerificationDismiss: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_change_phone_number_verify_otp,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
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
        } else {
            binding.sendCodeAgainBtn.isEnabled = false
            binding.sendCodeAgainBtn.setTextColor(resources.getColor(R.color.appTextGrey))
            binding.sendCodeAgainBtn.text = "${timeLeftInMillis / 1000}s"
        }

        binding.confirmBtn.setOnClick {
            val otp = binding.otpField.text.toString()
            if (otp.length == 6) {
                onVerificationDone?.invoke(otp)
            } else {
                shortToastNow(resources.getString(R.string.error_message_otp_length))
            }
        }

        binding.sendCodeAgainBtn.setOnClick {
            dismiss()
            onVerificationResendCode?.invoke()
            if (timeLeftInMillis <= 0) {
                startTimer()
            }
        }

        binding.cancelBtn.setOnClick {
            dismiss()
            onVerificationDismiss?.invoke()
        }
    }

    private fun validatePassword(password: String): Boolean {
        val regex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}\$"
        return password.length in 8..35
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }


    private fun startTimer() {
        timerInProgress = true
        binding.sendCodeAgainBtn.text = ""
        binding.sendCodeAgainBtn.isEnabled = false
        binding.sendCodeAgainBtn.setTextColor(resources.getColor(R.color.appTextGrey))

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
        binding.sendCodeAgainBtn.text = "${seconds}s"

        // If timer is finished, update the text on the "Resend OTP" button
        if (timeLeftInMillis <= 0) {
            timerInProgress = false
            binding.sendCodeAgainBtn.text = resources.getString(R.string.send_again_text)
            binding.sendCodeAgainBtn.isEnabled = true
            binding.sendCodeAgainBtn.setTextColor(resources.getColor(R.color.appBlue))
        } else {
            binding.sendCodeAgainBtn.isEnabled = false
            binding.sendCodeAgainBtn.setTextColor(resources.getColor(R.color.appTextGrey))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = true
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }
}