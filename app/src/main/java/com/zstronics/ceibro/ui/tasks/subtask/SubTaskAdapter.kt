package com.zstronics.ceibro.ui.tasks.subtask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.subtask.AssignedTo
import com.zstronics.ceibro.data.database.models.subtask.SubTaskStateItem
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.local.TaskLocalDataSource
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutSubtaskBoxBinding
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class SubTaskAdapter @Inject constructor(
    val sessionManager: SessionManager,
    private val localTask: TaskLocalDataSource
) :
    RecyclerView.Adapter<SubTaskAdapter.SubTaskViewHolder>() {
    val user = sessionManager.getUser().value
    var itemClickListener: ((view: View, position: Int, data: AllSubtask) -> Unit)? = null
    var itemLongClickListener: ((view: View, position: Int, data: AllSubtask) -> Unit)? =
        null
    var childItemClickListener: ((view: View, position: Int, data: AllSubtask, callBack: (result: Triple<Boolean, Boolean, Boolean>) -> Unit) -> Unit)? =
        null

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
                val state = item.state?.find { it.userId == user?.id }?.userState?.uppercase()
                    ?: TaskStatus.DRAFT.name

                val subTaskStatusNameBg: Pair<Int, String> = when (state) {
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
                        state
                    )
                }
                val (background, stringRes) = subTaskStatusNameBg
                subTaskStatusName.setBackgroundResource(background)
                subTaskStatusName.text = stringRes


                val isAssignee = isAssignToMember(user?.id, item.assignedTo)
                val isAdmin = isTaskAdmin(user?.id, item.taskData?.admins)

                if (isAdmin && !isAssignee) {
                    if (state == SubTaskStatus.DRAFT.name) {
                        draftStateBtnLayout.visibility = View.VISIBLE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    } else if (state == SubTaskStatus.ASSIGNED.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    } else if (state == SubTaskStatus.ACCEPTED.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    } else if (state == SubTaskStatus.ONGOING.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    } else if (state == SubTaskStatus.DONE.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    } else if (state == SubTaskStatus.REJECTED.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    }
                } else if (isAdmin && isAssignee) {
                    if (state == SubTaskStatus.DRAFT.name) {
                        draftStateBtnLayout.visibility = View.VISIBLE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    } else if (state == SubTaskStatus.ASSIGNED.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.VISIBLE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    } else if (state == SubTaskStatus.ACCEPTED.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.VISIBLE
                        ongoingStateBtnLayout.visibility = View.GONE
                    } else if (state == SubTaskStatus.ONGOING.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.VISIBLE
                    } else if (state == SubTaskStatus.DONE.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    } else if (state == SubTaskStatus.REJECTED.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    }
                } else if (!isAdmin && isAssignee) {
                    if (state == SubTaskStatus.DRAFT.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    } else if (state == SubTaskStatus.ASSIGNED.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.VISIBLE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    } else if (state == SubTaskStatus.ACCEPTED.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.VISIBLE
                        ongoingStateBtnLayout.visibility = View.GONE
                    } else if (state == SubTaskStatus.ONGOING.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.VISIBLE
                    } else if (state == SubTaskStatus.DONE.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    } else if (state == SubTaskStatus.REJECTED.name) {
                        draftStateBtnLayout.visibility = View.GONE
                        assignedStateBtnLayout.visibility = View.GONE
                        acceptedStateBtnLayout.visibility = View.GONE
                        ongoingStateBtnLayout.visibility = View.GONE
                    }
                } else {
                    draftStateBtnLayout.visibility = View.GONE
                    assignedStateBtnLayout.visibility = View.GONE
                    acceptedStateBtnLayout.visibility = View.GONE
                    ongoingStateBtnLayout.visibility = View.GONE
                }

                subTaskCDate.text = DateUtils.reformatStringDate(
                    date = item.createdAt,
                    DateUtils.SERVER_DATE_FULL_FORMAT,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                )
                subTaskCreatorName.text = "${item.creator?.firstName} ${item.creator?.surName}"

                val members: ArrayList<TaskMember> = ArrayList()
                for (assign in item.assignedTo) {
                    for (member in assign.members) {
                        members.add(member)
                    }
                }
                if (members.isNotEmpty()) {
                    subTaskAssignToName.text = if (members.size > 1)
                        "${members[0].firstName} ${members[0].surName}  +${members.size - 1}"
                    else
                        "${members[0].firstName} ${members[0].surName}"
                } else {
                    subTaskAssignToName.text = context.getString(R.string.no_user_assigned_text)
                }


                subTaskTitle.text = item.title
                if (item.description?.isNotEmpty() == true) {
                    subTaskDesc.text = item.description
                }
                else {
                    subTaskDesc.text = context.getString(R.string.no_description_added_by_creator_text)
                }
                subTaskDesc.text = item.description


                itemView.setOnClickListener {
                    itemClickListener?.invoke(it, adapterPosition, item)
                }

                assignedStateRejectBtn.setOnClickListener {
                    childItemClickListener?.invoke(it, absoluteAdapterPosition, item) { result ->
                        onApiResult(result, absoluteAdapterPosition)
                    }
                }
                acceptedStateRejectBtn.setOnClickListener {
                    childItemClickListener?.invoke(it, absoluteAdapterPosition, item) { result ->
                        onApiResult(result, absoluteAdapterPosition)
                    }
                }
                draftStateAssignBtn.setOnClickListener {
                    childItemClickListener?.invoke(it, absoluteAdapterPosition, item) { result ->
                        onApiResult(result, absoluteAdapterPosition)
                    }
                }
                assignedStateAcceptBtn.setOnClickListener {
                    childItemClickListener?.invoke(it, absoluteAdapterPosition, item) { result ->
                        onApiResult(result, absoluteAdapterPosition)
                    }
                }
            }
        }
    }

    private fun onApiResult(result: Triple<Boolean, Boolean, Boolean>, adapterPos: Int) {
        val (apiCallSuccess, taskDeleted, subTaskDeleted) = result
        if (apiCallSuccess) { // we will assume that API call successfully completed
            if (subTaskDeleted) {
                removeItem(adapterPos)
            } else {
                updateItemStatus(adapterPos, SubTaskStatus.REJECTED)
            }
        }
    }

    private fun updateItemStatus(adapterPos: Int, rejected: SubTaskStatus) {
        val allStates: ArrayList<SubTaskStateItem> =
            this.list[adapterPos].state as ArrayList<SubTaskStateItem>
        val user = user ?: return
        val userState = allStates.find { it.userId == user.id } ?: return
        val positionOfState = allStates.indexOf(userState)
        userState.userState = rejected.name.lowercase()
        allStates.removeAt(positionOfState)
        allStates.add(positionOfState, userState)
        this.list[adapterPos].state = allStates
        notifyItemChanged(adapterPos)
    }

    private fun removeItem(adapterPos: Int) {
        this.list.removeAt(adapterPos)
        notifyItemChanged(adapterPos)
    }

    fun isAssignToMember(userId: String?, assignedTo: List<AssignedTo>): Boolean {
        var isAssignee = false
        val members: ArrayList<TaskMember> = ArrayList()
        for (assign in assignedTo) {
            for (member in assign.members) {
                members.add(member)
            }
        }
        val assignMember: TaskMember? = members.find { it.id == userId }

        if (assignMember?.id.equals(userId)) {
            isAssignee = true
        }

        return isAssignee
    }

    fun isTaskAdmin(userId: String?, admins: List<String>?): Boolean {
        var isAdmin = false
        val id: String? = admins?.find { it == userId }
        if (id.equals(userId)) {
            isAdmin = true
        }

        return isAdmin
    }
}