package com.zstronics.ceibro.ui.tasks.subtask

import android.os.Build
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.subtask.AssignedTo
import com.zstronics.ceibro.data.database.models.subtask.SubTaskStateItem
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.local.TaskLocalDataSource
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutSubtaskBoxBinding
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus.Companion.stateToHeadingAndBg
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
    var simpleChildItemClickListener: ((view: View, position: Int, data: AllSubtask) -> Unit)? = null
    var deleteChildItemClickListener: ((view: View, position: Int, data: AllSubtask) -> Unit)? = null

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
                    ?: SubTaskStatus.DRAFT.name

                val headingAndBg: Pair<Int, SubTaskStatus> = state.stateToHeadingAndBg()
                val (background, heading) = headingAndBg
                subTaskStatusName.setBackgroundResource(background)
                subTaskStatusName.text = heading.name.toCamelCase()


                val isAssignee = isAssignToMember(user?.id, item.assignedTo)
                val isTaskAdmin = isTaskAdmin(user?.id, item.taskData?.admins)
                val isSubTaskCreator = isSubTaskCreator(user?.id, item.creator)

//                if (isTaskAdmin || isSubTaskCreator) {
//                    if (state.uppercase() == SubTaskStatus.DONE.name || state.uppercase() == SubTaskStatus.REJECTED.name) {
//                        subTaskMoreMenuBtn.visibility = View.GONE
//                    }
//                    else {
//                        subTaskMoreMenuBtn.visibility = View.VISIBLE
//                    }
//                }
//                else {
//                    subTaskMoreMenuBtn.visibility = View.GONE
//                }

                if (isTaskAdmin && !isAssignee) {
                    when (state) {
                        SubTaskStatus.DRAFT.name -> {
                            draftStateBtnLayout.visibility = View.VISIBLE
                            assignedStateBtnLayout.visibility = View.GONE
                            acceptedStateBtnLayout.visibility = View.GONE
                            ongoingStateBtnLayout.visibility = View.GONE
                        }
                        SubTaskStatus.ASSIGNED.name -> {
                            draftStateBtnLayout.visibility = View.GONE
                            assignedStateBtnLayout.visibility = View.GONE
                            acceptedStateBtnLayout.visibility = View.GONE
                            ongoingStateBtnLayout.visibility = View.GONE
                        }
                        SubTaskStatus.ACCEPTED.name -> {
                            draftStateBtnLayout.visibility = View.GONE
                            assignedStateBtnLayout.visibility = View.GONE
                            acceptedStateBtnLayout.visibility = View.GONE
                            ongoingStateBtnLayout.visibility = View.GONE
                        }
                        SubTaskStatus.ONGOING.name -> {
                            draftStateBtnLayout.visibility = View.GONE
                            assignedStateBtnLayout.visibility = View.GONE
                            acceptedStateBtnLayout.visibility = View.GONE
                            ongoingStateBtnLayout.visibility = View.GONE
                        }
                        SubTaskStatus.DONE.name -> {
                            draftStateBtnLayout.visibility = View.GONE
                            assignedStateBtnLayout.visibility = View.GONE
                            acceptedStateBtnLayout.visibility = View.GONE
                            ongoingStateBtnLayout.visibility = View.GONE
                        }
                        SubTaskStatus.REJECTED.name -> {
                            draftStateBtnLayout.visibility = View.GONE
                            assignedStateBtnLayout.visibility = View.GONE
                            acceptedStateBtnLayout.visibility = View.GONE
                            ongoingStateBtnLayout.visibility = View.GONE
                        }
                    }
                } else if (isTaskAdmin && isAssignee) {
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
                } else if (!isTaskAdmin && isAssignee) {
                    when (state) {
                        SubTaskStatus.DRAFT.name -> {
                            draftStateBtnLayout.visibility = View.GONE
                            assignedStateBtnLayout.visibility = View.GONE
                            acceptedStateBtnLayout.visibility = View.GONE
                            ongoingStateBtnLayout.visibility = View.GONE
                        }
                        SubTaskStatus.ASSIGNED.name -> {
                            draftStateBtnLayout.visibility = View.GONE
                            assignedStateBtnLayout.visibility = View.VISIBLE
                            acceptedStateBtnLayout.visibility = View.GONE
                            ongoingStateBtnLayout.visibility = View.GONE
                        }
                        SubTaskStatus.ACCEPTED.name -> {
                            draftStateBtnLayout.visibility = View.GONE
                            assignedStateBtnLayout.visibility = View.GONE
                            acceptedStateBtnLayout.visibility = View.VISIBLE
                            ongoingStateBtnLayout.visibility = View.GONE
                        }
                        SubTaskStatus.ONGOING.name -> {
                            draftStateBtnLayout.visibility = View.GONE
                            assignedStateBtnLayout.visibility = View.GONE
                            acceptedStateBtnLayout.visibility = View.GONE
                            ongoingStateBtnLayout.visibility = View.VISIBLE
                        }
                        SubTaskStatus.DONE.name -> {
                            draftStateBtnLayout.visibility = View.GONE
                            assignedStateBtnLayout.visibility = View.GONE
                            acceptedStateBtnLayout.visibility = View.GONE
                            ongoingStateBtnLayout.visibility = View.GONE
                        }
                        SubTaskStatus.REJECTED.name -> {
                            draftStateBtnLayout.visibility = View.GONE
                            assignedStateBtnLayout.visibility = View.GONE
                            acceptedStateBtnLayout.visibility = View.GONE
                            ongoingStateBtnLayout.visibility = View.GONE
                        }
                    }
                } else {
                    draftStateBtnLayout.visibility = View.GONE
                    assignedStateBtnLayout.visibility = View.GONE
                    acceptedStateBtnLayout.visibility = View.GONE
                    ongoingStateBtnLayout.visibility = View.GONE
                }


                subTaskDueDate.text = DateUtils.reformatStringDate(
                    date = item.dueDate,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                )
                if (subTaskDueDate.text == "") {                              // Checking if date format was not dd-MM-yyyy then still it is empty
                    subTaskDueDate.text = DateUtils.reformatStringDate(
                        date = item.dueDate,
                        DateUtils.FORMAT_YEAR_MON_DATE,
                        DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                    )                                                         // Checking if date format was not yyyy-MM-dd then it will be empty
                    if (subTaskDueDate.text == "") {
                        subTaskDueDate.text = context.getString(R.string.invalid_due_date_text)
                    }
                }

                subTaskCreatorName.text = "${item.creator.firstName} ${item.creator.surName}"

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

                if (item.rejectedBy?.isNotEmpty() == true) {
                    subTaskRejectedByCount.text = item.rejectedBy.size.toString()
                }
                else {
                    subTaskRejectedByCount.text = "0"
                }



                subTaskTitle.text = item.title
                if (item.description?.isNotEmpty() == true) {
                    subTaskDesc.text = item.description
                } else {
                    subTaskDesc.text =
                        context.getString(R.string.no_description_added_by_creator_text)
                }
                subTaskDesc.text = item.description


                itemView.setOnClickListener {
                    itemClickListener?.invoke(it, adapterPosition, item)
                }

                subTaskRejectedByLayout.setOnClickListener {
                    if (item.rejectedBy?.isNotEmpty() == true) {
                        var allUsers = ""
                        var count = 0
                        for (rejectedUser in item.rejectedBy) {
                            count++
                            if (count == 1) {
                                allUsers = "${rejectedUser.firstName} ${rejectedUser.surName}"
                            }
                            else {
                                allUsers += ", ${rejectedUser.firstName} ${rejectedUser.surName}"
                            }
                        }
                        subTaskRejectedByLayout.tooltipText = allUsers
                    } else {
                        subTaskRejectedByLayout.tooltipText = "No rejections found"
                    }
                    subTaskRejectedByLayout.performLongClick()

                    val handler = Handler()
                    handler.postDelayed(Runnable {
                        subTaskRejectedByLayout.tooltipText = null

                    }, 1700)
                }

                subTaskMoreMenuBtn.setOnClickListener {
                    simpleChildItemClickListener?.invoke(it, absoluteAdapterPosition, item)
                }

                assignedStateRejectBtn.setOnClickListener {
                    childItemClickListener?.invoke(it, absoluteAdapterPosition, item) { result ->
                        onApiResult(result, absoluteAdapterPosition, SubTaskStatus.REJECTED)
                    }
                }
                acceptedStateRejectBtn.setOnClickListener {
                    childItemClickListener?.invoke(it, absoluteAdapterPosition, item) { result ->
                        onApiResult(result, absoluteAdapterPosition, SubTaskStatus.REJECTED)
                    }
                }
                acceptedStateStartBtn.setOnClickListener {
                    childItemClickListener?.invoke(it, absoluteAdapterPosition, item) { result ->
                        onApiResult(result, absoluteAdapterPosition, SubTaskStatus.ONGOING)
                    }
                }
                ongoingStateDoneBtn.setOnClickListener {
                    childItemClickListener?.invoke(it, absoluteAdapterPosition, item) { result ->
                        onApiResult(result, absoluteAdapterPosition, SubTaskStatus.DONE)
                    }
                }
                draftStateAssignBtn.setOnClickListener {
                    childItemClickListener?.invoke(it, absoluteAdapterPosition, item) { result ->
                        onApiResult(result, absoluteAdapterPosition, SubTaskStatus.ASSIGNED)
                    }
                }
                draftStateDeleteBtn.setOnClickListener {
                    deleteChildItemClickListener?.invoke(it, absoluteAdapterPosition, item)
                }
                assignedStateAcceptBtn.setOnClickListener {
                    childItemClickListener?.invoke(it, absoluteAdapterPosition, item) { result ->
                        onApiResult(result, absoluteAdapterPosition, SubTaskStatus.ACCEPTED)
                    }
                }
            }
        }
    }

    private fun onApiResult(
        result: Triple<Boolean, Boolean, Boolean>,
        adapterPos: Int,
        subtaskStatus: SubTaskStatus
    ) {
        val (apiCallSuccess, taskDeleted, subTaskDeleted) = result
        if (apiCallSuccess) { // we will assume that API call successfully completed
            if (subTaskDeleted) {
                removeItem(adapterPos)
            } else {
                updateItemStatus(adapterPos, subtaskStatus)
            }
        }
    }

    private fun updateItemStatus(adapterPos: Int, subtaskStatus: SubTaskStatus) {
        val allStates: ArrayList<SubTaskStateItem> =
            this.list[adapterPos].state as ArrayList<SubTaskStateItem>
        val user = user ?: return
        val userState = allStates.find { it.userId == user.id } ?: return
        val positionOfState = allStates.indexOf(userState)
        userState.userState = subtaskStatus.name.lowercase()
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

    fun isSubTaskCreator(userId: String?, creator: TaskMember?): Boolean {
        var isCreator = false
        if (creator?.id.equals(userId)) {
            isCreator = true
        }
        return isCreator
    }
}