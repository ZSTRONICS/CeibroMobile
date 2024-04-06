package com.zstronics.ceibro.ui.dashboard.bottomSheet


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
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentLocaleChangeBinding

class ChangeLocaleBottomSheet(val type: String, val callback: (String) -> Unit) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentLocaleChangeBinding
    private var LocaleType = type

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_locale_change,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (type.equals("English", true)) {
            binding.rbEnglish.isChecked = true
        } else {
            binding.cbRussian.isChecked = true
        }
        binding.rbEnglish.setOnClick {
            LocaleType = "English"


        }
        binding
        binding.cbRussian.setOnClick {
            LocaleType = "Russian"

        }
        binding.ok.setOnClick {
            if (!(type.equals(LocaleType, true))) {
                callback.invoke(LocaleType)
            }else{
                callback.invoke("")
            }
            dismiss()
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = false
            dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        return dialog
    }
}