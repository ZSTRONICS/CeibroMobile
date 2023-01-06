package com.zstronics.ceibro.ui.tasks.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.databinding.LayoutTaskBoxBinding
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class TaskAdapter @Inject constructor() :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: CeibroTask) -> Unit)? = null
    var itemLongClickListener: ((view: View, position: Int, data: CeibroTask) -> Unit)? =
        null
    var childItemClickListener: ((view: View, position: Int, data: ChatRoom) -> Unit)? = null

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
                val taskStatusNameBg: Pair<Int, Int> = when (item.state.uppercase()) {
                    TaskState.DRAFT.name -> Pair(
                        R.drawable.status_draft_outline,
                        R.string.draft_heading
                    )
                    TaskState.ACTIVE.name -> Pair(
                        R.drawable.status_ongoing_outline,
                        R.string.active_heading
                    )
                    TaskState.DONE.name -> Pair(
                        R.drawable.status_ongoing_outline,
                        R.string.done_heading
                    )
                    else -> Pair(
                        R.drawable.status_draft_outline,
                        R.string.draft_heading
                    )
                }
                val (background, stringRes) = taskStatusNameBg
                taskStatusName.setBackgroundResource(background)
                taskStatusName.text = context.getString(stringRes)

                /// setting started date.

                taskCDate.text = DateUtils.reformatStringDate(
                    date = item.createdAt,
                    DateUtils.SERVER_DATE_FULL_FORMAT,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                )
                if (item.assignedTo.isNotEmpty()) {
                    taskAssignToName.text = if (item.assignedTo.size > 1)
                        "${item.assignedTo[0].firstName} ${item.assignedTo[0].surName} + ${item.assignedTo.size - 1}"
                    else
                        "${item.assignedTo[0].firstName} ${item.assignedTo[0].surName}"
                }

                taskDueDateText.text = DateUtils.reformatStringDate(
                    date = item.dueDate,
                    DateUtils.SERVER_DATE_FULL_FORMAT,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                )

                taskName.text = item.title
                taskCommentCountText.text = item.unSeenSubTaskCommentCount.toString()

                taskProjectName.text = item.project.title
                taskSubTaskTotalCountLayout.visibility =
                    if (item.totalSubTaskCount > 0)
                        View.VISIBLE
                    else
                        View.INVISIBLE
                taskSubTaskTotalCountText.text = "${item.totalSubTaskCount} subtask(s)"
            }
        }
    }
}