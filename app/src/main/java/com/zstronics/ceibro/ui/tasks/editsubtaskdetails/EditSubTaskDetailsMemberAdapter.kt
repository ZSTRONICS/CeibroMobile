package com.zstronics.ceibro.ui.tasks.editsubtaskdetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.task.models.SubtaskStatusData
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutItemEditMembersBinding
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus.Companion.stateToHeadingAndBg
import javax.inject.Inject

class EditSubTaskDetailsMemberAdapter @Inject constructor(
    val sessionManager: SessionManager,
    memberList: List<TaskMember>,
    _subtask: AllSubtask?,
    _addedBy: TaskMember
) :
    RecyclerView.Adapter<EditSubTaskDetailsMemberAdapter.SubtaskMemberStatusViewHolder>() {
    val user = sessionManager.getUser().value

    var deleteItemClickListener: ((view: View, position: Int, taskId: String, subTaskId: String, memberId: String) -> Unit)? =
        null
    var doneItemClickListener: ((view: View, position: Int, taskId: String, subTaskId: String, memberId: String) -> Unit)? =
        null

    private var list: MutableList<TaskMember> = mutableListOf()
    private var subtask: AllSubtask?
    private var addedBy: TaskMember?

    init {
        this.subtask = _subtask
        this.addedBy = _addedBy
        setList(memberList)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubtaskMemberStatusViewHolder {
        return SubtaskMemberStatusViewHolder(
            LayoutItemEditMembersBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SubtaskMemberStatusViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<TaskMember>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class SubtaskMemberStatusViewHolder(private val binding: LayoutItemEditMembersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskMember) {
            val context = binding.root.context
            with(binding) {

                //jonsa member display hony lga hai us ki id find krein gy states ki array mn sy
                val userState =
                    subtask?.state?.find { it.userId == item.id }?.userState?.uppercase()
                        ?: "Unknown"

//                println("Subtask in detail Adapter: $subtask")
                val subTaskStatusNameBg: Pair<Int, SubTaskStatus> =
                    userState.stateToHeadingAndBg()
                val (background, heading) = subTaskStatusNameBg
                memberStateName.setBackgroundResource(background)
                memberStateName.text = heading.name.toCamelCase()


                memberImgText.text = ""
                if (item.profilePic == "" || item.profilePic.isNullOrEmpty()) {
                    memberImgText.text =
                        "${item.firstName.get(0).uppercaseChar()}${
                            item.surName.get(0).uppercaseChar()
                        }"
                    memberImgText.visibility = View.VISIBLE
                    memberImg.visibility = View.GONE
                } else {
                    Glide.with(memberImg.context)
                        .load(item.profilePic)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.profile_img)
                        .into(memberImg)
                    memberImg.visibility = View.VISIBLE
                    memberImgText.visibility = View.GONE
                }

                memberName.text = "${item.firstName} ${item.surName}"

                val isTaskAdmin = isTaskAdmin(user?.id, subtask?.taskData?.admins)
                val isCreator = isSubTaskCreator(user?.id, subtask?.creator)


                if (addedBy?.id == user?.id || isTaskAdmin || isCreator) {
                    if (userState.uppercase() == SubTaskStatus.DRAFT.name || userState.uppercase() == SubTaskStatus.ASSIGNED.name ||
                        userState.uppercase() == SubTaskStatus.ACCEPTED.name
                    ) {
                        if (item.id == user?.id) {
                            deleteMemberBtn.visibility = View.GONE
                        } else {
                            deleteMemberBtn.visibility = View.VISIBLE
                        }
                        markDoneMemberStateBtn.visibility = View.GONE
                        doneStateTick.visibility = View.GONE
                    } else if (userState.uppercase() == SubTaskStatus.ONGOING.name) {
                        if (addedBy?.id == user?.id && !isTaskAdmin && !isCreator) {
                            markDoneMemberStateBtn.visibility = View.GONE
                        } else {
                            markDoneMemberStateBtn.visibility = View.VISIBLE
                        }
                        deleteMemberBtn.visibility = View.GONE
                        doneStateTick.visibility = View.GONE
                    } else if (userState.uppercase() == SubTaskStatus.DONE.name || userState.uppercase() == SubTaskStatus.REJECTED.name) {
                        deleteMemberBtn.visibility = View.GONE
                        markDoneMemberStateBtn.visibility = View.GONE
                        doneStateTick.visibility = View.VISIBLE
                    } else {
                        deleteMemberBtn.visibility = View.GONE
                        markDoneMemberStateBtn.visibility = View.GONE
                        doneStateTick.visibility = View.GONE
                    }
                } else {
                    deleteMemberBtn.visibility = View.GONE
                    markDoneMemberStateBtn.visibility = View.GONE
                    doneStateTick.visibility = View.GONE
                }


                deleteMemberBtn.setOnClickListener {
                    deleteItemClickListener?.invoke(
                        it,
                        adapterPosition,
                        subtask?.taskId ?: "",
                        subtask?.id ?: "",
                        item.id
                    )
                }

                markDoneMemberStateBtn.setOnClickListener {
                    doneItemClickListener?.invoke(
                        it,
                        adapterPosition,
                        subtask?.taskId ?: "",
                        subtask?.id ?: "",
                        item.id
                    )
                }

//                taskMoreMenuBtn.setOnClickListener {
//                    menuChildItemClickListener?.invoke(it, adapterPosition, item)
//                }
//
            }
        }
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