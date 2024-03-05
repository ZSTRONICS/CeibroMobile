package com.zstronics.ceibro.ui.tasks.v3.bottomsheets


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.databinding.FragmentTaskTypeBinding

class TaskTypeBottomSheet(val type: String, val callback: (String) -> Unit) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentTaskTypeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_task_type,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (type.equals("All", true)) {
            binding.rbAll.isChecked = true
        } else if (type.equals("From me", true)) {
            binding.rbFromMe.isChecked = true
        } else if (type.equals("To Me", true)) {
            binding.rbToMe.isChecked = true
        }
        binding.rbAll.setOnClick {
            callback.invoke("All")
            dismiss()
        }
        binding.rbFromMe.setOnClick {
            callback.invoke("From me")
            dismiss()
        }
        binding.rbToMe.setOnClick {
            callback.invoke("To me")
            dismiss()
        }

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