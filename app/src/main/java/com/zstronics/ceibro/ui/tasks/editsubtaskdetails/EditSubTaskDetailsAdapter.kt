package com.zstronics.ceibro.ui.tasks.editsubtaskdetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.task.models.SubtaskStatusData
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutItemEditMembersBinding
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class EditSubTaskDetailsAdapter @Inject constructor(
    val sessionManager: SessionManager
) :
    RecyclerView.Adapter<EditSubTaskDetailsAdapter.SubtaskMemberStatusViewHolder>() {
    val user = sessionManager.getUser().value

    var itemClickListener: ((view: View, position: Int, data: SubtaskStatusData) -> Unit)? = null
    var itemLongClickListener: ((view: View, position: Int, data: SubtaskStatusData) -> Unit)? =
        null
    var childItemClickListener: ((view: View, position: Int, data: SubtaskStatusData) -> Unit)? = null
    var menuChildItemClickListener: ((view: View, position: Int, data: SubtaskStatusData) -> Unit)? = null

    private var list: MutableList<SubtaskStatusData> = mutableListOf()
    private var subTaskObj: MutableList<AllSubtask> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskMemberStatusViewHolder {
        return SubtaskMemberStatusViewHolder(
            LayoutItemEditMembersBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SubtaskMemberStatusViewHolder, position: Int) {
        holder.bind(list[position], subTaskObj[0])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<SubtaskStatusData>, subtask: AllSubtask?) {
        this.list.clear()
        this.subTaskObj.clear()
        this.list.addAll(list)
        if (subtask != null) {
            this.subTaskObj.add(subtask)
        }
        notifyDataSetChanged()
    }

    inner class SubtaskMemberStatusViewHolder(private val binding: LayoutItemEditMembersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SubtaskStatusData, subtask: AllSubtask) {
            val context = binding.root.context
            with(binding) {
                /// Setting Status background and the status string.

                println("Subtask in detail Adapter: $subtask")
                val subTaskStatusNameBg: Pair<Int, String> = when (val state = item.userState.uppercase()) {
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
                memberStateName.setBackgroundResource(background)
                memberStateName.text = stringRes

                memberImgText.text = ""
                if (item.user.profilePic == "" || item.user.profilePic.isNullOrEmpty()) {
                    memberImgText.text =
                        "${item.user.firstName.get(0)?.uppercaseChar()}${item.user.surName.get(0)?.uppercaseChar()}"
                    memberImgText.visibility = View.VISIBLE
                    memberImg.visibility = View.GONE
                } else {
                    Glide.with(memberImg.context)
                        .load(item.user.profilePic)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.profile_img)
                        .into(memberImg)
                    memberImg.visibility = View.VISIBLE
                    memberImgText.visibility = View.GONE
                }


                memberName.text = "${item.user.firstName} ${item.user.surName}"

                if (item.userState.uppercase() == SubTaskStatus.DRAFT.name || item.userState.uppercase() == SubTaskStatus.ASSIGNED.name ||
                    item.userState.uppercase() == SubTaskStatus.ACCEPTED.name) {
                    deleteMemberBtn.visibility = View.VISIBLE
                    markDoneMemberStateBtn.visibility = View.GONE
                    doneStateTick.visibility = View.GONE
                }
                else if (item.userState.uppercase() == SubTaskStatus.ONGOING.name) {
                    deleteMemberBtn.visibility = View.GONE
                    markDoneMemberStateBtn.visibility = View.VISIBLE
                    doneStateTick.visibility = View.GONE
                }
                else if (item.userState.uppercase() == SubTaskStatus.DONE.name || item.userState.uppercase() == SubTaskStatus.REJECTED.name) {
                    deleteMemberBtn.visibility = View.GONE
                    markDoneMemberStateBtn.visibility = View.GONE
                    doneStateTick.visibility = View.VISIBLE
                }
                else {
                    deleteMemberBtn.visibility = View.GONE
                    markDoneMemberStateBtn.visibility = View.GONE
                    doneStateTick.visibility = View.GONE
                }


//                val isAdmin = isTaskAdmin(user?.id, item.admins)
//                val isCreator = isTaskCreator(user?.id, item.creator)
//
//                if (isAdmin || isCreator) {
//                    taskMoreMenuBtn.visibility = View.VISIBLE
//                }
//                else {
//                    taskMoreMenuBtn.visibility = View.GONE
//                }
//
//
//
//                /// setting started date.
//                taskCreationDateText.text = DateUtils.reformatStringDate(
//                    date = item.createdAt,
//                    DateUtils.SERVER_DATE_FULL_FORMAT,
//                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
//                )
//
//                taskCreatorName.text = "${item.creator?.firstName} ${item.creator?.surName}"
//
//                if (item.assignedTo.isNotEmpty()) {
//                    taskAssignToName.text = if (item.assignedTo.size > 1)
//                        "${item.assignedTo[0].firstName} ${item.assignedTo[0].surName}  +${item.assignedTo.size - 1}"
//                    else
//                        "${item.assignedTo[0].firstName} ${item.assignedTo[0].surName}"
//                }
//                else {
//                    taskAssignToName.text = context.getString(R.string.no_user_assigned_text)
//                }


//
//                taskDueDateText.text = DateUtils.reformatStringDate(
//                    date = item.dueDate,
//                    DateUtils.FORMAT_YEAR_MON_DATE,
//                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
//                )
//                if (taskDueDateText.text == "") {                              // Checking if date format was not yyyy-MM-dd then it will be empty
//                    taskDueDateText.text = DateUtils.reformatStringDate(
//                        date = item.dueDate,
//                        DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
//                        DateUtils.FORMAT_SHORT_DATE_MON_YEAR
//                    )
//                    if (taskDueDateText.text == "") {                          // Checking if date format was not dd-MM-yyyy then still it is empty
//                        taskDueDateText.text = context.getString(R.string.invalid_due_date_text)
//                    }
//                }
//
//                taskName.text = item.title
//                taskCommentCountText.text = item.unSeenSubTaskCommentCount.toString()
//
//                taskProjectName.text = item.project.title
//                taskSubTasksRV.visibility = View.INVISIBLE
//
//                taskSubTaskTotalCountText.text = "${item.totalSubTaskCount} subtask(s)"
//
//
//
//                itemView.setOnClickListener {
//                    itemClickListener?.invoke(it, adapterPosition, item)
//                }
//
//                taskMoreMenuBtn.setOnClickListener {
//                    menuChildItemClickListener?.invoke(it, adapterPosition, item)
//                }
//
            }
        }
    }

    fun isTaskAdmin(userId: String?, admins: List<TaskMember>): Boolean {
        var isAdmin = false
        val member = admins.find { it.id == userId }
        if (member?.id.equals(userId)) {
            isAdmin = true
        }
        return isAdmin
    }

    fun isTaskCreator(userId: String?, creator: TaskMember?): Boolean {
        var isCreator = false
        if (creator?.id.equals(userId)) {
            isCreator = true
        }
        return isCreator
    }
}