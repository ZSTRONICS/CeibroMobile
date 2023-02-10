package com.zstronics.ceibro.ui.tasks.taskdetailview

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.FragmentTaskDetailSheetBinding

class FragmentTaskDetailSheet constructor(val taskTitle: String, val taskDetail: String) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentTaskDetailSheetBinding

//    var onDoneClick: ((view: View?, dataList: ArrayList<Member>) -> Unit)? =
//        null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_task_detail_sheet,
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

        binding.taskDescriptionText.setText(
            taskDetail.ifEmpty { "No description added by creator" }
        )

        binding.taskTitleText.setText(
            taskTitle.ifEmpty { "Can't fetch title" }
        )

//        binding.doneBtn.setOnClickListener {
//            onDoneClick?.invoke(it, adapter.dataList)
//            dismiss()
//        }
    }
}