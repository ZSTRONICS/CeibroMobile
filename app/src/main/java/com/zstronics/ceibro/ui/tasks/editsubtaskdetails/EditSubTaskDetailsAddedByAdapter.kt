package com.zstronics.ceibro.ui.tasks.editsubtaskdetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.subtask.AssignedTo
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.task.models.SubtaskStatusData
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutItemEditMembersAddedByBinding
import javax.inject.Inject

class EditSubTaskDetailsAddedByAdapter @Inject constructor(
    val sessionManager: SessionManager
) :
    RecyclerView.Adapter<EditSubTaskDetailsAddedByAdapter.SubtaskMemberAddedByViewHolder>() {
    val user = sessionManager.getUser().value

    var deleteItemClickListener: ((view: View, position: Int, taskId: String, subTaskId: String, memberId: String) -> Unit)? = null
    var doneItemClickListener: ((view: View, position: Int, taskId: String, subTaskId: String, memberId: String) -> Unit)? = null


    private var list: MutableList<AssignedTo> = mutableListOf()
    private var subTaskObj: MutableList<AllSubtask> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskMemberAddedByViewHolder {
        return SubtaskMemberAddedByViewHolder(
            LayoutItemEditMembersAddedByBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SubtaskMemberAddedByViewHolder, position: Int) {
        holder.bind(list[position], subTaskObj[0])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<AssignedTo>, subtask: AllSubtask?) {
        this.list.clear()
        this.subTaskObj.clear()
        this.list.addAll(list)
        if (subtask != null) {
            this.subTaskObj.add(subtask)
        }
        notifyDataSetChanged()
    }

    inner class SubtaskMemberAddedByViewHolder(private val binding: LayoutItemEditMembersAddedByBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AssignedTo, subtask: AllSubtask) {
            val context = binding.root.context
            with(binding) {

                if (item.addedBy.id == user?.id) {
                    addedByName.text = "Added by me"
                }
                else {
                    addedByName.text = "Added by " + item.addedBy.firstName + " " + item.addedBy.surName
                }

                val memberAdapter = EditSubTaskDetailsMemberAdapter(sessionManager, item.members, subtask, item.addedBy)
                addedByMembersRV.adapter = memberAdapter

                memberAdapter.deleteItemClickListener = { childView: View, position: Int, taskId: String, subTaskId: String, memberId: String ->
                    deleteItemClickListener?.invoke(childView, adapterPosition, taskId, subTaskId, memberId)
                }
                memberAdapter.doneItemClickListener = { childView: View, position: Int, taskId: String, subTaskId: String, memberId: String ->
                    doneItemClickListener?.invoke(childView, adapterPosition, taskId, subTaskId, memberId)
                }

            }
        }
    }

}