package com.zstronics.ceibro.ui.projects.newproject.overview.addnewstatus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.FragmentAddNewStatusBinding

class AddNewStatusSheet :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddNewStatusBinding
    var onAdd: ((status: String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_new_status,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addStatusBtn.setOnClickListener {
            val status = binding.statusText.text ?: ""
            if (status.isNotEmpty()) {
                onAdd?.invoke(status.toString())
                dismiss()
            }
        }
    }

}