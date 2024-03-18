package com.zstronics.ceibro.ui.tasks.v3.hidden.fragment


import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutTaskBoxV3Binding
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class HiddenTasksV3Adapter @Inject constructor() :
    RecyclerView.Adapter<HiddenTasksV3Adapter.TaskToMeViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: CeibroTaskV2) -> Unit)? =
        null
    var menuClickListener: ((view: View, position: Int, data: CeibroTaskV2) -> Unit)? =
        null
    var itemLongClickListener: ((view: View, position: Int, data: CeibroTaskV2) -> Unit)? =
        null
    var listItems: MutableList<CeibroTaskV2> = mutableListOf()
    var currentUser = SessionManager.user.value
    var sessionManager: SessionManager? = null
    var selectedState: String = TaskRootStateTags.All.tagValue

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskToMeViewHolder {
        return TaskToMeViewHolder(
            LayoutTaskBoxV3Binding.inflate(
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

    fun setList(list: List<CeibroTaskV2>, selectedState: String) {
        this.listItems.clear()
        this.listItems.addAll(list)
        this.selectedState = selectedState
        notifyDataSetChanged()
    }

    inner class TaskToMeViewHolder(private val binding: LayoutTaskBoxV3Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CeibroTaskV2?) {
            val context = binding.root.context

            if (item != null) {
                binding.apply {

                    taskCard.setOnClickListener {
                        itemClickListener?.invoke(it, absoluteAdapterPosition, item)
                    }
                    taskMenuBtn.setOnClickListener {
                        menuClickListener?.invoke(it, absoluteAdapterPosition, item)
                    }

                    val taskState =
                        if (selectedState.equals(TaskRootStateTags.All.tagValue, true)) {
                            if (item.isCreator || item.isTaskViewer) {
                                item.creatorState
                            } else if (item.isAssignedToMe) {
                                item.userSubState
                            } else {
                                item.userSubState
                            }
                        } else if (selectedState.equals(
                                TaskRootStateTags.ToMe.tagValue,
                                true
                            ) || selectedState.equals(TaskRootStateTags.InReview.tagValue, true)
                        ) {
                            item.userSubState
                        } else if (selectedState.equals(
                                TaskRootStateTags.FromMe.tagValue,
                                true
                            ) || selectedState.equals(TaskRootStateTags.ToReview.tagValue, true)
                        ) {
                            item.creatorState
                        } else {
                            "Unknown"
                        }

                    val taskStatusNameBg: Pair<Int, Int> = when (taskState.uppercase()) {
                        TaskStatus.NEW.name -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_tasks_footer)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                            Pair(
                                R.drawable.status_new_outline_new,
                                R.drawable.status_new_outline
                            )
                        }

                        TaskStatus.UNREAD.name -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_tasks_footer)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                            Pair(
                                R.drawable.status_new_outline_new,
                                R.drawable.status_new_outline
                            )
                        }

                        TaskStatus.ONGOING.name -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_ongoing)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                            Pair(
                                R.drawable.status_ongoing_outline_new,
                                R.drawable.status_ongoing_outline
                            )
                        }

                        TaskRootStateTags.InReview.tagValue.uppercase() -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_pending)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                            Pair(
                                R.drawable.status_in_review_outline,
                                R.drawable.status_in_review_outline
                            )
                        }

                        TaskRootStateTags.ToReview.tagValue.uppercase() -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_pending)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                            Pair(
                                R.drawable.status_in_review_outline,
                                R.drawable.status_in_review_outline
                            )
                        }

                        TaskStatus.DONE.name -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_done)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                            Pair(
                                R.drawable.status_done_outline_new,
                                R.drawable.status_done_outline
                            )
                        }

                        TaskStatus.CANCELED.name -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_canceled)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                            Pair(
                                R.drawable.status_cancelled_outline,
                                R.drawable.status_cancelled_filled_less_corner
                            )
                        }

                        TaskDetailEvents.REJECT_CLOSED.eventValue.uppercase() -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_canceled)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                            Pair(
                                R.drawable.status_reject_outline_more_corner,
                                R.drawable.status_reject_filled
                            )
                        }

                        else -> {
                            inboxTaskStateIcon.visibility = View.GONE
                            Pair(
                                R.drawable.unseen_corners_background,
                                R.drawable.unseen_corners_background
                            )
                        }
                    }

                    val (inboxTaskCardBackground, inboxTaskUIdBackground) = taskStatusNameBg
