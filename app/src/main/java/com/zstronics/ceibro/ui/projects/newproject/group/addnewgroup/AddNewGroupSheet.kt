package com.zstronics.ceibro.ui.projects.newproject.group.addnewgroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.databinding.FragmentAddNewGroupBinding
import com.zstronics.ceibro.databinding.FragmentAddNewStatusBinding

class AddNewGroupSheet constructor(groupName: String) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddNewGroupBinding
    var onGroupAdd: ((groupName: String) -> Unit)? = null
    var onGroupEdited: ((status: String) -> Unit)? = null
    var oldGroupName = groupName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_new_group,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (oldGroupName != "") {
            binding.groupText.setText(oldGroupName)
            binding.addGroupBtn.text = context?.getString(R.string.update_btn_text)
            binding.headingText.text = context?.getString(R.string.edit_group_heading)
        }

        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addGroupBtn.setOnClickListener {
            val groupText = binding.groupText.text ?: ""
            if (oldGroupName != "") {
                if (groupText.isNotEmpty()) {
                    onGroupEdited?.invoke(groupText.toString())
                    dismiss()
                }
                else {
                    shortToastNow("Please rename the group")
                }
            }
            else {
                if (groupText.isNotEmpty()) {
                    onGroupAdd?.invoke(groupText.toString())
                    dismiss()
                }
                else {
                    shortToastNow("Please enter group name")
                }
            }
        }
    }

}