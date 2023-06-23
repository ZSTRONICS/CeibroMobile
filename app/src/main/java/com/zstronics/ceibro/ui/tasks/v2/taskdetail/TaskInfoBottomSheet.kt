package com.zstronics.ceibro.ui.tasks.v2.taskdetail

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.databinding.FragmentTaskInfoBinding

class TaskInfoBottomSheet(_taskDetail: CeibroTaskV2?) : BottomSheetDialogFragment() {
    lateinit var binding: FragmentTaskInfoBinding
    var onChangePassword: ((oldPassword: String, newPassword: String) -> Unit)? = null
    var onChangePasswordDismiss: (() -> Unit)? = null
    val taskDetail = _taskDetail

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_task_info,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (taskDetail != null) {
            binding.taskCreatorName.text = "${taskDetail.creator.firstName} ${taskDetail.creator.surName}"

            if (taskDetail.assignedToState.isNotEmpty()) {
                val allAssignee = taskDetail.assignedToState.map { it }
                var assigneeMembers = ""

                var index = 0
                if (allAssignee.isNotEmpty()) {
                    for (item in allAssignee) {
                        assigneeMembers += if (index == allAssignee.size - 1) {
                            "${item.firstName} ${item.surName}"
                        } else {
                            "${item.firstName} ${item.surName}, "
                        }
                        index++
                    }
                }
                binding.taskAssigneeNames.text = assigneeMembers
            } else {
                binding.taskAssigneeNames.text = "No assignee members"
            }

            if (taskDetail.invitedNumbers.isNotEmpty()) {
                var invitedNumber = ""

                var index = 0
                for (item in taskDetail.invitedNumbers) {
                    invitedNumber += if (index == taskDetail.invitedNumbers.size - 1) {
                        item
                    } else {
                        "${item}, "
                    }
                    index++
                }

                binding.taskInvitedMembersNumbers.text = invitedNumber
            } else {
                binding.taskInvitedMembersLayout.visibility = View.GONE
            }


            binding.taskConfirmerName.text = "No confirmer added on this task"
            binding.taskViewerName.text = "No viewer added on this task"
            binding.taskConfirmerLayout.visibility = View.GONE
            binding.taskViewerLayout.visibility = View.GONE

            if (taskDetail.project != null && taskDetail.project.title.isNotEmpty()) {
                binding.taskProjectName.text = taskDetail.project.title
            } else {
                binding.taskProjectLayout.visibility = View.GONE
            }
        }


        binding.closeBtn.setOnClick {
            dismiss()
        }
    }


    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
//            dialog.behavior.skipCollapsed = true
            dialog.behavior.state = STATE_COLLAPSED
        }
        return dialog

    }
}