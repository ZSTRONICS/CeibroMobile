package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailcomments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
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
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroDownloadDrawingV2
import com.zstronics.ceibro.data.database.models.tasks.EventFiles
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.databinding.LayoutCeibroEventChatOnlyBinding
import com.zstronics.ceibro.databinding.LayoutCeibroTaskEventsBinding
import com.zstronics.ceibro.databinding.LayoutCeibroTaskTagsOnlyBinding
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.EventsFilesRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.EventsImageWithCommentRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.EventsOnlyImageRVAdapter
import com.zstronics.ceibro.utils.DateUtils

class EventsMultiViewRVAdapter constructor(
    val networkConnectivityObserver: NetworkConnectivityObserver,
    val context: Context,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        private const val VIEW_TYPE_COMMENT_AND_TAG = 0
        private const val VIEW_TYPE_TAG_ONLY = 1
        private const val VIEW_TYPE_CHAT_COMMENT = 2
    }

    var pinClickListener: ((position: Int, data: Events, isPinned: Boolean) -> Unit)? =
        null
    var openEventImageClickListener: ((view: View, position: Int, imageFiles: List<TaskFiles>) -> Unit)? =
        null

    var fileClickListener: ((view: View, position: Int, data: EventFiles, downloadedData: CeibroDownloadDrawingV2) -> Unit)? =
        null
    var listItems: MutableList<Events> = mutableListOf()
    var loggedInUserId: String = ""


    var requestPermissionClickListener: ((tag: String) -> Unit)? = null

    fun requestPermissionCallBack(requestPermissionClickListener: (tag: String) -> Unit) {
        this.requestPermissionClickListener = requestPermissionClickListener
    }


    var downloadFileClickListener: ((textView: TextView, ivDownload: AppCompatImageView, downloaded: AppCompatImageView, triplet: Triple<String, String, String>, tag: String) -> Unit)? =
        null

    fun downloadFileCallBack(itemClickListener: ((textView: TextView, ivDownload: AppCompatImageView, downloaded: AppCompatImageView, triplet: Triple<String, String, String>, tag: String) -> Unit)?) {
        this.downloadFileClickListener = itemClickListener
    }


    var pinnedCommentClickListener: ((data: Events) -> Unit)? =
        null

    override fun getItemViewType(position: Int): Int {

        return if (listItems[position].eventType.equals(
                TaskDetailEvents.Comment.eventValue,
                true
            )
        ) {
            VIEW_TYPE_CHAT_COMMENT
        } else if (listItems[position].commentData != null && (!listItems[position].commentData?.message.isNullOrEmpty() || listItems[position].commentData?.files?.isNotEmpty() == true)) {
            VIEW_TYPE_COMMENT_AND_TAG
        } else {
            VIEW_TYPE_TAG_ONLY
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_COMMENT_AND_TAG -> EventsCeibroTagWithCommentsViewHolder(
                LayoutCeibroTaskEventsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            VIEW_TYPE_CHAT_COMMENT -> EventsCeibroChatViewHolder(
                LayoutCeibroEventChatOnlyBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            VIEW_TYPE_TAG_ONLY -> EventsCeibroTagsViewHolder(
                LayoutCeibroTaskTagsOnlyBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        when (viewType) {
            VIEW_TYPE_COMMENT_AND_TAG -> {
                (holder as EventsCeibroTagWithCommentsViewHolder)
                holder.bind((listItems[position]))
            }

            VIEW_TYPE_CHAT_COMMENT -> {
                (holder as EventsCeibroChatViewHolder)
                holder.bind(listItems[position])
            }

            VIEW_TYPE_TAG_ONLY -> {
                (holder as EventsCeibroTagsViewHolder)
                holder.bind(listItems[position])


            }
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<Events>, userId: String) {
        loggedInUserId = userId
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class EventsCeibroTagWithCommentsViewHolder(private val binding: LayoutCeibroTaskEventsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Events) {
            binding.rootLayout.setOnClickListener {
                pinnedCommentClickListener?.invoke(item)
            }

            val context = binding.eventPinImg.context
            binding.onlyComment.text = ""
            binding.onlyComment.visibility = View.GONE
            binding.onlyImagesRV.visibility = View.GONE
            binding.imagesWithCommentRV.visibility = View.GONE
            binding.forwardedToNames.text = ""
            binding.forwardedToNames.visibility = View.GONE
            binding.invitedNumbers.visibility = View.GONE
            binding.eventPinImg.visibility = View.GONE
            binding.viewMoreLessLayout.visibility = View.GONE
            binding.viewMoreBtn.visibility = View.GONE
            binding.viewLessBtn.visibility = View.GONE


            val creatorName =
                if (item.initiator.firstName.isEmpty() && item.initiator.surName.isEmpty()) {
                    "${item.initiator.phoneNumber}"
                } else {
                    "${item.initiator.firstName.trim()} ${item.initiator.surName.trim()}"
                }
            binding.eventBy.text = creatorName

            if (!item.initiator.profilePic.isNullOrEmpty()) {
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
                    .load(item.initiator.profilePic)
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
                    .into(binding.creatorImg)
            }

            binding.eventDate.text = DateUtils.formatCreationUTCTimeToCustomForDetailFiles(
                utcTime = item.createdAt,
                inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
            )


            val tintColor = if (!item.eventSeenBy.isNullOrEmpty()) {
                if (item.eventSeenBy?.contains(loggedInUserId) == true) {
                    context.resources.getColor(R.color.appBlue)
                } else {
                    context.resources.getColor(R.color.appGrey3)
                }
            } else {
                context.resources.getColor(R.color.appGrey3)
            }
            binding.seenImg.setColorFilter(tintColor)


            when (item.eventType) {
                TaskDetailEvents.ForwardTask.eventValue -> {

                    var forwardedToUsers = ""
                    if (!item.eventData.isNullOrEmpty()) {
                        forwardedToUsers += item.eventData.map {
                            if (it.firstName.isNullOrEmpty()) {
                                if (it.phoneNumber.isNullOrEmpty()) {
                                    " Unknown User ;"
                                } else {
                                    " ${it.phoneNumber} ;"
                                }
                            } else {
                                " ${it.firstName} ${it.surName} ;"
                            }
                        }
                            .toString().removeSurrounding("[", "]").removeSuffix(";")
                            .replace(",", "")
                    }
                    if (forwardedToUsers.isNotEmpty()) {
                        binding.forwardedToNames.text = "To: $forwardedToUsers"
                        binding.forwardedToNames.visibility = View.VISIBLE
                    }

                    var invitedUsers = ""
                    if (!item.invitedMembers.isNullOrEmpty()) {
                        invitedUsers += item.invitedMembers.map {
                            if (it.firstName.isNullOrEmpty())
                                " ${it.phoneNumber} ;"
                            else
                                " ${it.firstName} ${it.surName} ;"
                        }.toString().removeSurrounding("[", "]").removeSuffix(";").replace(",", "")
                    }
                    if (invitedUsers.isNotEmpty()) {
                        binding.invitedNumbers.text = "Invited: $invitedUsers"
                        binding.invitedNumbers.visibility = View.VISIBLE
                    }

                    var eventText = "${creatorName.toCamelCase()}"
                    if (forwardedToUsers.isNotEmpty()) {
                        eventText = "$eventText forwarded task to: $forwardedToUsers"
                    }
                    if (invitedUsers.isNotEmpty()) {
                        if (forwardedToUsers.isNotEmpty()) {
                            eventText = "$eventText and invited to: $invitedUsers"
                        } else {
                            eventText = "$eventText invited to: $invitedUsers"
                        }
                    }
                    binding.otherEventText.text = eventText

                    if (item.commentData != null && (!item.commentData.message.isNullOrEmpty() || item.commentData.files.isNotEmpty())) {
                        val marginEndOrStartInPixels =
                            context.resources.getDimensionPixelSize(R.dimen.comment_card_margin)
                        val marginWithZeroPixels =
                            context.resources.getDimensionPixelSize(R.dimen.comment_card_no_margin)

                        val layoutParams =
                            binding.myMsgLayout.layoutParams as ConstraintLayout.LayoutParams

                        if (item.initiator.id == loggedInUserId) {
                            layoutParams.marginStart = marginEndOrStartInPixels
                            layoutParams.marginEnd = marginWithZeroPixels
                        } else {
                            layoutParams.marginStart = marginWithZeroPixels
                            layoutParams.marginEnd = marginEndOrStartInPixels
                        }
                        binding.myMsgLayout.layoutParams = layoutParams

                        if (item.isPinned == true) {
                            binding.eventPinImg.visibility = View.VISIBLE
                        } else {
                            binding.eventPinImg.visibility = View.GONE
                        }

                        binding.onlyImagesRV.visibility = View.GONE
                        binding.imagesWithCommentRV.visibility = View.GONE
                        binding.filesRV.visibility = View.GONE


                        if (!item.commentData.message.isNullOrEmpty()) {
                            binding.onlyComment.text = item.commentData.message.trim()
                            binding.onlyComment.visibility = View.VISIBLE
                        } else {
                            binding.onlyComment.visibility = View.GONE
                        }

                        if (item.commentData.files.isNotEmpty()) {
                            separateFiles(item.commentData.files)
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            if (binding.onlyComment.lineCount > 6) {
                                binding.viewMoreLessLayout.visibility = View.VISIBLE
                                binding.viewMoreBtn.visibility = View.VISIBLE
                                binding.viewLessBtn.visibility = View.GONE
                            } else {
                                binding.viewMoreLessLayout.visibility = View.GONE
                                binding.viewMoreBtn.visibility = View.GONE
                                binding.viewLessBtn.visibility = View.GONE
                            }
                        }, 15)

                    }

                }

                TaskDetailEvents.InvitedUser.eventValue -> {

                    var invitedUsers = ""
                    if (!item.eventData.isNullOrEmpty()) {
                        invitedUsers += item.eventData.map {
                            if (it.firstName.isNullOrEmpty())
                                " ${it.phoneNumber} ;"
                            else
                                " ${it.firstName} ${it.surName} ;"
                        }.toString().removeSurrounding("[", "]").removeSuffix(";").replace(",", "")
                    }
                    if (invitedUsers.isNotEmpty()) {
                        binding.invitedNumbers.text = "Invited: $invitedUsers"
                        binding.invitedNumbers.visibility = View.VISIBLE
                    }

                    if (!item.eventData.isNullOrEmpty() && item.eventData.size > 1) {
                        binding.otherEventText.text =
                            "${creatorName.toCamelCase()} invited $invitedUsers to Ceibro"
                    } else {
                        binding.otherEventText.text =
                            "${creatorName.toCamelCase()} invited $invitedUsers to Ceibro"
                    }

                }

                TaskDetailEvents.Comment.eventValue -> {
                    binding.otherEventText.text = "Comment+tag"

                }

                TaskDetailEvents.CancelTask.eventValue -> {
                    binding.dotMenu.visibility = View.GONE
                    binding.otherEventText.text = "Task canceled by ${creatorName.toCamelCase()}"

                }

                TaskDetailEvents.UnCancelTask.eventValue -> {
                    binding.otherEventText.text = "Task un-canceled by ${creatorName.toCamelCase()}"

                }

                TaskDetailEvents.JoinedTask.eventValue -> {
                    binding.otherEventText.text = "${creatorName.toCamelCase()} joined the task"

                }

                TaskDetailEvents.ReOpen.eventValue -> {
                    binding.otherEventText.text = "${creatorName.toCamelCase()} re-opened the task"

                }

                TaskDetailEvents.DoneTask.eventValue -> {
                    binding.dotMenu.visibility = View.GONE
                    binding.otherEventText.text =
                        "${creatorName.toCamelCase()} marked the task as done"

                    if (item.commentData != null && (!item.commentData.message.isNullOrEmpty() || item.commentData.files.isNotEmpty())) {
                        val marginEndOrStartInPixels =
                            context.resources.getDimensionPixelSize(R.dimen.comment_card_margin)
                        val marginWithZeroPixels =
                            context.resources.getDimensionPixelSize(R.dimen.comment_card_no_margin)

                        val layoutParams =
                            binding.myMsgLayout.layoutParams as ConstraintLayout.LayoutParams

                        if (item.initiator.id == loggedInUserId) {
                            layoutParams.marginStart = marginEndOrStartInPixels
                            layoutParams.marginEnd = marginWithZeroPixels
                        } else {
                            layoutParams.marginStart = marginWithZeroPixels
                            layoutParams.marginEnd = marginEndOrStartInPixels
                        }
                        binding.myMsgLayout.layoutParams = layoutParams

                        if (item.isPinned == true) {
                            binding.eventPinImg.visibility = View.VISIBLE
                        } else {
                            binding.eventPinImg.visibility = View.GONE
                        }

                        binding.onlyImagesRV.visibility = View.GONE
                        binding.imagesWithCommentRV.visibility = View.GONE
                        binding.filesRV.visibility = View.GONE


                        if (!item.commentData.message.isNullOrEmpty()) {
                            binding.onlyComment.text = item.commentData.message.trim()
                            binding.onlyComment.visibility = View.VISIBLE
                        } else {
                            binding.onlyComment.visibility = View.GONE
                        }

                        if (item.commentData.files.isNotEmpty()) {
                            separateFiles(item.commentData.files)
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            if (binding.onlyComment.lineCount > 6) {
                                binding.viewMoreLessLayout.visibility = View.VISIBLE
                                binding.viewMoreBtn.visibility = View.VISIBLE
                                binding.viewLessBtn.visibility = View.GONE
                            } else {
                                binding.viewMoreLessLayout.visibility = View.GONE
                                binding.viewMoreBtn.visibility = View.GONE
                                binding.viewLessBtn.visibility = View.GONE
                            }
                        }, 15)

                    }

                }

                TaskDetailEvents.APPROVED.eventValue -> {

                    binding.otherEventText.text =
                        "${creatorName.toCamelCase()} approved the task"

                    if (item.commentData != null && (!item.commentData.message.isNullOrEmpty() || item.commentData.files.isNotEmpty())) {
                        val marginEndOrStartInPixels =
                            context.resources.getDimensionPixelSize(R.dimen.comment_card_margin)
                        val marginWithZeroPixels =
                            context.resources.getDimensionPixelSize(R.dimen.comment_card_no_margin)

                        val layoutParams =
                            binding.myMsgLayout.layoutParams as ConstraintLayout.LayoutParams

                        if (item.initiator.id == loggedInUserId) {
                            layoutParams.marginStart = marginEndOrStartInPixels
                            layoutParams.marginEnd = marginWithZeroPixels
                        } else {
                            layoutParams.marginStart = marginWithZeroPixels
                            layoutParams.marginEnd = marginEndOrStartInPixels
                        }
                        binding.myMsgLayout.layoutParams = layoutParams

                        if (item.isPinned == true) {
                            binding.eventPinImg.visibility = View.VISIBLE
                        } else {
                            binding.eventPinImg.visibility = View.GONE
                        }

                        binding.onlyImagesRV.visibility = View.GONE
                        binding.imagesWithCommentRV.visibility = View.GONE
                        binding.filesRV.visibility = View.GONE


                        if (!item.commentData.message.isNullOrEmpty()) {
                            binding.onlyComment.text = item.commentData.message.trim()
                            binding.onlyComment.visibility = View.VISIBLE
                        } else {
                            binding.onlyComment.visibility = View.GONE
                        }

                        if (item.commentData.files.isNotEmpty()) {
                            separateFiles(item.commentData.files)
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            if (binding.onlyComment.lineCount > 6) {
                                binding.viewMoreLessLayout.visibility = View.VISIBLE
                                binding.viewMoreBtn.visibility = View.VISIBLE
                                binding.viewLessBtn.visibility = View.GONE
                            } else {
                                binding.viewMoreLessLayout.visibility = View.GONE
                                binding.viewMoreBtn.visibility = View.GONE
                                binding.viewLessBtn.visibility = View.GONE
                            }
                        }, 15)

                    }
                }

                TaskDetailEvents.REJECT_REOPEN.eventValue -> {

                    binding.otherEventText.text =
                        "${creatorName.toCamelCase()} rejected and reopened the task"

                    if (item.commentData != null && (!item.commentData.message.isNullOrEmpty() || item.commentData.files.isNotEmpty())) {
                        val marginEndOrStartInPixels =
                            context.resources.getDimensionPixelSize(R.dimen.comment_card_margin)
                        val marginWithZeroPixels =
                            context.resources.getDimensionPixelSize(R.dimen.comment_card_no_margin)

                        val layoutParams =
                            binding.myMsgLayout.layoutParams as ConstraintLayout.LayoutParams

                        if (item.initiator.id == loggedInUserId) {
                            layoutParams.marginStart = marginEndOrStartInPixels
                            layoutParams.marginEnd = marginWithZeroPixels
                        } else {
                            layoutParams.marginStart = marginWithZeroPixels
                            layoutParams.marginEnd = marginEndOrStartInPixels
                        }
                        binding.myMsgLayout.layoutParams = layoutParams

                        if (item.isPinned == true) {
                            binding.eventPinImg.visibility = View.VISIBLE
                        } else {
                            binding.eventPinImg.visibility = View.GONE
                        }

                        binding.onlyImagesRV.visibility = View.GONE
                        binding.imagesWithCommentRV.visibility = View.GONE
                        binding.filesRV.visibility = View.GONE


                        if (!item.commentData.message.isNullOrEmpty()) {
                            binding.onlyComment.text = item.commentData.message.trim()
                            binding.onlyComment.visibility = View.VISIBLE
                        } else {
                            binding.onlyComment.visibility = View.GONE
                        }

                        if (item.commentData.files.isNotEmpty()) {
                            separateFiles(item.commentData.files)
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            if (binding.onlyComment.lineCount > 6) {
                                binding.viewMoreLessLayout.visibility = View.VISIBLE
                                binding.viewMoreBtn.visibility = View.VISIBLE
                                binding.viewLessBtn.visibility = View.GONE
                            } else {
                                binding.viewMoreLessLayout.visibility = View.GONE
                                binding.viewMoreBtn.visibility = View.GONE
                                binding.viewLessBtn.visibility = View.GONE
                            }
                        }, 15)

                    }
                }

                TaskDetailEvents.REJECT_CLOSE.eventValue -> {

                    binding.otherEventText.text =
                        "${creatorName.toCamelCase()} rejected and closed the task"

                    if (item.commentData != null && (!item.commentData.message.isNullOrEmpty() || item.commentData.files.isNotEmpty())) {
                        val marginEndOrStartInPixels =
                            context.resources.getDimensionPixelSize(R.dimen.comment_card_margin)
                        val marginWithZeroPixels =
                            context.resources.getDimensionPixelSize(R.dimen.comment_card_no_margin)

                        val layoutParams =
                            binding.myMsgLayout.layoutParams as ConstraintLayout.LayoutParams

                        if (item.initiator.id == loggedInUserId) {
                            layoutParams.marginStart = marginEndOrStartInPixels
                            layoutParams.marginEnd = marginWithZeroPixels
                        } else {
                            layoutParams.marginStart = marginWithZeroPixels
                            layoutParams.marginEnd = marginEndOrStartInPixels
                        }
                        binding.myMsgLayout.layoutParams = layoutParams

                        if (item.isPinned == true) {
                            binding.eventPinImg.visibility = View.VISIBLE
                        } else {
                            binding.eventPinImg.visibility = View.GONE
                        }

                        binding.onlyImagesRV.visibility = View.GONE
                        binding.imagesWithCommentRV.visibility = View.GONE
                        binding.filesRV.visibility = View.GONE


                        if (!item.commentData.message.isNullOrEmpty()) {
                            binding.onlyComment.text = item.commentData.message.trim()
                            binding.onlyComment.visibility = View.VISIBLE
                        } else {
                            binding.onlyComment.visibility = View.GONE
                        }

                        if (item.commentData.files.isNotEmpty()) {
                            separateFiles(item.commentData.files)
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            if (binding.onlyComment.lineCount > 6) {
                                binding.viewMoreLessLayout.visibility = View.VISIBLE
                                binding.viewMoreBtn.visibility = View.VISIBLE
                                binding.viewLessBtn.visibility = View.GONE
                            } else {
                                binding.viewMoreLessLayout.visibility = View.GONE
                                binding.viewMoreBtn.visibility = View.GONE
                                binding.viewLessBtn.visibility = View.GONE
                            }
                        }, 15)

                    }
                }
            }


            binding.dotMenu.setOnClickListener {
                createPopupWindow(it, item) { value ->
                    if (value.equals("pin", true)) {
                        pinClickListener?.invoke(absoluteAdapterPosition, item, true)
                    } else if (value.equals("unpin", true)) {
                        pinClickListener?.invoke(absoluteAdapterPosition, item, false)
                    }
                }
            }


            binding.viewMoreBtn.setOnClickListener {
                if (binding.onlyComment.maxLines == 6) {
                    binding.onlyComment.maxLines = Int.MAX_VALUE
                    binding.viewMoreLessLayout.visibility = View.VISIBLE
                    binding.viewMoreBtn.visibility = View.GONE
                    binding.viewLessBtn.visibility = View.VISIBLE
                }
            }

            binding.viewLessBtn.setOnClickListener {
                if (binding.onlyComment.maxLines > 6) {
                    binding.onlyComment.maxLines = 6
                    binding.viewMoreLessLayout.visibility = View.VISIBLE
                    binding.viewMoreBtn.visibility = View.VISIBLE
                    binding.viewLessBtn.visibility = View.GONE
                }
            }
        }

        private fun separateFiles(files: List<EventFiles>) {
            val onlyImage: ArrayList<EventFiles> = arrayListOf()
            val imagesWithComment: ArrayList<EventFiles> = arrayListOf()
            val document: ArrayList<EventFiles> = arrayListOf()

            for (item in files) {
                when (item.fileTag) {
                    AttachmentTags.Image.tagValue -> {
                        onlyImage.add(item)
                    }

                    AttachmentTags.ImageWithComment.tagValue -> {
                        imagesWithComment.add(item)
                    }

                    AttachmentTags.File.tagValue -> {
                        document.add(item)
                    }
                }
            }

            if (onlyImage.isNotEmpty()) {
                val onlyImagesFiles = onlyImage.map {
                    TaskFiles(
                        access = listOf(),
                        comment = it.comment,
                        createdAt = "",
                        fileName = it.fileName,
                        fileTag = it.fileTag,
                        fileType = AttachmentTags.ImageWithComment.tagValue,
                        fileUrl = it.fileUrl,
                        hasComment = it.hasComment,
                        id = it.id,
                        moduleId = it.moduleId,
                        moduleType = it.moduleType,
                        updatedAt = "",
                        uploadStatus = it.uploadStatus,
                        uploadedBy = TaskMemberDetail(
                            firstName = "",
                            surName = "",
                            profilePic = "",
                            id = "",
                            phoneNumber = "",
                            companyName = ""
                        ),
                        v = 0,
                        version = 0
                    )
                }
                val onlyImageAdapter = EventsOnlyImageRVAdapter()
                binding.onlyImagesRV.adapter = onlyImageAdapter
                onlyImageAdapter.setList(onlyImage)
                onlyImageAdapter.openImageClickListener =
                    { view: View, position: Int, fileUrl: String ->
                        openEventImageClickListener?.invoke(view, position, onlyImagesFiles)
                    }
                binding.onlyImagesRV.visibility = View.VISIBLE
            }
            if (imagesWithComment.isNotEmpty()) {
                val imagesWithCommentFiles = imagesWithComment.map {
                    TaskFiles(
                        access = listOf(),
                        comment = it.comment,
                        createdAt = "",
                        fileName = it.fileName,
                        fileTag = it.fileTag,
                        fileType = AttachmentTags.ImageWithComment.tagValue,
                        fileUrl = it.fileUrl,
                        hasComment = it.hasComment,
                        id = it.id,
                        moduleId = it.moduleId,
                        moduleType = it.moduleType,
                        updatedAt = "",
                        uploadStatus = it.uploadStatus,
                        uploadedBy = TaskMemberDetail(
                            firstName = "",
                            surName = "",
                            profilePic = "",
                            id = "",
                            phoneNumber = "",
                            companyName = ""
                        ),
                        v = 0,
                        version = 0
                    )
                }
                val imageWithCommentAdapter = EventsImageWithCommentRVAdapter()
                binding.imagesWithCommentRV.adapter = imageWithCommentAdapter
                imageWithCommentAdapter.setList(imagesWithComment)
                imageWithCommentAdapter.openImageClickListener =
                    { view: View, position: Int, fileUrl: String ->
                        openEventImageClickListener?.invoke(view, position, imagesWithCommentFiles)
                    }
                binding.imagesWithCommentRV.visibility = View.VISIBLE
            }
            if (document.isNotEmpty()) {
                val filesAdapter = EventsFilesRVAdapter(
                    networkConnectivityObserver,
                    context,
                    downloadedDrawingV2Dao
                )

                filesAdapter.fileClickListener =
                    { view: View, position: Int, data: EventFiles, drawingFile ->

                        fileClickListener?.invoke(view, position, data, drawingFile)

                    }
                filesAdapter.requestPermissionCallBack {

                    requestPermissionClickListener?.invoke("")
                }
                filesAdapter.downloadFileCallBack { textView, ivDownload, downloaded, triplet, tag ->
                    downloadFileClickListener?.invoke(
                        textView,
                        ivDownload,
                        downloaded,
                        triplet,
                        tag
                    )
                }
                binding.filesRV.adapter = filesAdapter
                filesAdapter.setList(document)
                binding.filesRV.visibility = View.VISIBLE
            }
        }


        private fun createPopupWindow(
            v: View,
            event: Events,
            callback: (String) -> Unit
        ): PopupWindow {
            val context: Context = v.context
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = inflater.inflate(R.layout.task_detail_comment_menu_dialog, null)

            val popupWindow = PopupWindow(
                view,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                true
            )
            popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            popupWindow.elevation = 13F
            popupWindow.isOutsideTouchable = true


            val replyToComment: TextView = view.findViewById(R.id.replyToComment)
            val pinOrUnpinComment: TextView = view.findViewById(R.id.pinOrUnpinComment)
            if (event.isPinned == true) {
                pinOrUnpinComment.text = context.resources.getString(R.string.unpin_comment)
            } else {
                pinOrUnpinComment.text = context.resources.getString(R.string.pin_comment)
            }

//            replyToComment.setOnClickListener {
//                popupWindow.dismiss()
//                callback.invoke("reply")
//            }

            pinOrUnpinComment.setOnClickListener {
                popupWindow.dismiss()
                if (event.isPinned == true) {
                    callback.invoke("unpin")
                } else {
                    callback.invoke("pin")
                }
            }

            val values = IntArray(2)
            v.getLocationInWindow(values)
            val positionOfIcon = values[1]

            //Get the height of 2/3rd of the height of the screen
            val displayMetrics = context.resources.displayMetrics
            val height = displayMetrics.heightPixels * 2 / 3

            if (positionOfIcon > height) {
//                if (tvDownload.visibility == View.GONE) {
//                    popupWindow.showAsDropDown(v, 0, -295)
//                } else {
//                    popupWindow.showAsDropDown(v, 0, -420)
//                }
                popupWindow.showAsDropDown(v, 0, -180)
            } else {
                popupWindow.showAsDropDown(v, 0, -30)
            }


            return popupWindow
        }

    }

    inner class EventsCeibroTagsViewHolder(private val binding: LayoutCeibroTaskTagsOnlyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Events) {
            val context = binding.otherEventText.context

            binding.rootLayout.setOnClickListener {
                pinnedCommentClickListener?.invoke(item)
            }

            val creatorName =
                if (item.initiator.firstName.isEmpty() && item.initiator.surName.isEmpty()) {
                    "${item.initiator.phoneNumber}"
                } else {
                    "${item.initiator.firstName.trim()} ${item.initiator.surName.trim()}"
                }


            when (item.eventType) {
                TaskDetailEvents.ForwardTask.eventValue -> {

                    var forwardedToUsers = ""
                    if (!item.eventData.isNullOrEmpty()) {
                        forwardedToUsers += item.eventData.map {
                            if (it.firstName.isNullOrEmpty()) {
                                if (it.phoneNumber.isNullOrEmpty()) {
                                    " Unknown User ;"
                                } else {
                                    " ${it.phoneNumber} ;"
                                }
                            } else {
                                " ${it.firstName} ${it.surName} ;"
                            }
                        }
                            .toString().removeSurrounding("[", "]").removeSuffix(";")
                            .replace(",", "")
                    }


                    var invitedUsers = ""
                    if (!item.invitedMembers.isNullOrEmpty()) {
                        invitedUsers += item.invitedMembers.map {
                            if (it.firstName.isNullOrEmpty())
                                " ${it.phoneNumber} ;"
                            else
                                " ${it.firstName} ${it.surName} ;"
                        }.toString().removeSurrounding("[", "]").removeSuffix(";").replace(",", "")
                    }


                    var eventText = "${creatorName.toCamelCase()}"
                    if (forwardedToUsers.isNotEmpty()) {
                        eventText = "$eventText forwarded task to: $forwardedToUsers"
                    }
                    if (invitedUsers.isNotEmpty()) {
                        if (forwardedToUsers.isNotEmpty()) {
                            eventText = "$eventText and invited to: $invitedUsers"
                        } else {
                            eventText = "$eventText invited to: $invitedUsers"
                        }
                    }
                    binding.otherEventText.text = eventText

                }

                TaskDetailEvents.InvitedUser.eventValue -> {

                    var invitedUsers = ""
                    if (!item.eventData.isNullOrEmpty()) {
                        invitedUsers += item.eventData.map {
                            if (it.firstName.isNullOrEmpty())
                                " ${it.phoneNumber} ;"
                            else
                                " ${it.firstName} ${it.surName} ;"
                        }.toString().removeSurrounding("[", "]").removeSuffix(";").replace(",", "")
                    }


                    if (!item.eventData.isNullOrEmpty() && item.eventData.size > 1) {
                        binding.otherEventText.text =
                            "${creatorName.toCamelCase()} invited $invitedUsers to Ceibro"
                    } else {
                        binding.otherEventText.text =
                            "${creatorName.toCamelCase()} invited $invitedUsers to Ceibro"
                    }

                }

                TaskDetailEvents.Comment.eventValue -> {
                    binding.otherEventText.text = "Comment"

                }

                TaskDetailEvents.CancelTask.eventValue -> {
                    binding.otherEventText.text = "Task canceled by ${creatorName.toCamelCase()}"

                }

                TaskDetailEvents.UnCancelTask.eventValue -> {
                    binding.otherEventText.text = "Task un-canceled by ${creatorName.toCamelCase()}"

                }

                TaskDetailEvents.JoinedTask.eventValue -> {
                    binding.otherEventText.text = "${creatorName.toCamelCase()} joined the task"

                }

                TaskDetailEvents.ReOpen.eventValue -> {
                    binding.otherEventText.text = "${creatorName.toCamelCase()} re-opened the task"

                }

                TaskDetailEvents.DoneTask.eventValue -> {
                    binding.otherEventText.text =
                        "${creatorName.toCamelCase()} marked the task as done"

                }

                TaskDetailEvents.APPROVED.eventValue -> {
                    binding.otherEventText.text =
                        "${creatorName.toCamelCase()} approved the task"

                }

                TaskDetailEvents.REJECT_REOPEN.eventValue -> {
                    binding.otherEventText.text =
                        "${creatorName.toCamelCase()} rejected and reopened the task"
                }

                TaskDetailEvents.REJECT_CLOSE.eventValue -> {
                    binding.otherEventText.text =
                        "${creatorName.toCamelCase()} rejected and closed the task"
                }
            }

        }
    }

    inner class EventsCeibroChatViewHolder(private val binding: LayoutCeibroEventChatOnlyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Events) {
            binding.rootLayout.setOnClickListener {
                pinnedCommentClickListener?.invoke(item)
            }

            val context = binding.eventPinImg.context
            binding.onlyComment.text = ""
            binding.onlyComment.visibility = View.GONE
            binding.onlyImagesRV.visibility = View.GONE
            binding.imagesWithCommentRV.visibility = View.GONE
            binding.forwardedToNames.text = ""
            binding.forwardedToNames.visibility = View.GONE
            binding.invitedNumbers.visibility = View.GONE
            binding.eventPinImg.visibility = View.GONE
            binding.viewMoreLessLayout.visibility = View.GONE
            binding.viewMoreBtn.visibility = View.GONE
            binding.viewLessBtn.visibility = View.GONE


            val creatorName =
                if (item.initiator.firstName.isEmpty() && item.initiator.surName.isEmpty()) {
                    "${item.initiator.phoneNumber}"
                } else {
                    "${item.initiator.firstName.trim()} ${item.initiator.surName.trim()}"
                }
            binding.eventBy.text = creatorName

            if (!item.initiator.profilePic.isNullOrEmpty()) {
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
                    .load(item.initiator.profilePic)
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
                    .into(binding.creatorImg)
            }

            binding.eventDate.text = DateUtils.formatCreationUTCTimeToCustomForDetailFiles(
                utcTime = item.createdAt,
                inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
            )


            val tintColor = if (!item.eventSeenBy.isNullOrEmpty()) {
                if (item.eventSeenBy?.contains(loggedInUserId) == true) {
                    context.resources.getColor(R.color.appBlue)
                } else {
                    context.resources.getColor(R.color.appGrey3)
                }
            } else {
                context.resources.getColor(R.color.appGrey3)
            }
            binding.seenImg.setColorFilter(tintColor)



            when (item.eventType) {
                TaskDetailEvents.Comment.eventValue -> {
                    binding.myMsgLayout.visibility = View.VISIBLE

                    val marginEndOrStartInPixels =
                        context.resources.getDimensionPixelSize(R.dimen.comment_card_margin)
                    val marginWithZeroPixels =
                        context.resources.getDimensionPixelSize(R.dimen.comment_card_no_margin)

                    val layoutParams =
                        binding.myMsgLayout.layoutParams as ConstraintLayout.LayoutParams

                    if (item.initiator.id == loggedInUserId) {
                        layoutParams.marginStart = marginEndOrStartInPixels
                        layoutParams.marginEnd = marginWithZeroPixels
                    } else {
                        layoutParams.marginStart = marginWithZeroPixels
                        layoutParams.marginEnd = marginEndOrStartInPixels
                    }
                    binding.myMsgLayout.layoutParams = layoutParams

                    if (item.isPinned == true) {
                        binding.eventPinImg.visibility = View.VISIBLE
                    } else {
                        binding.eventPinImg.visibility = View.GONE
                    }

                    binding.onlyImagesRV.visibility = View.GONE
                    binding.imagesWithCommentRV.visibility = View.GONE
                    binding.filesRV.visibility = View.GONE


                    if (item.commentData != null) {

                        if (!item.commentData.message.isNullOrEmpty()) {
                            binding.onlyComment.text = item.commentData.message.trim()
                            binding.onlyComment.visibility = View.VISIBLE
                        } else {
                            binding.onlyComment.visibility = View.GONE
                        }

                        if (item.commentData.files.isNotEmpty()) {
                            separateFiles(item.commentData.files)
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            if (binding.onlyComment.lineCount > 6) {
                                binding.viewMoreLessLayout.visibility = View.VISIBLE
                                binding.viewMoreBtn.visibility = View.VISIBLE
                                binding.viewLessBtn.visibility = View.GONE
                            } else {
                                binding.viewMoreLessLayout.visibility = View.GONE
                                binding.viewMoreBtn.visibility = View.GONE
                                binding.viewLessBtn.visibility = View.GONE
                            }
                        }, 15)
                    }
                }


            }


            binding.dotMenu.setOnClickListener {
                createPopupWindow(it, item) { value ->
                    if (value.equals("pin", true)) {
                        pinClickListener?.invoke(absoluteAdapterPosition, item, true)
                    } else if (value.equals("unpin", true)) {
                        pinClickListener?.invoke(absoluteAdapterPosition, item, false)
                    }
                }
            }


            binding.viewMoreBtn.setOnClickListener {
                if (binding.onlyComment.maxLines == 6) {
                    binding.onlyComment.maxLines = Int.MAX_VALUE
                    binding.viewMoreLessLayout.visibility = View.VISIBLE
                    binding.viewMoreBtn.visibility = View.GONE
                    binding.viewLessBtn.visibility = View.VISIBLE
                }
            }

            binding.viewLessBtn.setOnClickListener {
                if (binding.onlyComment.maxLines > 6) {
                    binding.onlyComment.maxLines = 6
                    binding.viewMoreLessLayout.visibility = View.VISIBLE
                    binding.viewMoreBtn.visibility = View.VISIBLE
                    binding.viewLessBtn.visibility = View.GONE
                }
            }
        }

        private fun separateFiles(files: List<EventFiles>) {
            val onlyImage: ArrayList<EventFiles> = arrayListOf()
            val imagesWithComment: ArrayList<EventFiles> = arrayListOf()
            val document: ArrayList<EventFiles> = arrayListOf()

            for (item in files) {
                when (item.fileTag) {
                    AttachmentTags.Image.tagValue -> {
                        onlyImage.add(item)
                    }

                    AttachmentTags.ImageWithComment.tagValue -> {
                        imagesWithComment.add(item)
                    }

                    AttachmentTags.File.tagValue -> {
                        document.add(item)
                    }
                }
            }

            if (onlyImage.isNotEmpty()) {
                val onlyImagesFiles = onlyImage.map {
                    TaskFiles(
                        access = listOf(),
                        comment = it.comment,
                        createdAt = "",
                        fileName = it.fileName,
                        fileTag = it.fileTag,
                        fileType = AttachmentTags.ImageWithComment.tagValue,
                        fileUrl = it.fileUrl,
                        hasComment = it.hasComment,
                        id = it.id,
                        moduleId = it.moduleId,
                        moduleType = it.moduleType,
                        updatedAt = "",
                        uploadStatus = it.uploadStatus,
                        uploadedBy = TaskMemberDetail(
                            firstName = "",
                            surName = "",
                            profilePic = "",
                            id = "",
                            phoneNumber = "",
                            companyName = ""
                        ),
                        v = 0,
                        version = 0
                    )
                }
                val onlyImageAdapter = EventsOnlyImageRVAdapter()
                binding.onlyImagesRV.adapter = onlyImageAdapter
                onlyImageAdapter.setList(onlyImage)
                onlyImageAdapter.openImageClickListener =
                    { view: View, position: Int, fileUrl: String ->
                        openEventImageClickListener?.invoke(view, position, onlyImagesFiles)
                    }
                binding.onlyImagesRV.visibility = View.VISIBLE
            }
            if (imagesWithComment.isNotEmpty()) {
                val imagesWithCommentFiles = imagesWithComment.map {
                    TaskFiles(
                        access = listOf(),
                        comment = it.comment,
                        createdAt = "",
                        fileName = it.fileName,
                        fileTag = it.fileTag,
                        fileType = AttachmentTags.ImageWithComment.tagValue,
                        fileUrl = it.fileUrl,
                        hasComment = it.hasComment,
                        id = it.id,
                        moduleId = it.moduleId,
                        moduleType = it.moduleType,
                        updatedAt = "",
                        uploadStatus = it.uploadStatus,
                        uploadedBy = TaskMemberDetail(
                            firstName = "",
                            surName = "",
                            profilePic = "",
                            id = "",
                            phoneNumber = "",
                            companyName = ""
                        ),
                        v = 0,
                        version = 0
                    )
                }
                val imageWithCommentAdapter = EventsImageWithCommentRVAdapter()
                binding.imagesWithCommentRV.adapter = imageWithCommentAdapter
                imageWithCommentAdapter.setList(imagesWithComment)
                imageWithCommentAdapter.openImageClickListener =
                    { view: View, position: Int, fileUrl: String ->
                        openEventImageClickListener?.invoke(view, position, imagesWithCommentFiles)
                    }
                binding.imagesWithCommentRV.visibility = View.VISIBLE
            }
            if (document.isNotEmpty()) {
                val filesAdapter = EventsFilesRVAdapter(
                    networkConnectivityObserver,
                    context,
                    downloadedDrawingV2Dao
                )

                filesAdapter.fileClickListener =
                    { view: View, position: Int, data: EventFiles, drawingFile ->

                        fileClickListener?.invoke(view, position, data, drawingFile)

                    }
                filesAdapter.requestPermissionCallBack {

                    requestPermissionClickListener?.invoke("")
                }
                filesAdapter.downloadFileCallBack { textView, ivDownload, downloaded, triplet, tag ->
                    downloadFileClickListener?.invoke(
                        textView,
                        ivDownload,
                        downloaded,
                        triplet,
                        tag
                    )
                }
                binding.filesRV.adapter = filesAdapter
                filesAdapter.setList(document)
                binding.filesRV.visibility = View.VISIBLE
            }
        }


        private fun createPopupWindow(
            v: View,
            event: Events,
            callback: (String) -> Unit
        ): PopupWindow {
            val context: Context = v.context
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = inflater.inflate(R.layout.task_detail_comment_menu_dialog, null)

            val popupWindow = PopupWindow(
                view,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                true
            )
            popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            popupWindow.elevation = 13F
            popupWindow.isOutsideTouchable = true


            val replyToComment: TextView = view.findViewById(R.id.replyToComment)
            val pinOrUnpinComment: TextView = view.findViewById(R.id.pinOrUnpinComment)
            if (event.isPinned == true) {
                pinOrUnpinComment.text = context.resources.getString(R.string.unpin_comment)
            } else {
                pinOrUnpinComment.text = context.resources.getString(R.string.pin_comment)
            }

//            replyToComment.setOnClickListener {
//                popupWindow.dismiss()
//                callback.invoke("reply")
//            }

            pinOrUnpinComment.setOnClickListener {
                popupWindow.dismiss()
                if (event.isPinned == true) {
                    callback.invoke("unpin")
                } else {
                    callback.invoke("pin")
                }
            }

            val values = IntArray(2)
            v.getLocationInWindow(values)
            val positionOfIcon = values[1]

            //Get the height of 2/3rd of the height of the screen
            val displayMetrics = context.resources.displayMetrics
            val height = displayMetrics.heightPixels * 2 / 3

            if (positionOfIcon > height) {
//                if (tvDownload.visibility == View.GONE) {
//                    popupWindow.showAsDropDown(v, 0, -295)
//                } else {
//                    popupWindow.showAsDropDown(v, 0, -420)
//                }
                popupWindow.showAsDropDown(v, 0, -180)
            } else {
                popupWindow.showAsDropDown(v, 0, -30)
            }


            return popupWindow
        }

    }
}