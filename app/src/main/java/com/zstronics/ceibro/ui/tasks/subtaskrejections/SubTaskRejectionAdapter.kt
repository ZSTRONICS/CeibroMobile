package com.zstronics.ceibro.ui.tasks.subtaskrejections

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
import com.zstronics.ceibro.data.repos.task.models.SubTaskRejections
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutRejectionBoxBinding
import com.zstronics.ceibro.databinding.LayoutSubtaskBoxBinding
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus.Companion.stateToHeadingAndBg
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class SubTaskRejectionAdapter @Inject constructor(
    val sessionManager: SessionManager
) :
    RecyclerView.Adapter<SubTaskRejectionAdapter.SubTaskRejectionViewHolder>() {
    val user = sessionManager.getUser().value
    var itemClickListener: ((view: View, position: Int, data: AllSubtask) -> Unit)? = null

    private var list: MutableList<SubTaskRejections> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubTaskRejectionViewHolder {
        return SubTaskRejectionViewHolder(
            LayoutRejectionBoxBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SubTaskRejectionViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<SubTaskRejections>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class SubTaskRejectionViewHolder(private val binding: LayoutRejectionBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SubTaskRejections) {
            val context = binding.root.context
            with(binding) {

                rejectionUserName.text =
                    if (item.sender?.firstName == null)
                        "Unknown User"
                    else
                        "${item.sender.firstName} ${item.sender.surName}"


                rejectionDate.text = DateUtils.reformatStringDate(
                    date = item.createdAt,
                    DateUtils.SERVER_DATE_FULL_FORMAT,
                    DateUtils.FXRATE_DATE_TIME_FORMAT
                )

                rejectionCommentDetail.text = item.message

            }
        }
    }
}