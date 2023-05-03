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
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.extensions.gone
import com.zstronics.ceibro.base.extensions.visible
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.data.repos.projects.member.EditProjectMemberRequest
import com.zstronics.ceibro.data.repos.projects.member.GetProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.databinding.FragmentChangePasswordBinding
import com.zstronics.ceibro.databinding.FragmentCreateNewPasswordBinding
import com.zstronics.ceibro.databinding.FragmentEditProjectMemberBinding

class ChangePasswordSheet constructor() : BottomSheetDialogFragment() {
    lateinit var binding: FragmentChangePasswordBinding
    var onChangePassword: ((oldPassword: String, newPassword: String) -> Unit)? = null
    var onChangePasswordDismiss: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_change_password,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.changePasswordContinueBtn.setOnClick {
            val oldPassword = binding.etOldPasswordField.text.toString()
            val newPassword = binding.etPasswordField.text.toString()
            val confirmPassword = binding.etConfirmPasswordField.text.toString()

            if (oldPassword.length < 6) {
                showToast(resources.getString(R.string.error_message_invalid_old_password))
            } else if (!validatePassword(newPassword)) {
                showToast(resources.getString(R.string.error_message_invalid_new_password))
            } else if (!validatePassword(confirmPassword)) {
                showToast(resources.getString(R.string.error_message_invalid_confirm_password))
            } else if (newPassword != confirmPassword) {
                showToast(resources.getString(R.string.error_message_not_equal_new_password))
            } else {
                onChangePassword?.invoke(oldPassword, newPassword)
            }
        }

        binding.cancelBtn.setOnClick {
            dismiss()
            onChangePasswordDismiss?.invoke()
        }
    }

    private fun validatePassword(password: String): Boolean {
        val regex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@\$%^&*-]).{8,}\$"
        return password.matches(Regex(regex))
                && password.length >= 8
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}