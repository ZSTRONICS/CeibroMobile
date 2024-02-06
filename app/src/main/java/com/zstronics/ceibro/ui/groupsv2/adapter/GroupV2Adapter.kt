package com.zstronics.ceibro.ui.groupsv2.adapter


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
import com.zstronics.ceibro.data.database.models.inbox.CeibroInboxV2
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutInboxTaskBoxV2Binding
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class GroupV2Adapter @Inject constructor() :
    RecyclerView.Adapter<GroupV2Adapter.TaskToMeViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: CeibroInboxV2) -> Unit)? =
        null
    var itemLongClickListener: ((view: View, position: Int, data: CeibroInboxV2) -> Unit)? =
        null
    var listItems: MutableList<CeibroInboxV2> = mutableListOf()
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

    fun setList(list: List<CeibroInboxV2>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class TaskToMeViewHolder(private val binding: LayoutInboxTaskBoxV2Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CeibroInboxV2?) {
            val context = binding.root.context

            if (item != null) {
                binding.apply {

                    taskCard.setOnClickListener {
                        itemClickListener?.invoke(it, absoluteAdapterPosition, item)
                    }

                    val taskStatusNameBg: Pair<Int, Int> = when (item.taskState.uppercase()) {
                        TaskStatus.NEW.name -> Pair(
                            R.drawable.status_new_outline_new,
                            R.drawable.status_new_filled
                        )

//                        TaskStatus.UNREAD.name -> Pair(
//                            R.drawable.status_new_outline_new,
//                            R.drawable.status_new_filled
//                        )

                        TaskStatus.ONGOING.name -> Pair(
                            R.drawable.status_ongoing_outline_new,
                            R.drawable.status_ongoing_filled
                        )

                        TaskStatus.DONE.name -> Pair(
                            R.drawable.status_done_outline_new,
                            R.drawable.status_done_filled
                        )

                        TaskStatus.CANCELED.name -> Pair(
                            R.drawable.status_cancelled_outline,
                            R.drawable.status_cancelled_filled_less_corner
                        )

                        else -> Pair(
                            R.drawable.unseen_corners_background,
                            R.drawable.unseen_corners_background
                        )
                    }

                    val (inboxTaskCardBackground, inboxTaskUIdBackground) = taskStatusNameBg
//                    inboxTaskCardParentLayout.setBackgroundResource(inboxTaskCardBackground)
                    inboxTaskUId.setBackgroundResource(inboxTaskUIdBackground)
                    inboxTaskUId.text = item.actionDataTask.taskUID


                    when (item.actionType) {
                        SocketHandler.TaskEvent.IB_TASK_CREATED.name -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_created)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                        }

                        SocketHandler.TaskEvent.IB_STATE_CHANGED.name -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_state_change_new_to_ongoing)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                        }

                        SocketHandler.TaskEvent.IB_NEW_TASK_COMMENT.name -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_comment)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                        }

                        SocketHandler.TaskEvent.IB_TASK_FORWARDED.name -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_forwarded)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                        }

                        SocketHandler.TaskEvent.IB_JOINED_TASK.name -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_joined)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                        }

                        SocketHandler.TaskEvent.IB_TASK_DONE.name -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_done)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                        }

                        SocketHandler.TaskEvent.IB_CANCELED_TASK.name -> {
                            inboxTaskStateIcon.setBackgroundResource(R.drawable.icon_task_canceled)
                            inboxTaskStateIcon.visibility = View.VISIBLE
                        }

                        else -> {
                            inboxTaskStateIcon.visibility = View.GONE
                        }
                    }

                    if (item.actionDataTask.project != null) {
                        if (item.actionDataTask.project.title.isNotEmpty()) {
                            inboxTaskProjectName.text = item.actionDataTask.project.title
                            inboxTaskProjectName.visibility = View.VISIBLE
                        } else {
                            inboxTaskProjectName.text = ""
                            inboxTaskProjectName.visibility = View.GONE
                        }
                    } else {
                        inboxTaskProjectName.text = ""
                        inboxTaskProjectName.visibility = View.GONE
                    }

                    if (item.actionDataTask.dueDate.isEmpty()) {
                        inboxTaskDueDate.text = ""
                        inboxTaskDueDate.visibility = View.GONE
                    } else {
                        var dueDate = ""
                        dueDate = DateUtils.reformatStringDate(
                            date = item.actionDataTask.dueDate,
                            DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                            DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
                        )
                        if (dueDate == "") {                              // Checking if date format was not dd-MM-yyyy then it will be empty
                            dueDate = DateUtils.reformatStringDate(
                                date = item.actionDataTask.dueDate,
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


                    inboxTaskTitle.text = item.actionTitle


                    if (item.actionDescription.isEmpty()) {
                        inboxTaskEventDescription.text = ""
                        inboxTaskEventDescription.visibility = View.GONE
                    } else {
                        inboxTaskEventDescription.text = item.actionDescription
                        inboxTaskEventDescription.visibility = View.VISIBLE
                    }

                    if (!item.actionBy.profilePic.isNullOrEmpty()) {
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
                            .load(item.actionBy.profilePic)
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

                    inboxCreatorName.text = "${item.actionBy.firstName} ${item.actionBy.surName}"

                    inboxCreatedAt.text =
                        DateUtils.formatCreationUTCTimeToCustom(
                            utcTime = item.createdAt,
                            inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
                        )



                    if (item.actionFiles.isEmpty()) {
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
                        val extension = getFileUrlExtension(item.actionFiles[0].fileUrl)
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
                                .load(item.actionFiles[0].fileUrl)
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
                            } else if (extension.equals("odt", true) || extension.equals("odp", true) ||
                                extension.equals("docx", true) || extension.equals("doc", true) ||
                                extension.equals("xlsx", true) || extension.equals("xls", true) ||
                                extension.equals("pptx", true) || extension.equals("ppt", true)) {
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

                        if (item.actionFiles.size > 1) {
                            inboxImgCount.text = "+${item.actionFiles.size - 1}"
                            inboxImgCount.visibility = View.VISIBLE
                        } else {
                            inboxImgCount.visibility = View.GONE
                        }
                    }

                    // inboxTaskUnseenLayout background depends whether the task has image or not, so background is set in actionFiles on top of here
                    // and inboxTaskUnseenLayout background color depends on task seen

                    if (item.isSeen) {
                        val tintColor = context.resources.getColor(R.color.white)
                        inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    } else {
                        val tintColor = context.resources.getColor(R.color.appPaleBlue)
                        inboxTaskUnseenLayout.backgroundTintList = ColorStateList.valueOf(tintColor)
                    }

                    //Following unseen count can also come outside from this else condition depending on the scenario
                    if (item.unSeenNotifCount > 1) {
                        unreadCount.text = item.unSeenNotifCount.toString()
                        unreadCount.visibility = View.VISIBLE
                    } else {
                        unreadCount.text = "0"
                        unreadCount.visibility = View.GONE
                    }

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