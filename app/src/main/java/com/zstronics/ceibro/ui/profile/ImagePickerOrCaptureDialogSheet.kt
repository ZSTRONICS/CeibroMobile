package com.zstronics.ceibro.ui.profile

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
import com.zstronics.ceibro.databinding.LayoutCameraOrGalleryPickerDialogBinding
import com.zstronics.ceibro.databinding.LayoutFeedbackDialogBinding

class ImagePickerOrCaptureDialogSheet constructor() : DialogFragment() {
    lateinit var binding: LayoutCameraOrGalleryPickerDialogBinding
    var onCameraBtnClick: (() -> Unit)? = null
    var onGalleryBtnClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.layout_camera_or_gallery_picker_dialog,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val drawable = GradientDrawable()
        drawable.setColor(resources.getColor(android.R.color.white))
        drawable.cornerRadius = resources.getDimension(R.dimen.dialog_small_corner)
        dialog?.window?.setBackgroundDrawable(drawable)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cameraBtnLayout.setOnClick {
            dismiss()
            onCameraBtnClick?.invoke()
        }

        binding.galleryBtnLayout.setOnClick {
            dismiss()
            onGalleryBtnClick?.invoke()
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