package com.zstronics.ceibro.ui.tasks.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.databinding.FragmentTaskDetailSheetBinding
import com.zstronics.ceibro.databinding.FragmentTaskFilterSheetBinding

class FragmentTaskFilterSheet constructor() :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentTaskFilterSheetBinding

    var onConfirmClickListener: (() -> Unit)? = null
    var onClearAllClickListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_task_filter_sheet,
            container,
            false
        )
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        binding.taskFilterClearAllBtn.setOnClickListener {
            dismiss()
        }
        binding.confirmFilterBtn.setOnClickListener {
            dismiss()
        }
        
    }
}