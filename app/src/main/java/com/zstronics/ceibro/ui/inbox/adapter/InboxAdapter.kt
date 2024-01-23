package com.zstronics.ceibro.ui.inbox.adapter


import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutInboxTaskBoxV2Binding
import javax.inject.Inject

class InboxAdapter @Inject constructor() :
    RecyclerView.Adapter<InboxAdapter.TaskToMeViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: CeibroTaskV2) -> Unit)? =
        null
    var itemLongClickListener: ((view: View, position: Int, data: CeibroTaskV2) -> Unit)? =
        null
    var listItems: MutableList<Int> = mutableListOf()
    var currentUser = SessionManager.user.value
    var sessionManager: SessionManager? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskToMeViewHolder {
        return TaskToMeViewHolder(
            LayoutInboxTaskBoxV2Binding.inflate(
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

    fun setList(list: List<Int>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class TaskToMeViewHolder(private val binding: LayoutInboxTaskBoxV2Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Int?) {
            val context = binding.root.context
            binding.apply {

                if (item == 2) {
                    inboxTaskCardParentLayout.background = context.resources.getDrawable(R.drawable.status_new_outline_new)
                    inboxTaskUId.background = context.resources.getDrawable(R.drawable.status_new_filled)
                    inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_comment)
                    inboxTaskStateIcon.visibility = View.GONE
                    inboxTaskEventDescription.visibility = View.VISIBLE
                    inboxItemImg.visibility = View.VISIBLE
                    val layoutParams =
                        inboxImgEndPoint.layoutParams as ConstraintLayout.LayoutParams
                    // Set the new horizontal bias
                    layoutParams.horizontalBias = 0.31f
                    inboxImgEndPoint.layoutParams = layoutParams

                    val tintColor = context.resources.getColor(R.color.white)
                    inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    inboxTaskUnseenLayout.setBackgroundResource(R.drawable.right_corners_background)

                } else if (item == 3) {
                    inboxTaskCardParentLayout.background = context.resources.getDrawable(R.drawable.status_new_outline_new)
                    inboxTaskUId.background = context.resources.getDrawable(R.drawable.status_new_filled)
                    inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_comment)
                    inboxTaskStateIcon.visibility = View.GONE
                    inboxTaskEventDescription.visibility = View.GONE
                    inboxItemImg.visibility = View.GONE
                    val layoutParams =
                        inboxImgEndPoint.layoutParams as ConstraintLayout.LayoutParams
                    // Set the new horizontal bias
                    layoutParams.horizontalBias = 0.0f
                    inboxImgEndPoint.layoutParams = layoutParams

                    val tintColor = context.resources.getColor(R.color.white)
                    inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    inboxTaskUnseenLayout.setBackgroundResource(R.drawable.unseen_corners_background)

                } else if (item == 4) {
                    inboxTaskCardParentLayout.background = context.resources.getDrawable(R.drawable.status_ongoing_outline_new)
                    inboxTaskUId.background = context.resources.getDrawable(R.drawable.status_ongoing_filled)
                    inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_comment)
                    inboxTaskStateIcon.visibility = View.VISIBLE
                    inboxTaskEventDescription.visibility = View.GONE
                    inboxItemImg.visibility = View.VISIBLE
                    val layoutParams =
                        inboxImgEndPoint.layoutParams as ConstraintLayout.LayoutParams
                    // Set the new horizontal bias
                    layoutParams.horizontalBias = 0.31f
                    inboxImgEndPoint.layoutParams = layoutParams

                    val tintColor = context.resources.getColor(R.color.white)
                    inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    inboxTaskUnseenLayout.setBackgroundResource(R.drawable.right_corners_background)

                } else if (item == 5) {
                    inboxTaskCardParentLayout.background = context.resources.getDrawable(R.drawable.status_done_outline_new)
                    inboxTaskUId.background = context.resources.getDrawable(R.drawable.status_done_filled)
                    inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_done)
                    inboxTaskStateIcon.visibility = View.VISIBLE
                    inboxTaskEventDescription.visibility = View.VISIBLE
                    inboxItemImg.visibility = View.GONE
                    val layoutParams =
                        inboxImgEndPoint.layoutParams as ConstraintLayout.LayoutParams
                    // Set the new horizontal bias
                    layoutParams.horizontalBias = 0.0f
                    inboxImgEndPoint.layoutParams = layoutParams

                    val tintColor = context.resources.getColor(R.color.appPaleBlue)
                    inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    inboxTaskUnseenLayout.setBackgroundResource(R.drawable.unseen_corners_background)

                } else if (item == 6) {
                    inboxTaskCardParentLayout.background = context.resources.getDrawable(R.drawable.status_ongoing_outline_new)
                    inboxTaskUId.background = context.resources.getDrawable(R.drawable.status_ongoing_filled)
                    inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_comment)
                    inboxTaskStateIcon.visibility = View.VISIBLE
                    inboxTaskEventDescription.visibility = View.VISIBLE
                    inboxItemImg.visibility = View.GONE
                    val layoutParams =
                        inboxImgEndPoint.layoutParams as ConstraintLayout.LayoutParams
                    // Set the new horizontal bias
                    layoutParams.horizontalBias = 0.0f
                    inboxImgEndPoint.layoutParams = layoutParams

                    val tintColor = context.resources.getColor(R.color.white)
                    inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    inboxTaskUnseenLayout.setBackgroundResource(R.drawable.unseen_corners_background)

                } else if (item == 7) {
                    inboxTaskCardParentLayout.background = context.resources.getDrawable(R.drawable.status_cancelled_outline)
                    inboxTaskUId.background = context.resources.getDrawable(R.drawable.status_cancelled_filled_less_corner)
                    inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_canceled)
                    inboxTaskStateIcon.visibility = View.VISIBLE
                    inboxTaskEventDescription.visibility = View.GONE
                    inboxItemImg.visibility = View.GONE
                    val layoutParams =
                        inboxImgEndPoint.layoutParams as ConstraintLayout.LayoutParams
                    // Set the new horizontal bias
                    layoutParams.horizontalBias = 0.0f
                    inboxImgEndPoint.layoutParams = layoutParams

                    val tintColor = context.resources.getColor(R.color.white)
                    inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    inboxTaskUnseenLayout.setBackgroundResource(R.drawable.unseen_corners_background)

                } else if (item == 8) {
                    inboxTaskCardParentLayout.background = context.resources.getDrawable(R.drawable.status_ongoing_outline_new)
                    inboxTaskUId.background = context.resources.getDrawable(R.drawable.status_ongoing_filled)
                    inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_state_change_new_to_ongoing)
                    inboxTaskStateIcon.visibility = View.VISIBLE
                    inboxTaskEventDescription.visibility = View.VISIBLE
                    inboxItemImg.visibility = View.GONE
                    val layoutParams =
                        inboxImgEndPoint.layoutParams as ConstraintLayout.LayoutParams
                    // Set the new horizontal bias
                    layoutParams.horizontalBias = 0.0f
                    inboxImgEndPoint.layoutParams = layoutParams
                    val tintColor = context.resources.getColor(R.color.white)
                    inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    inboxTaskUnseenLayout.setBackgroundResource(R.drawable.unseen_corners_background)

                } else if (item == 9) {
                    inboxTaskCardParentLayout.background = context.resources.getDrawable(R.drawable.status_done_outline_new)
                    inboxTaskUId.background = context.resources.getDrawable(R.drawable.status_done_filled)
                    inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_comment)
                    inboxTaskStateIcon.visibility = View.VISIBLE
                    inboxTaskEventDescription.visibility = View.VISIBLE
                    inboxItemImg.visibility = View.VISIBLE
                    val layoutParams =
                        inboxImgEndPoint.layoutParams as ConstraintLayout.LayoutParams
                    // Set the new horizontal bias
                    layoutParams.horizontalBias = 0.31f
                    inboxImgEndPoint.layoutParams = layoutParams

                    val tintColor = context.resources.getColor(R.color.appPaleBlue)
                    inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    inboxTaskUnseenLayout.setBackgroundResource(R.drawable.right_corners_background)

                } else if (item == 10) {
                    inboxTaskCardParentLayout.background = context.resources.getDrawable(R.drawable.status_ongoing_outline_new)
                    inboxTaskUId.background = context.resources.getDrawable(R.drawable.status_ongoing_filled)
                    inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_comment)
                    inboxTaskStateIcon.visibility = View.VISIBLE
                    inboxTaskEventDescription.visibility = View.GONE
                    inboxItemImg.visibility = View.VISIBLE
                    val layoutParams =
                        inboxImgEndPoint.layoutParams as ConstraintLayout.LayoutParams
                    // Set the new horizontal bias
                    layoutParams.horizontalBias = 0.31f
                    inboxImgEndPoint.layoutParams = layoutParams

                    val tintColor = context.resources.getColor(R.color.white)
                    inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    inboxTaskUnseenLayout.setBackgroundResource(R.drawable.right_corners_background)

                } else {
                    inboxTaskCardParentLayout.background = context.resources.getDrawable(R.drawable.status_ongoing_outline_new)
                    inboxTaskUId.background = context.resources.getDrawable(R.drawable.status_ongoing_filled)
                    inboxTaskStateIcon.visibility = View.GONE
                    inboxTaskEventDescription.visibility = View.VISIBLE
                    inboxItemImg.visibility = View.VISIBLE
                    val layoutParams =
                        inboxImgEndPoint.layoutParams as ConstraintLayout.LayoutParams
                    // Set the new horizontal bias
                    layoutParams.horizontalBias = 0.31f
                    inboxImgEndPoint.layoutParams = layoutParams

                    val tintColor = context.resources.getColor(R.color.white)
                    inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    inboxTaskUnseenLayout.setBackgroundResource(R.drawable.right_corners_background)
                }
            }


        }
    }
}