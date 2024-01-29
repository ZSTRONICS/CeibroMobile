package com.zstronics.ceibro.ui.inbox

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
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentTaskInfoBinding
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils

class InboxSortingBottomSheet(_rootState: String, _selectedState: String, _userId: String, _taskDetail: CeibroTaskV2?) : BottomSheetDialogFragment() {
    lateinit var binding: FragmentTaskInfoBinding
    var onChangePassword: ((oldPassword: String, newPassword: String) -> Unit)? = null
    var onChangePasswordDismiss: (() -> Unit)? = null
    val rootState = _rootState
    val selectedState = _selectedState
    val userId = _userId
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
            var state = ""
            state = if (rootState == TaskRootStateTags.FromMe.tagValue && userId == taskDetail.creator.id) {
                taskDetail.creatorState
            } else if (rootState == TaskRootStateTags.Hidden.tagValue && selectedState.equals(TaskStatus.CANCELED.name, true)) {
                taskDetail.creatorState
            } else {
                taskDetail.assignedToState.find { it.userId == userId }?.state ?: ""
            }
            val taskStatusNameBg: Pair<Int, String> = when (state.uppercase()) {
                TaskStatus.NEW.name -> Pair(
                    R.drawable.status_new_filled_more_corners,
                    requireContext().getString(R.string.new_heading)
                )

                TaskStatus.UNREAD.name -> Pair(
                    R.drawable.status_new_filled_more_corners,
                    requireContext().getString(R.string.unread_heading)
                )

                TaskStatus.ONGOING.name -> Pair(
                    R.drawable.status_ongoing_filled_more_corners,
                    requireContext().getString(R.string.ongoing_heading)
                )

                TaskStatus.DONE.name -> Pair(
                    R.drawable.status_done_filled_more_corners,
                    requireContext().getString(R.string.done_heading)
                )

                TaskStatus.CANCELED.name -> Pair(
                    R.drawable.status_cancelled_filled_more_corners,
                    requireContext().getString(R.string.canceled)
                )

                else -> Pair(
                    R.drawable.status_draft_outline,
                    state.ifEmpty {
                        "N/A"
                    }
                )
            }
            val (background, status) = taskStatusNameBg
            binding.taskDetailStatusName.setBackgroundResource(background)
            binding.taskDetailStatusName.text = status

            binding.taskDetailCreationDate.text = DateUtils.formatCreationUTCTimeToCustom(
                utcTime = taskDetail.createdAt,
                inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
            )

            var dueDate = ""
            dueDate = DateUtils.reformatStringDate(
                date = taskDetail.dueDate,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
            )
            if (dueDate == "") {                              // Checking if date format was not dd-MM-yyyy then it will be empty
                dueDate = DateUtils.reformatStringDate(
                    date = taskDetail.dueDate,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
                )
                if (dueDate == "") {
                    dueDate = "N/A"
                }
            }
            binding.taskDetailDueDate.text = "Due Date: $dueDate"


            binding.taskCreatorName.text = "${taskDetail.creator.firstName} ${taskDetail.creator.surName}"

            if (taskDetail.assignedToState.isNotEmpty()) {
                val allAssignee = taskDetail.assignedToState.map { it }
                var assigneeMembers = ""

                var index = 0
                if (allAssignee.isNotEmpty()) {
                    for (item in allAssignee) {
                        assigneeMembers += if (index == allAssignee.size - 1) {
                            if (item.firstName.isEmpty()) {
                                item.phoneNumber
                            } else {
                                "${item.firstName} ${item.surName}"
                            }
                        } else {
                            if (item.firstName.isEmpty()) {
                                "${item.phoneNumber}, "
                            } else {
                                "${item.firstName} ${item.surName}, "
                            }
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
                        if (item.firstName.isNotEmpty()) {
                            "${item.firstName} ${item.surName}"
                        } else {
                            item.phoneNumber
                        }
                    } else {
                        if (item.firstName.isNotEmpty()) {
                            "${item.firstName} ${item.surName}, "
                        } else {
                            "${item.phoneNumber}, "
                        }
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