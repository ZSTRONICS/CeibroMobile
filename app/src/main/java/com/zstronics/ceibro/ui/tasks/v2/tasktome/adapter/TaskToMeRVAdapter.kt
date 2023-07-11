package com.zstronics.ceibro.ui.tasks.v2.tasktome.adapter

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.auth.login.User
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutCeibroFilesBinding
import com.zstronics.ceibro.databinding.LayoutCeibroOnlyImageBinding
import com.zstronics.ceibro.databinding.LayoutTaskBoxV2Binding
import com.zstronics.ceibro.utils.DateUtils
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

class TaskToMeRVAdapter @Inject constructor() :
    RecyclerView.Adapter<TaskToMeRVAdapter.TaskToMeViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: CeibroTaskV2) -> Unit)? =
        null
    var listItems: MutableList<CeibroTaskV2> = mutableListOf()
    val currentUser = SessionManager.user.value

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskToMeViewHolder {
        return TaskToMeViewHolder(
            LayoutTaskBoxV2Binding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TaskToMeViewHolder, position: Int) {
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

    inner class TaskToMeViewHolder(private val binding: LayoutTaskBoxV2Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CeibroTaskV2) {
            val context = binding.root.context

            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }

            binding.taskCardParentLayout.background = null
            binding.taskCanceledText.visibility = View.GONE
            //Use following two lines if a task is cancelled
//            binding.taskCardParentLayout.background = context.resources.getDrawable(R.drawable.task_card_cancel_outline)
//            binding.taskCanceledText.visibility = View.VISIBLE

            val seenByMe = item.seenBy.find { it == currentUser?.id }
            if (seenByMe != null) {
                val tintColor = context.resources.getColor(R.color.appBlue)
                binding.taskTickMark.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
            } else {
                val tintColor = context.resources.getColor(R.color.appGrey3)
                binding.taskTickMark.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
            }

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

            binding.taskFromText.text = "${item.creator.firstName} ${item.creator.surName}"

            if (item.project != null) {
                binding.taskProjectText.text = item.project.title

                val layoutParams = binding.bottomCenterPoint.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.horizontalBias = 0.52f  // Set the desired bias value between 0.0 and 1.0
                binding.bottomCenterPoint.layoutParams = layoutParams
            } else {
                binding.taskProjectLayout.visibility = View.GONE

                val layoutParams = binding.bottomCenterPoint.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.horizontalBias = 0.85f  // Set the desired bias value between 0.0 and 1.0
                binding.bottomCenterPoint.layoutParams = layoutParams
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