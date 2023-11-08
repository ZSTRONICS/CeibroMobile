package com.zstronics.ceibro.ui.projectv2.newprojectv2


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.FragmentAddPhotoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddNewPhotoBottomSheet(val callback: (String) -> Unit) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddPhotoBinding



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_photo,
            container,
            false
        )
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cancelBtn.setOnClickListener {
            callback.invoke("cancel")
            dismiss()
        }
        binding.tvFromLocal.setOnClickListener {
            callback.invoke("local")
            dismiss()
        }
        binding.tvFromCeibroFiles.setOnClickListener {
            callback.invoke("ceibro")
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = true
        }
        return dialog

    }
}