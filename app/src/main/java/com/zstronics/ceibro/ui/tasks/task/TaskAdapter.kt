package com.zstronics.ceibro.ui.tasks.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.databinding.LayoutTaskBoxBinding
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class TaskAdapter @Inject constructor() :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: CeibroTask) -> Unit)? = null
    var itemLongClickListener: ((view: View, position: Int, data: CeibroTask) -> Unit)? =
        null
    var childItemClickListener: ((view: View, position: Int, data: CeibroTask) -> Unit)? = null

    private var list: MutableList<CeibroTask> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            LayoutTaskBoxBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<CeibroTask>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(private val binding: LayoutTaskBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CeibroTask) {
            val context = binding.root.context
            with(binding) {
                /// Setting Status background and the status string.
                val taskStatusNameBg: Pair<Int, String> = when (item.state.uppercase()) {
                    TaskStatus.NEW.name -> Pair(
                        R.drawable.status_assigned_outline,
                        context.getString(R.string.new_heading)
                    )
                    TaskStatus.ACTIVE.name -> Pair(
                        R.drawable.status_ongoing_outline,
                        context.getString(R.string.active_heading)
                    )
                    TaskStatus.DRAFT.name -> Pair(
                        R.drawable.status_draft_outline,
                        context.getString(R.string.draft_heading)
                    )
                    TaskStatus.DONE.name -> Pair(
                        R.drawable.status_done_outline,
                        context.getString(R.string.done_heading)
                    )
                    else -> Pair(
                        R.drawable.status_draft_outline,
                        item.state
                    )
                }
                val (background, stringRes) = taskStatusNameBg
                taskCardLayout.setBackgroundResource(background)
                taskStatusName.setBackgroundResource(background)
                taskStatusName.text = stringRes

                /// setting started date.

                taskCreationDateText.text = DateUtils.reformatStringDate(
                    date = item.createdAt,
                    DateUtils.SERVER_DATE_FULL_FORMAT,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                )

                taskCreatorName.text = "${item.creator?.firstName} ${item.creator?.surName}"

                if (item.assignedTo.isNotEmpty()) {
                    taskAssignToName.text = if (item.assignedTo.size > 1)
                        "${item.assignedTo[0].firstName} ${item.assignedTo[0].surName}  +${item.assignedTo.size - 1}"
                    else
                        "${item.assignedTo[0].firstName} ${item.assignedTo[0].surName}"
                }
                else {
                    taskAssignToName.text = "No user assigned"
                }

                taskDueDateText.text = DateUtils.reformatStringDate(
                    date = item.dueDate,
                    DateUtils.FORMAT_YEAR_MON_DATE,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                )
                if (taskDueDateText.text == "") {                              // Checking if date format was not yyyy-MM-dd then it will be empty
                    taskDueDateText.text = DateUtils.reformatStringDate(
                        date = item.dueDate,
                        DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                        DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                    )
                    if (taskDueDateText.text == "") {                          // Checking if date format was not dd-MM-yyyy then still it is empty
                        taskDueDateText.text = "Invalid due date"
                    }
                }

                taskName.text = item.title
                taskCommentCountText.text = item.unSeenSubTaskCommentCount.toString()

                taskProjectName.text = item.project.title
                taskSubTasksRV.visibility =
                    if (item.totalSubTaskCount > 0)
                        View.VISIBLE
                    else
                        View.INVISIBLE
                taskSubTaskTotalCountText.text = "${item.totalSubTaskCount} subtask(s)"
                taskSubTaskCount.text = "${item.subTaskStatusCount?.done}/${item.totalSubTaskCount}"

                itemView.setOnClickListener {
                    itemClickListener?.invoke(it, adapterPosition, item)
                }

            }
        }
    }
}