package com.zstronics.ceibro.ui.locationv2


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.databinding.FragmentAddPhotoBinding
import com.zstronics.ceibro.databinding.LayoutLocationTaskCardBinding
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationTaskCardBottomSheet(val task: CeibroTaskV2, val callback: (String) -> Unit) :
    BottomSheetDialogFragment() {
    lateinit var binding: LayoutLocationTaskCardBinding



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.layout_location_task_card,
            container,
            false
        )
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = binding.taskId.context

        var state = ""
        state =
            if (task.isAssignedToMe) {
                task.userSubState
            } else if (task.isCreator || task.isTaskViewer || task.isTaskConfirmer) {
                task.creatorState
            } else {
                task.userSubState
            }
        val taskStatusNameBg: Pair<Int, String> = when (state.uppercase()) {
            TaskStatus.NEW.name -> Pair(
                R.drawable.status_new_filled_with_border,
                context.getString(R.string.new_heading)
            )

            TaskStatus.UNREAD.name -> Pair(
                R.drawable.status_new_filled_with_border,
                context.getString(R.string.unread_heading)
            )

            TaskStatus.ONGOING.name -> Pair(
                R.drawable.status_ongoing_filled_with_border,
                context.getString(R.string.ongoing_heading)
            )

            TaskRootStateTags.InReview.tagValue.uppercase() -> Pair(
                R.drawable.status_in_review_outline_more_corners,
                state.toCamelCase()
            )

            TaskRootStateTags.ToReview.tagValue.uppercase() -> Pair(
                R.drawable.status_in_review_outline_more_corners,
                state.toCamelCase()
            )

            TaskStatus.DONE.name -> Pair(
                R.drawable.status_done_filled_with_border,
                context.getString(R.string.done_heading)
            )

            TaskStatus.CANCELED.name -> Pair(
                R.drawable.status_cancelled_filled_with_border,
                context.getString(R.string.canceled)
            )

            TaskDetailEvents.REJECT_CLOSED.eventValue.uppercase() -> Pair(
                R.drawable.status_reject_filled_with_border,
                state.toCamelCase()
            )

            else -> Pair(
                R.drawable.status_draft_outline,
                state.ifEmpty {
                    "N/A"
                }
            )
        }
        val (background, status) = taskStatusNameBg
        binding.taskId.setBackgroundResource(background)
        binding.taskId.text = task.taskUID


        binding.taskCreationDate.text =
            DateUtils.formatCreationUTCTimeToCustom(
                utcTime = task.createdAt,
                inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
            )

        var dueDate = ""
        dueDate = DateUtils.reformatStringDate(
            date = task.dueDate,
            DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
            DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
        )
        if (dueDate == "") {                              // Checking if date format was not dd-MM-yyyy then it will be empty
            dueDate = DateUtils.reformatStringDate(
                date = task.dueDate,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
            )
            if (dueDate == "") {
                dueDate = "N/A"
            }
        }
        binding.taskDueDate.text = "Due Date: $dueDate"

        binding.taskTitle.text =
            if (task.title != null) {
                task.title.ifEmpty {
                    "N/A"
                }
            } else {
                "N/A"
            }

        if (task.description.trim().isEmpty()) {
            binding.taskDescription.visibility = View.GONE
        } else {
            binding.taskDescription.text = task.description
            binding.taskDescription.visibility = View.VISIBLE
        }


        binding.taskFromText.text = "${task.creator.firstName} ${task.creator.surName}"

        if (task.project != null) {
            binding.taskProjectLayout.visibility = View.VISIBLE
            binding.taskProjectText.text = task.project.title

            val layoutParams = binding.bottomCenterPoint.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.horizontalBias = 0.52f  // Set the desired bias value between 0.0 and 1.0
            binding.bottomCenterPoint.layoutParams = layoutParams
        } else {
            binding.taskProjectLayout.visibility = View.GONE

            val layoutParams = binding.bottomCenterPoint.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.horizontalBias = 0.85f  // Set the desired bias value between 0.0 and 1.0
            binding.bottomCenterPoint.layoutParams = layoutParams
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = true
        }
        return dialog

    }
}