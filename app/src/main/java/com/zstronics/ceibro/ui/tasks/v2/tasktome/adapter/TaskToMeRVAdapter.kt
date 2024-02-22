package com.zstronics.ceibro.ui.tasks.v2.tasktome.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutTaskBoxV2Binding
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class TaskToMeRVAdapter @Inject constructor() :
    RecyclerView.Adapter<TaskToMeRVAdapter.TaskToMeViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: CeibroTaskV2) -> Unit)? =
        null
    var itemLongClickListener: ((view: View, position: Int, data: CeibroTaskV2) -> Unit)? =
        null
    var listItems: MutableList<CeibroTaskV2> = mutableListOf()
    var currentUser = SessionManager.user.value
    var sessionManager: SessionManager? = null

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

    fun setList(list: List<CeibroTaskV2>, sessionManager: SessionManager) {
        this.sessionManager = sessionManager
        println("TaskAdaptersSeenIssue: To-Me -> currentUser in setList = ${currentUser?.id}")
        if (sessionManager.getUser().value?.id.isNullOrEmpty()) {
            sessionManager.setUser()
            currentUser = sessionManager.getUser().value
        } else {
            currentUser = sessionManager.getUser().value
        }
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
            binding.root.setOnLongClickListener {
                itemLongClickListener?.invoke(it, absoluteAdapterPosition, item)
                true
            }

            if (sessionManager != null) {
                if (currentUser?.id.isNullOrEmpty()) {
                    println("TaskAdaptersSeenIssue: To-Me -> currentUser.id = ${currentUser?.id}")
                    sessionManager?.setUser()
                    currentUser = sessionManager?.getUser()?.value
                }
            }

//            if (state.equals(TaskStatus.NEW.name.lowercase(), true)) {
//                binding.taskId.background =
//                    context.getDrawable(R.drawable.task_card_uid_new)
//            } else if (state.equals(TaskStatus.ONGOING.name.lowercase(), true)) {
//                binding.taskId.background =
//                    context.getDrawable(R.drawable.task_card_uid_ongoing)
//            } else if (state.equals(TaskStatus.DONE.name.lowercase(), true)) {
//                binding.taskId.background =
//                    context.getDrawable(R.drawable.task_card_uid_done)
//            }

            binding.taskCardParentLayout.background = null
            binding.taskCanceledText.visibility = View.GONE
            //Use following two lines if a task is cancelled
//            binding.taskCardParentLayout.background = context.resources.getDrawable(R.drawable.task_card_cancel_outline)
//            binding.taskCanceledText.visibility = View.VISIBLE

            val seenByMe = item.seenBy.find { it == currentUser?.id }
            if (!seenByMe.isNullOrEmpty()) {
//                val tintColor = context.resources.getColor(R.color.appBlue)
//                binding.taskTickMark.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
                val tintColor = context.resources.getColor(R.color.white)
                binding.taskCardHeaderLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                binding.taskCardLayout.setBackgroundResource(R.drawable.task_card_outline)
            } else {
//                val tintColor = context.resources.getColor(R.color.appGrey3)
//                binding.taskTickMark.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
                val tintColor = context.resources.getColor(R.color.appPaleBlue)
                binding.taskCardHeaderLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                binding.taskCardLayout.setBackgroundResource(R.drawable.task_card_outline_unseen)
            }

            binding.taskId.text = item.taskUID

            binding.taskCreationDate.text = DateUtils.formatCreationUTCTimeToCustom(
                utcTime = item.createdAt,
                inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
            )

            var dueDate = ""
            dueDate = DateUtils.reformatStringDate(
                date = item.dueDate,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                DateUtils.SHORT_DATE_MON_YEAR_ONLY
            )
            if (dueDate == "") {                              // Checking if date format was not dd-MM-yyyy then it will be empty
                dueDate = DateUtils.reformatStringDate(
                    date = item.dueDate,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT,
                    DateUtils.SHORT_DATE_MON_YEAR_ONLY
                )
                if (dueDate == "") {
                    dueDate = "N/A"
                }
            }
            binding.taskDueDate.text = "Due Date: $dueDate"

            binding.taskFromText.text = "${item.creator.firstName} ${item.creator.surName}"

            if (item.project != null) {
                binding.taskProjectLayout.visibility = View.VISIBLE
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

            if (!item.title.isNullOrEmpty()) {
                binding.taskTitle.text = item.title
            } else {
                binding.taskTitle.text = "N/A"
            }

            if (item.description.isEmpty()) {
                binding.taskDescription.visibility = View.GONE
                binding.taskDescription.text = ""
            } else {
                binding.taskDescription.visibility = View.VISIBLE
                binding.taskDescription.text = item.description
            }

        }
    }
}