package com.zstronics.ceibro.ui.tasks.v2.taskfromme.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.databinding.LayoutCeibroFilesBinding
import com.zstronics.ceibro.databinding.LayoutCeibroOnlyImageBinding
import com.zstronics.ceibro.databinding.LayoutTaskBoxV2Binding
import com.zstronics.ceibro.databinding.LayoutTaskBoxV2FromMeBinding
import com.zstronics.ceibro.utils.DateUtils
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

class TaskFromMeRVAdapter @Inject constructor() :
    RecyclerView.Adapter<TaskFromMeRVAdapter.TaskFromMeViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: CeibroTaskV2) -> Unit)? =
        null
    var listItems: MutableList<CeibroTaskV2> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskFromMeViewHolder {
        return TaskFromMeViewHolder(
            LayoutTaskBoxV2FromMeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TaskFromMeViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<CeibroTaskV2>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class TaskFromMeViewHolder(private val binding: LayoutTaskBoxV2FromMeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CeibroTaskV2) {
            val context = binding.taskTickMark.context

            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }

            binding.taskCardParentLayout.background = null
            binding.taskCanceledText.visibility = View.GONE
            //Use following two lines if a task is cancelled
//            binding.taskCardParentLayout.background = context.resources.getDrawable(R.drawable.task_card_cancel_outline)
//            binding.taskCanceledText.visibility = View.VISIBLE

            binding.taskId.text = item.taskUID

            binding.taskDueDate.text = DateUtils.reformatStringDate(
                date = item.dueDate,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
            )
            if (binding.taskDueDate.text == "") {                              // Checking if date format was not dd-MM-yyyy then it will be empty
                binding.taskDueDate.text = DateUtils.reformatStringDate(
                    date = item.dueDate,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
                )
                if (binding.taskDueDate.text == "") {
                    binding.taskDueDate.text = "N/A"
                }
            }

            binding.taskToText.text =
                if (item.assignedToState.size == 1) {
                    "${item.assignedToState[0].firstName} ${item.assignedToState[0].surName}"
                } else if (item.assignedToState.size > 1) {
                    "${item.assignedToState[0].firstName} ${item.assignedToState[0].surName}  +${item.assignedToState.size -1}"
                } else {
                    "N/A"
                }

            if (item.project != null) {
                binding.taskProjectText.text = item.project.title
            } else {
                binding.taskProjectText.text = "N/A"
            }

            if (item.topic != null) {
                binding.taskTitle.text = item.topic.topic
            } else {
                binding.taskTitle.text = "N/A"
            }

            binding.taskDescription.text = item.description

            binding.taskCreationDate.text = DateUtils.reformatStringDate(
                date = item.createdAt,
                DateUtils.SERVER_DATE_FULL_FORMAT,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
            )

        }
    }
}