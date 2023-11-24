package com.zstronics.ceibro.ui.dashboard.bottomSheet


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
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.data.database.models.tasks.LocalTaskDetail
import com.zstronics.ceibro.databinding.FragmentDraftTasksBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UnSyncTaskBottomSheet(private val draftTasks: ArrayList<LocalTaskDetail>) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentDraftTasksBinding

    @Inject
    lateinit var adapter: UnSyncTasksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_draft_tasks,
            container,
            false
        )
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.taskRV.adapter = adapter
        draftTasks.let {
            if (draftTasks.isNotEmpty()) {
                adapter.setList(draftTasks)
            }
        }



        binding.closeBtn.setOnClick {
            dismiss()
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = true
            //    dialog.behavior.state = STATE_COLLAPSED
        }
        return dialog

    }
}