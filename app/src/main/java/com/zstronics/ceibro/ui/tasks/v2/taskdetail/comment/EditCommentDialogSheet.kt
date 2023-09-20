package com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment

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
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.databinding.LayoutEditCommentDialogBinding
import ee.zstronics.ceibro.camera.PickedImages

class EditCommentDialogSheet(data: PickedImages) : DialogFragment() {
    lateinit var binding: LayoutEditCommentDialogBinding
    val commentData = data
    var updateCommentOnClick: ((text: String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.layout_edit_comment_dialog,
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

        binding.originalCommentText.isFocusable = false
        binding.originalCommentText.isClickable = false
        binding.originalCommentText.isLongClickable = true
        binding.originalCommentText.alpha = 0.5f
        binding.originalCommentText.setText(commentData.comment)
        binding.updatedCommentText.setText(commentData.comment)

        binding.updateBtn.setOnClickListener {
            val updatedText = binding.updatedCommentText.text.toString().trim()
            if (updatedText.isNotEmpty()) {
                dismiss()
                updateCommentOnClick?.invoke(updatedText)
            } else {
                shortToastNow("Write some comment to update")
            }
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