package com.zstronics.ceibro.ui.verifynumber

import android.app.Dialog
import android.os.Bundle
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
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.extensions.gone
import com.zstronics.ceibro.base.extensions.visible
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.data.repos.projects.member.EditProjectMemberRequest
import com.zstronics.ceibro.data.repos.projects.member.GetProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.databinding.FragmentCreateNewPasswordBinding
import com.zstronics.ceibro.databinding.FragmentEditProjectMemberBinding

class CreateNewPasswordSheet constructor() : BottomSheetDialogFragment() {
    lateinit var binding: FragmentCreateNewPasswordBinding
    var onNewPasswordCreation: ((password: String) -> Unit)? = null
    var onNewPasswordCreationDismiss: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_create_new_password,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.newPasswordContinueBtn.setOnClick {
            val password = binding.etPasswordField.text.toString()
            val confirmPassword = binding.etConfirmPasswordField.text.toString()

            if (!validatePassword(password)) {
                showToast(resources.getString(R.string.error_message_invalid_password))
            } else if (!validatePassword(confirmPassword)) {
                showToast(resources.getString(R.string.error_message_invalid_confirm_password))
            } else if (password != confirmPassword) {
                showToast(resources.getString(R.string.error_message_not_equal_password))
            } else {
                onNewPasswordCreation?.invoke(password)
            }
        }

        binding.closeBtn.setOnClick {
            dismiss()
            onNewPasswordCreationDismiss?.invoke()
        }
    }

    private fun validatePassword(password: String): Boolean {
        val regex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}\$"
        return password.matches(Regex(regex))
                && password.length >= 8
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
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