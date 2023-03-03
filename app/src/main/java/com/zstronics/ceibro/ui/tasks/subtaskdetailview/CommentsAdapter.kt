package com.zstronics.ceibro.ui.tasks.subtaskdetailview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutCommentBoxBinding
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus.Companion.stateToHeadingAndBg
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class CommentsAdapter @Inject constructor(
    val sessionManager: SessionManager
) :
    RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {
    val user = sessionManager.getUser().value
    var itemClickListener: ((view: View, position: Int, data: SubTaskComments) -> Unit)? = null

    private var list: MutableList<SubTaskComments> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        return CommentsViewHolder(
            LayoutCommentBoxBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<SubTaskComments>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class CommentsViewHolder(private val binding: LayoutCommentBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SubTaskComments) {
            binding.comment = item
            val headingAndBg: Pair<Int, SubTaskStatus> = item.userState.stateToHeadingAndBg()
            val (background, heading) = headingAndBg
            binding.commentStatusName.setBackgroundResource(background)
            binding.commentStatusName.text = heading.name.toCamelCase()
            binding.commentsAttachment.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
        }
    }
}