package com.zstronics.ceibro.ui.projects.newproject.documents.newfolder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.databinding.FragmentNewFolderBinding

class NewFolderSheet :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentNewFolderBinding
    var onFolderAdd: ((groupName: String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_new_folder,
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

        binding.addBtn.setOnClickListener {
            val inputText = binding.inputText.text ?: ""
            if (inputText.isNotEmpty()) {
                onFolderAdd?.invoke(inputText.toString())
                dismiss()
            } else {
                shortToastNow("Please enter folder name")
            }
        }
    }

}