package com.zstronics.ceibro.ui.dashboard

import android.app.Dialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.databinding.LayoutFeedbackDialogBinding

class FeedbackDialogSheet constructor() : DialogFragment() {
    lateinit var binding: LayoutFeedbackDialogBinding
    var onEstonianFormBtn: (() -> Unit)? = null
    var onEnglishFormBtn: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.layout_feedback_dialog,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val drawable = GradientDrawable()
        drawable.setColor(resources.getColor(android.R.color.white))
        drawable.cornerRadius = resources.getDimension(R.dimen.dialog_medium_corner)
        dialog?.window?.setBackgroundDrawable(drawable)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        onChangePassword?.invoke(oldPassword, newPassword)

        binding.estonianFormBtn.setOnClick {
            dismiss()
            onEstonianFormBtn?.invoke()
        }

        binding.englishFormBtn.setOnClick {
            dismiss()
            onEnglishFormBtn?.invoke()
        }

        binding.cancelBtn.setOnClick {
            dismiss()
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
//        if (dialog is BottomSheetDialog) {
//            dialog.behavior.skipCollapsed = true
//            dialog.behavior.state = STATE_EXPANDED
//        }
        return dialog

    }
}