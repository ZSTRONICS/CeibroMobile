package com.zstronics.ceibro.ui.dashboard.BottomSheet

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
                    "${item.assignee[0]}, +1"
                } else (if (item.assignee.size == 1) {
                    item.assignee[0]
                } else {
                    "Me"
                }).toString()
            }

        }
    }
}