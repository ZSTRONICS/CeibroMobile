package com.zstronics.ceibro.ui.projects.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutChatBoxBinding
import com.zstronics.ceibro.databinding.LayoutProjectBoxBinding
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class AllProjectsAdapter @Inject constructor(val sessionManager: SessionManager) :
    RecyclerView.Adapter<AllProjectsAdapter.AllProjectsViewHolder>() {
    val user = sessionManager.getUser().value
    var itemClickListener: ((view: View, position: Int, data: AllProjectsResponse.Result.Projects) -> Unit)? = null
    var itemLongClickListener: ((view: View, position: Int, data: AllProjectsResponse.Result.Projects) -> Unit)? =
        null
    var childItemClickListener: ((view: View, position: Int, data: AllProjectsResponse.Result.Projects) -> Unit)? = null

    private var list: MutableList<AllProjectsResponse.Result.Projects> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllProjectsViewHolder {
        return AllProjectsViewHolder(
            LayoutProjectBoxBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllProjectsViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<AllProjectsResponse.Result.Projects>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllProjectsViewHolder(private val binding: LayoutProjectBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AllProjectsResponse.Result.Projects) {
            val context = binding.root.context
            //val otherUser: Member? = item.members.find { member -> member.id != user?.id }
            binding.allProjects = item

            binding.projectOwnerNameText.text = item.owner.get(0).firstName + " " + item.owner.get(0).surName



            if (item.dueDate == null || item.dueDate == "") {
                binding.projectDueDateText.text = "No expiry"
                binding.projectCDate.text = "No expiry"
            }
            else {
                val date = item.dueDate
                binding.projectDueDateText.text = DateUtils.reformatStringDate(
                    item.dueDate, DateUtils.SERVER_DATE_FULL_FORMAT, DateUtils.FORMAT_YEAR_MON_DATE
                )
                binding.projectCDate.text = DateUtils.reformatStringDate(
                    item.dueDate, DateUtils.SERVER_DATE_FULL_FORMAT, DateUtils.FORMAT_YEAR_MON_DATE
                )
            }


            binding.projectStatusName.text = item.publishStatus.toCamelCase()
            if (item.publishStatus.toLowerCase() == "draft") {
                binding.projectCardLayout.setBackgroundResource(R.drawable.status_draft_outline)
                binding.projectStatusName.background = context.getDrawable(R.drawable.status_draft_filled)
            }
            else if (item.publishStatus.toLowerCase() == "ongoing") {
                binding.projectCardLayout.setBackgroundResource(R.drawable.status_ongoing_outline)
                binding.projectStatusName.background = context.getDrawable(R.drawable.status_ongoing_filled)
            }
            else if (item.publishStatus.toLowerCase() == "approved" || item.publishStatus.toLowerCase() == "approve") {
                binding.projectCardLayout.setBackgroundResource(R.drawable.status_approve_outline)
                binding.projectStatusName.background = context.getDrawable(R.drawable.status_approve_filled)
            }
            else if (item.publishStatus.toLowerCase() == "done" || item.publishStatus.toLowerCase() == "complete" || item.publishStatus.toLowerCase() == "completed") {
                binding.projectCardLayout.setBackgroundResource(R.drawable.status_done_outline)
                binding.projectStatusName.background = context.getDrawable(R.drawable.status_done_filled)
            }
            else if (item.publishStatus.toLowerCase() == "published" || item.publishStatus.toLowerCase() == "publish") {
                binding.projectCardLayout.setBackgroundResource(R.drawable.status_publish_outline)
                binding.projectStatusName.background = context.getDrawable(R.drawable.status_publish_filled)
            }
            else if (item.publishStatus.toLowerCase() == "submit" || item.publishStatus.toLowerCase() == "submitted") {
                binding.projectCardLayout.setBackgroundResource(R.drawable.status_submit_outline)
                binding.projectStatusName.background = context.getDrawable(R.drawable.status_submit_filled)
            }


//            itemView.setOnClickListener {
//                itemClickListener?.invoke(it, adapterPosition, item)
//            }
//
//            itemView.setOnLongClickListener {
//                itemLongClickListener?.invoke(it, adapterPosition, item)
//                true
//            }


        }
    }
}