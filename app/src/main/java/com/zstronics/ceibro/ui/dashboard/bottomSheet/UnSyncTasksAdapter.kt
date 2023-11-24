package com.zstronics.ceibro.ui.dashboard.bottomSheet

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.LocalTaskDetail
import com.zstronics.ceibro.databinding.UnsyncTasksItemBinding
import javax.inject.Inject

class UnSyncTasksAdapter @Inject constructor() :
    RecyclerView.Adapter<UnSyncTasksAdapter.UnSyncTaskViewHolder>() {

    private var listItems: MutableList<LocalTaskDetail>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnSyncTaskViewHolder {
        return UnSyncTaskViewHolder(
            UnsyncTasksItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: UnSyncTaskViewHolder, position: Int) {
        listItems?.get(position)?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return listItems?.size ?: 0
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: ArrayList<LocalTaskDetail>) {
        this.listItems = list.toMutableList()
        notifyDataSetChanged()
    }

    inner class UnSyncTaskViewHolder(private val binding: UnsyncTasksItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LocalTaskDetail) {
            val context = binding.taskDueDate.context

            binding.taskDueDate.text = if (item.dueDate?.isEmpty() == true) {
                "N/A"
            } else {
                item.dueDate
            }
            binding.taskTitle.text = item.topicName
            binding.files.text = if (item.noOfFiles > 0) {
                "Files: ${item.noOfFiles}"
            } else {
                ""
            }

            item.assignee?.let {
                binding.taskToText.text = if (item.assignee.size > 1) {
                    "${item.assignee[0]}, +${item.assignee.size - 1}"
                } else (if (item.assignee.size == 1) {
                    item.assignee[0]
                } else {
                    "Me"
                }).toString()
            }


            if (item.isDraftTaskCreationFailed) {
                binding.taskCardParentLayout.background =
                    context.resources.getDrawable(R.drawable.task_card_cancel_outline)
                binding.taskFailedText.visibility = View.VISIBLE
                val greyColor = ContextCompat.getColor(context, R.color.appPaleRed)
                ViewCompat.setBackgroundTintList(
                    binding.taskFailedText,
                    ColorStateList.valueOf(greyColor)
                )

                if (item.taskCreationFailedError.isNotEmpty()) {
                    binding.taskFailedErrorMessage.text = item.taskCreationFailedError
                    binding.taskFailedErrorMessage.visibility = View.VISIBLE
                } else {
                    binding.taskFailedErrorMessage.text = ""
                    binding.taskFailedErrorMessage.visibility = View.GONE
                }

            } else {        //else case will run when the cancelled task is shown for assignee, not a creator
                binding.taskCardParentLayout.background = null
                binding.taskFailedText.visibility = View.GONE
                val greyColor = ContextCompat.getColor(context, R.color.appGrey2)
                ViewCompat.setBackgroundTintList(
                    binding.taskFailedText,
                    ColorStateList.valueOf(greyColor)
                )
                binding.taskFailedErrorMessage.text = ""
                binding.taskFailedErrorMessage.visibility = View.GONE

            }

        }
    }
}