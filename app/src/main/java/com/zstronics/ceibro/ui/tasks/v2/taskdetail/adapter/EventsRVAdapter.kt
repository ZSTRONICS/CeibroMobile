package com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
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
import com.zstronics.ceibro.databinding.LayoutCeibroTaskEventsBinding
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class EventsRVAdapter constructor(
    val networkConnectivityObserver: NetworkConnectivityObserver,
    val context: Context,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao
) :
    RecyclerView.Adapter<EventsRVAdapter.EventsViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: Events) -> Unit)? =
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


    var downloadFileClickListener: ((textView: TextView, ivDownload: AppCompatImageView, downloaded: AppCompatImageView, triplet: Triple<String,String,String>, tag: String) -> Unit)? =
        null

    fun downloadFileCallBack(itemClickListener: ((textView: TextView, ivDownload: AppCompatImageView, downloaded: AppCompatImageView, triplet: Triple<String,String,String>, tag: String) -> Unit)?) {
        this.downloadFileClickListener = itemClickListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventsViewHolder {
        return EventsViewHolder(
            LayoutCeibroTaskEventsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        holder.bind(listItems[position])
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

    inner class EventsViewHolder(private val binding: LayoutCeibroTaskEventsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Events) {
//            binding.clearIcon.setOnClickListener {
//                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
//            }

            val context = binding.eventImg.context
            binding.onlyComment.text = ""
            binding.onlyComment.visibility = View.GONE
            binding.onlyImagesRV.visibility = View.GONE
            binding.imagesWithCommentRV.visibility = View.GONE
            binding.forwardedToNames.text = ""
            binding.forwardedToNames.visibility = View.GONE
            binding.invitedNumbers.visibility = View.GONE
            binding.eventImg.visibility = View.GONE
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
            binding.eventDate.text = DateUtils.formatCreationUTCTimeToCustom(
                utcTime = item.createdAt,
                inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
            )

            var isCreator = false
            if (item.initiator.id == loggedInUserId) {
//                binding.mainLayout.setBackgroundResource(R.drawable.round_grey)
                isCreator = true
            } else {
//                binding.mainLayout.setBackgroundResource(R.drawable.round_blue)
                isCreator = false
            }

            when (item.eventType) {
                TaskDetailEvents.ForwardTask.eventValue -> {
                    binding.myMsgLayout.visibility = View.GONE

//                    binding.onlyImagesRV.visibility = View.GONE
//                    binding.imagesWithCommentRV.visibility = View.GONE
//
//                    binding.eventImg.setImageResource(R.drawable.icon_forward)
//                    binding.eventImg.visibility = View.VISIBLE

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
                    binding.otherEventLayout.visibility = View.VISIBLE

                    binding.onlyComment.text = ""
//                    if (item.commentData != null) {
//                        if (!item.commentData.message.isNullOrEmpty()) {
//                            binding.onlyComment.text = item.commentData.message.trim()
//                            binding.onlyComment.visibility = View.VISIBLE
//                        } else {
//                            binding.onlyComment.text = ""
//                            binding.onlyComment.visibility = View.GONE
//                        }
//                    }
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
                    }, 10)

                }

                TaskDetailEvents.InvitedUser.eventValue -> {
                    binding.myMsgLayout.visibility = View.GONE


//                    binding.onlyImagesRV.visibility = View.GONE
//                    binding.imagesWithCommentRV.visibility = View.GONE
//
//                    binding.eventImg.setImageResource(R.drawable.icon_forward)
//                    binding.eventImg.visibility = View.VISIBLE

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
                            "$invitedUsers are invited by ${creatorName.toCamelCase()}"
                    } else {
                        binding.otherEventText.text =
                            "$invitedUsers is invited by ${creatorName.toCamelCase()}"
                    }
                    binding.otherEventLayout.visibility = View.VISIBLE

                    binding.onlyComment.text = ""

//                    if (item.commentData != null) {
//                        if (!item.commentData.message.isNullOrEmpty()) {
//                            binding.onlyComment.text = item.commentData.message.trim()
//                            binding.onlyComment.visibility = View.VISIBLE
//                        } else {
//                            binding.onlyComment.text = ""
//                            binding.onlyComment.visibility = View.GONE
//                        }
//                    }
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
                    }, 10)
                }

                TaskDetailEvents.Comment.eventValue -> {
                    binding.otherEventText.text = ""
                    binding.otherEventLayout.visibility = View.GONE
                    binding.myMsgLayout.visibility = View.VISIBLE


                    binding.onlyImagesRV.visibility = View.GONE
                    binding.imagesWithCommentRV.visibility = View.GONE
                    binding.filesRV.visibility = View.GONE

                    binding.eventImg.setImageResource(R.drawable.icon_reply)
                    binding.eventImg.visibility = View.VISIBLE

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
                        }, 10)
                    }
                }

                TaskDetailEvents.CancelTask.eventValue -> {
                    binding.myMsgLayout.visibility = View.GONE

                    binding.otherEventText.text = "${creatorName.toCamelCase()} canceled the task"
                    binding.otherEventLayout.visibility = View.VISIBLE

//                    binding.onlyImagesRV.visibility = View.GONE
//                    binding.imagesWithCommentRV.visibility = View.GONE
//                    binding.filesRV.visibility = View.GONE
//
//                    binding.eventImg.setImageResource(R.drawable.icon_canceled_task)
//                    binding.eventImg.visibility = View.VISIBLE
//
//                    binding.onlyComment.text =
//                        context.resources.getString(R.string.task_has_been_canceled)
//                    binding.onlyComment.visibility = View.VISIBLE
                }

                TaskDetailEvents.UnCancelTask.eventValue -> {
                    binding.myMsgLayout.visibility = View.GONE

                    binding.otherEventText.text = "${creatorName.toCamelCase()} un-canceled the task"
                    binding.otherEventLayout.visibility = View.VISIBLE

//                    binding.onlyImagesRV.visibility = View.GONE
//                    binding.imagesWithCommentRV.visibility = View.GONE
//                    binding.filesRV.visibility = View.GONE
//
//                    binding.onlyComment.text =
//                        context.resources.getString(R.string.task_has_been_un_canceled)
//                    binding.onlyComment.visibility = View.VISIBLE
                }

                TaskDetailEvents.JoinedTask.eventValue -> {
                    binding.myMsgLayout.visibility = View.GONE

                    binding.otherEventText.text = "${creatorName.toCamelCase()} joined the task"
                    binding.otherEventLayout.visibility = View.VISIBLE

//                    binding.onlyImagesRV.visibility = View.GONE
//                    binding.imagesWithCommentRV.visibility = View.GONE
//                    binding.filesRV.visibility = View.GONE
//
//                    binding.eventImg.setImageResource(R.drawable.icon_person_add)
//                    binding.eventImg.visibility = View.VISIBLE
//
//                    binding.onlyComment.text =
//                        context.resources.getString(R.string.joined_the_task)
//                    binding.onlyComment.visibility = View.VISIBLE
                }

                TaskDetailEvents.DoneTask.eventValue -> {
                    binding.myMsgLayout.visibility = View.GONE

                    binding.otherEventText.text = "${creatorName.toCamelCase()} marked the task as done"
                    binding.otherEventLayout.visibility = View.VISIBLE

//                    binding.mainLayout.setBackgroundResource(R.drawable.round_green)
//
//                    binding.eventImg.setImageResource(R.drawable.icon_tick_mark)
//                    binding.eventImg.visibility = View.VISIBLE
//
//                    binding.onlyImagesRV.visibility = View.GONE
//                    binding.imagesWithCommentRV.visibility = View.GONE
//                    binding.filesRV.visibility = View.GONE
//
//                    if (item.commentData != null) {
//
//                        if (!item.commentData.message.isNullOrEmpty()) {
//                            binding.onlyComment.text = item.commentData.message.trim()
//                            binding.onlyComment.visibility = View.VISIBLE
//                        } else {
//                            binding.onlyComment.text =
//                                context.resources.getString(R.string.marked_the_task_as_done)
//                            binding.onlyComment.visibility = View.VISIBLE
//                        }
//
//                        if (item.commentData.files.isNotEmpty()) {
//                            separateFiles(item.commentData.files)
//                        }
//                    } else {
//                        binding.onlyComment.text =
//                            context.resources.getString(R.string.marked_the_task_as_done)
//                        binding.onlyComment.visibility = View.VISIBLE
//                    }
                    binding.onlyComment.text = ""
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
                    }, 10)
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
                val filesAdapter = EventsFilesRVAdapter(networkConnectivityObserver, context, downloadedDrawingV2Dao)

                filesAdapter.fileClickListener = { view: View, position: Int, data: EventFiles,drawingFile ->

                    fileClickListener?.invoke(view, position, data,drawingFile)

                }
                filesAdapter.requestPermissionCallBack {

                    requestPermissionClickListener?.invoke("")
                }
                filesAdapter.downloadFileCallBack { textView, ivDownload, downloaded, triplet, tag ->
                    downloadFileClickListener?.invoke(textView,ivDownload,downloaded,triplet,tag)
                }
                binding.filesRV.adapter = filesAdapter
                filesAdapter.setList(document)
                binding.filesRV.visibility = View.VISIBLE
            }
        }
    }
}