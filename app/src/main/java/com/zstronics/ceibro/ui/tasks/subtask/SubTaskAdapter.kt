package com.zstronics.ceibro.ui.tasks.subtask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.databinding.LayoutSubtaskBoxBinding
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class SubTaskAdapter @Inject constructor() :
    RecyclerView.Adapter<SubTaskAdapter.SubTaskViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: AllSubtask) -> Unit)? = null
    var itemLongClickListener: ((view: View, position: Int, data: AllSubtask) -> Unit)? =
        null
    var childItemClickListener: ((view: View, position: Int, data: AllSubtask) -> Unit)? = null

    private var list: MutableList<AllSubtask> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubTaskViewHolder {
        return SubTaskViewHolder(
            LayoutSubtaskBoxBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SubTaskViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<AllSubtask>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class SubTaskViewHolder(private val binding: LayoutSubtaskBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AllSubtask) {
            val context = binding.root.context
            with(binding) {
                /// Setting Status background and the status string.
                val subTaskStatusNameBg: Pair<Int, String> = when (item.subTaskState.uppercase()) {
                    SubTaskStatus.ONGOING.name -> Pair(
                        R.drawable.status_ongoing_filled,
                        context.getString(R.string.ongoing_heading)
                    )
                    SubTaskStatus.ASSIGNED.name -> Pair(
                        R.drawable.status_assigned_filled,
                        context.getString(R.string.assigned_heading)
                    )
                    SubTaskStatus.ACCEPTED.name -> Pair(
                        R.drawable.status_accepted_filled,
                        context.getString(R.string.accepted_heading)
                    )
                    SubTaskStatus.REJECTED.name -> Pair(
                        R.drawable.status_reject_filled,
                        context.getString(R.string.rejected_heading)
                    )
                    SubTaskStatus.DONE.name -> Pair(
                        R.drawable.status_done_filled,
                        context.getString(R.string.done_heading)
                    )
                    SubTaskStatus.DRAFT.name -> Pair(
                        R.drawable.status_draft_filled,
                        context.getString(R.string.draft_heading)
                    )
                    else -> Pair(
                        R.drawable.status_draft_filled,
                        item.subTaskState
                    )
                }
                val (background, stringRes) = subTaskStatusNameBg
                subTaskStatusName.setBackgroundResource(background)
                subTaskStatusName.text = stringRes


                subTaskCDate.text = DateUtils.reformatStringDate(
                    date = item.createdAt,
                    DateUtils.SERVER_DATE_FULL_FORMAT,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                )

//                if (item.assignedTo[0].members.isNotEmpty()) {
//                    taskAssignToName.text = if (item.assignedTo.size > 1)
//                        "${item.assignedTo[0].firstName} ${item.assignedTo[0].surName} + ${item.assignedTo.size - 1}"
//                    else
//                        "${item.assignedTo[0].firstName} ${item.assignedTo[0].surName}"
//                }
//                else {
//                    taskAssignToName.text = "No user assigned"
//                }


                subTaskTitle.text = item.title
                subTaskDesc.text = item.description


//                itemView.setOnClickListener {
//                    itemClickListener?.invoke(it, adapterPosition, item)
//                }

            }
        }
    }
}