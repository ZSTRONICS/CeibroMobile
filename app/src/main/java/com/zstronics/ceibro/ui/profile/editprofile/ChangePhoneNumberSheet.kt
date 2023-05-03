package com.zstronics.ceibro.ui.profile.editprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import com.zstronics.ceibro.databinding.FragmentCreateNewPasswordBinding
import com.zstronics.ceibro.databinding.FragmentEditProjectMemberBinding

class ChangePhoneNumberSheet constructor() : BottomSheetDialogFragment() {
    lateinit var binding: FragmentChangePhoneNumberBinding
    var onChangeNumber: ((newNumber: String, countryCode: String, password: String) -> Unit)? = null
    var onChangeNumberDismiss: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_change_phone_number,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ccp.registerCarrierNumberEditText(binding.editTextPhone)

        binding.changeNumberContinueBtn.setOnClick {
            val phoneNumber = binding.ccp.fullNumberWithPlus               //getting unformatted number with prefix "+" i.e "+923001234567"
            val phoneCode = binding.ccp.selectedCountryCodeWithPlus        // +1, +92
            val nameCode = binding.ccp.selectedCountryNameCode             // US, PK
            val password = binding.etPasswordField.text.toString()

            try {
                // Parsing the phone number with the selected country code
                val phoneNumberUtil = PhoneNumberUtil.getInstance()
                val parsedNumber = phoneNumberUtil.parse(phoneNumber, nameCode)

                if (!phoneNumberUtil.isValidNumber(parsedNumber)) {
                    shortToastNow(resources.getString(R.string.error_message_phone_validation))
                } else if (!validatePassword(password)) {
                    shortToastNow(resources.getString(R.string.error_message_password_length))
                }
                else {
                    val formattedNumber = phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
                    onChangeNumber?.invoke(formattedNumber, phoneCode, password)
                }
            } catch (e: NumberParseException) {
                shortToastNow("Error parsing phone number")
            }
        }

        binding.cancelBtn.setOnClick {
            dismiss()
            onChangeNumberDismiss?.invoke()
        }
    }

    private fun validatePassword(password: String): Boolean {
        val regex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@\$%^&*-]).{8,}\$"
        return password.length in 8..35
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}