//                    inboxTaskCardParentLayout.setBackgroundResource(inboxTaskCardBackground)
                    inboxTaskUId.setBackgroundResource(inboxTaskUIdBackground)
                    inboxTaskUId.text = item.taskUID

                    if (item.isTaskViewer) {
                        inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_viewer)
                        inboxTaskStateIcon.visibility = View.VISIBLE
                    }

//                    if (item.isTaskInApproval) {
                    taskMenuBtn.visibility = View.VISIBLE
//                    } else {
//                        taskMenuBtn.visibility = View.GONE
//                    }

                    if (item.project != null) {
                        if (item.project.title.isNotEmpty()) {
                            inboxTaskProjectName.text = item.project.title
                            inboxTaskProjectName.visibility = View.VISIBLE
                        } else {
                            inboxTaskProjectName.text = ""
                            inboxTaskProjectName.visibility = View.GONE
                        }
                    } else {
                        inboxTaskProjectName.text = ""
                        inboxTaskProjectName.visibility = View.GONE
                    }

                    if (item.dueDate.isEmpty()) {
                        inboxTaskDueDate.text = ""
                        inboxTaskDueDate.visibility = View.GONE
                    } else {
                        var dueDate = ""
                        dueDate = DateUtils.reformatStringDate(
                            date = item.dueDate,
                            DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                            DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
                        )
                        if (dueDate == "") {                              // Checking if date format was not dd-MM-yyyy then it will be empty
                            dueDate = DateUtils.reformatStringDate(
                                date = item.dueDate,
                                DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT,
                                DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
                            )
                            if (dueDate == "") {
                                dueDate = "N/A"
                            }
                        }
                        inboxTaskDueDate.text = "Due By: $dueDate"
                        inboxTaskDueDate.visibility = View.VISIBLE
                    }


                    inboxTaskTitle.text = item.title


                    if (item.description.isEmpty()) {
                        inboxTaskEventDescription.text = ""
                        inboxTaskEventDescription.visibility = View.GONE
                    } else {
                        inboxTaskEventDescription.text = item.description
                        inboxTaskEventDescription.visibility = View.VISIBLE
                    }

                    if (!item.creator.profilePic.isNullOrEmpty()) {
                        val circularProgressDrawable = CircularProgressDrawable(context)
                        circularProgressDrawable.strokeWidth = 4f
                        circularProgressDrawable.centerRadius = 14f
                        circularProgressDrawable.start()

                        val requestOptions = RequestOptions()
                            .placeholder(circularProgressDrawable)
                            .error(R.drawable.profile_img)
                            .skipMemoryCache(true)
                            .centerCrop()

                        Glide.with(context)
                            .load(item.creator.profilePic)
                            .apply(requestOptions)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    circularProgressDrawable.stop()
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    circularProgressDrawable.stop()
                                    return false
                                }
                            })
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(inboxTaskCreatorImg)
                    }

                    inboxCreatorName.text = "${item.creator.firstName} ${item.creator.surName}"

                    inboxCreatedAt.text =
                        DateUtils.formatCreationUTCTimeToCustom(
                            utcTime = item.createdAt,
                            inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
                        )



                    if (item.files.isEmpty()) {
                        inboxTaskUnseenLayout.setBackgroundResource(R.drawable.unseen_corners_background)
                        inboxItemImgCard.visibility = View.GONE
                        inboxItemImg.visibility = View.GONE
                        inboxImgCount.visibility = View.GONE
                        val layoutParams =
                            inboxImgEndPoint.layoutParams as ConstraintLayout.LayoutParams
                        // Set the new horizontal bias for image width
                        layoutParams.horizontalBias = 0.0f
                        inboxImgEndPoint.layoutParams = layoutParams
                    } else {
                        val extension = getFileUrlExtension(item.files[0].fileUrl)
                        println("File Extension: $extension")

                        if (isImageExtension(extension)) {
                            val circularProgressDrawable = CircularProgressDrawable(context)
                            circularProgressDrawable.strokeWidth = 4f
                            circularProgressDrawable.centerRadius = 14f
                            circularProgressDrawable.start()

                            val requestOptions = RequestOptions()
                                .placeholder(circularProgressDrawable)
                                .error(R.drawable.icon_corrupted)
                                .skipMemoryCache(true)
                                .centerCrop()

                            Glide.with(context)
                                .load(item.files[0].fileUrl)
                                .apply(requestOptions)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        circularProgressDrawable.stop()
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        circularProgressDrawable.stop()
                                        return false
                                    }
                                })
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(inboxItemImg)
                            inboxItemImg.scaleType = ImageView.ScaleType.CENTER_CROP

                        } else {
                            if (extension.equals("pdf", true)) {
                                inboxItemImg.setImageResource(R.drawable.icon_pdf)
                                inboxItemImg.scaleType = ImageView.ScaleType.FIT_CENTER
                            } else if (extension.equals("odt", true) || extension.equals(
                                    "odp",
                                    true
                                ) ||
                                extension.equals("docx", true) || extension.equals("doc", true) ||
                                extension.equals("xlsx", true) || extension.equals("xls", true) ||
                                extension.equals("pptx", true) || extension.equals("ppt", true)
                            ) {
                                inboxItemImg.setImageResource(R.drawable.icon_doc)
                                inboxItemImg.scaleType = ImageView.ScaleType.FIT_CENTER
                            } else {
                                inboxItemImg.setImageResource(R.drawable.icon_corrupted)
                                inboxItemImg.scaleType = ImageView.ScaleType.FIT_CENTER
                            }
                        }


                        inboxTaskUnseenLayout.setBackgroundResource(R.drawable.right_corners_background)
                        inboxItemImgCard.visibility = View.VISIBLE
                        inboxItemImg.visibility = View.VISIBLE
                        val layoutParams =
                            inboxImgEndPoint.layoutParams as ConstraintLayout.LayoutParams
                        // Set the new horizontal bias for image width
                        layoutParams.horizontalBias = 0.20f
                        inboxImgEndPoint.layoutParams = layoutParams

                        if (item.files.size > 1) {
                            inboxImgCount.text = "+${item.files.size - 1}"
                            inboxImgCount.visibility = View.VISIBLE
                        } else {
                            inboxImgCount.visibility = View.GONE
                        }
                    }

                    // inboxTaskUnseenLayout background depends whether the task has image or not, so background is set in actionFiles on top of here
                    // and inboxTaskUnseenLayout background color depends on task seen

                    if (item.isSeenByMe) {
                        val tintColor = context.resources.getColor(R.color.white)
                        inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    } else {
                        val tintColor = context.resources.getColor(R.color.appPaleBlue)
                        inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    }


                    unreadCount.visibility = View.GONE

                }
            } else {
                binding.taskCard.visibility = View.GONE
                binding.unreadCount.visibility = View.GONE
            }

        }

        private fun getFileUrlExtension(url: String): String {
            val lastDotIndex = url.lastIndexOf('.')
            return if (lastDotIndex != -1) {
                url.substring(lastDotIndex + 1)
            } else {
                ""
            }
        }

        private fun isImageExtension(extension: String): Boolean {
            val imageExtensions = listOf("png", "jpg", "jpeg", "gif", "bmp", "webp", "heic")
            return extension.lowercase() in imageExtensions
        }
    }
}