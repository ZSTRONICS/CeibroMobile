package com.zstronics.ceibro.ui.projects.newproject.overview.addnewstatus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.databinding.FragmentAddNewStatusBinding

class AddNewStatusSheet constructor(status: String) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddNewStatusBinding
    var onAdd: ((status: String) -> Unit)? = null
    var onEdited: ((status: String) -> Unit)? = null
    var oldStatus = status

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
        if (oldStatus != "") {
            binding.statusText.setText(oldStatus)
            binding.addStatusBtn.text = context?.getString(R.string.update_btn_text)
            binding.headingText.text = context?.getString(R.string.update_status_heading)
        }

        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addStatusBtn.setOnClickListener {
            val status = binding.statusText.text ?: ""
            if (oldStatus != "") {
                if (status.isNotEmpty()) {
                    onEdited?.invoke(status.toString())
                    dismiss()
                }
                else {
                    shortToastNow("Please rename the status")
                }
            }
            else {
                if (status.isNotEmpty()) {
                    onAdd?.invoke(status.toString())
                    dismiss()
                }
                else {
                    shortToastNow("Please enter a status")
                }
            }
        }
    }

}