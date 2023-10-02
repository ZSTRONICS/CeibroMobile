package com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.EventFiles
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.databinding.LayoutCeibroTaskEventsBinding
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class EventsRVAdapter @Inject constructor() :
    RecyclerView.Adapter<EventsRVAdapter.EventsViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: Events) -> Unit)? =
        null
    var openEventImageClickListener: ((view: View, position: Int, imageFiles: List<TaskFiles>) -> Unit)? =
        null
    var listItems: MutableList<Events> = mutableListOf()
    var loggedInUserId: String = ""

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

            val context = binding.eventName.context
            binding.onlyComment.visibility = View.GONE
            binding.onlyImagesRV.visibility = View.GONE
            binding.imagesWithCommentRV.visibility = View.GONE
            binding.forwardedToNames.visibility = View.GONE
            binding.invitedNumbers.visibility = View.GONE

            binding.eventName.text = ""
            binding.eventBy.text =
                if (item.initiator.firstName.isEmpty() && item.initiator.surName.isEmpty()) {
                    "${item.initiator.phoneNumber}"
                } else {
                    "${item.initiator.firstName.trim()} ${item.initiator.surName.trim()}"
                }
            binding.eventDate.text = DateUtils.formatCreationUTCTimeToCustom(
                utcTime = item.createdAt,
                inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
            )


            when (item.eventType) {
                TaskDetailEvents.ForwardTask.eventValue -> {
                    binding.onlyComment.visibility = View.GONE
                    binding.onlyImagesRV.visibility = View.GONE
                    binding.imagesWithCommentRV.visibility = View.GONE

                    if (item.initiator.id == loggedInUserId) {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_grey)
                    } else {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_blue)
                    }


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


                    if (item.commentData != null) {
                        if (!item.commentData.message.isNullOrEmpty()) {
                            binding.onlyComment.text = item.commentData.message.trim()
                            binding.onlyComment.visibility = View.VISIBLE
                        } else {
                            binding.onlyComment.visibility = View.GONE
                        }
                    }

                }

                TaskDetailEvents.InvitedUser.eventValue -> {
                    if (item.initiator.id == loggedInUserId) {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_grey)
                    } else {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_blue)
                    }
                    binding.onlyComment.visibility = View.GONE
                    binding.onlyImagesRV.visibility = View.GONE
                    binding.imagesWithCommentRV.visibility = View.GONE
                    binding.forwardedToNames.visibility = View.GONE

                    binding.eventName.text = context.resources.getString(R.string.invited_by)

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

                    if (item.commentData != null) {
                        if (!item.commentData.message.isNullOrEmpty()) {
                            binding.onlyComment.text = item.commentData.message.trim()
                            binding.onlyComment.visibility = View.VISIBLE
                        } else {
                            binding.onlyComment.visibility = View.GONE
                        }
                    }
                }

                TaskDetailEvents.Comment.eventValue -> {
                    binding.onlyComment.visibility = View.GONE
                    binding.onlyImagesRV.visibility = View.GONE
                    binding.imagesWithCommentRV.visibility = View.GONE
                    binding.filesRV.visibility = View.GONE
                    binding.forwardedToNames.visibility = View.GONE

                    binding.eventName.text = context.resources.getString(R.string.comment_by)

                    binding.eventName.visibility = View.GONE

                    if (item.initiator.id == loggedInUserId) {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_grey)
                    } else {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_blue)
                    }

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
                    }
                }

                TaskDetailEvents.CancelTask.eventValue -> {
                    if (item.initiator.id == loggedInUserId) {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_grey)
                    } else {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_blue)
                    }
                    binding.onlyComment.visibility = View.GONE
                    binding.onlyImagesRV.visibility = View.GONE
                    binding.imagesWithCommentRV.visibility = View.GONE
                    binding.filesRV.visibility = View.GONE
                    binding.forwardedToNames.visibility = View.GONE

                    binding.eventName.text = context.resources.getString(R.string.canceled_by)
                    binding.onlyComment.text =
                        context.resources.getString(R.string.task_has_been_canceled)
                    binding.onlyComment.visibility = View.VISIBLE
                }

                TaskDetailEvents.UnCancelTask.eventValue -> {
                    if (item.initiator.id == loggedInUserId) {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_grey)
                    } else {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_blue)
                    }
                    binding.onlyComment.visibility = View.GONE
                    binding.onlyImagesRV.visibility = View.GONE
                    binding.imagesWithCommentRV.visibility = View.GONE
                    binding.filesRV.visibility = View.GONE
                    binding.forwardedToNames.visibility = View.GONE

                    binding.eventName.text = context.resources.getString(R.string.un_canceled_by)
                    binding.onlyComment.text =
                        context.resources.getString(R.string.task_has_been_un_canceled)
                    binding.onlyComment.visibility = View.VISIBLE
                }

                TaskDetailEvents.JoinedTask.eventValue -> {
                    if (item.initiator.id == loggedInUserId) {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_grey)
                    } else {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_blue)
                    }
                    binding.onlyComment.visibility = View.GONE
                    binding.onlyImagesRV.visibility = View.GONE
                    binding.imagesWithCommentRV.visibility = View.GONE
                    binding.filesRV.visibility = View.GONE
                    binding.forwardedToNames.visibility = View.GONE

                    binding.eventName.text = context.resources.getString(R.string.joined_by)
                }

                TaskDetailEvents.DoneTask.eventValue -> {
                    if (item.initiator.id == loggedInUserId) {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_grey)
                    } else {
                        binding.mainLayout.setBackgroundResource(R.drawable.round_blue)
                    }
                    binding.onlyComment.visibility = View.GONE
                    binding.onlyImagesRV.visibility = View.GONE
                    binding.imagesWithCommentRV.visibility = View.GONE
                    binding.filesRV.visibility = View.GONE
                    binding.forwardedToNames.visibility = View.GONE

                    binding.eventName.text = context.resources.getString(R.string.done_by)
                    if (item.commentData != null) {

                        if (!item.commentData.message.isNullOrEmpty()) {
                            binding.onlyComment.text = item.commentData.message.trim()
                            binding.onlyComment.visibility = View.VISIBLE
                        } else {
                            binding.onlyComment.text =
                                context.resources.getString(R.string.task_has_been_closed)
                            binding.onlyComment.visibility = View.VISIBLE
                        }

                        if (item.commentData.files.isNotEmpty()) {
                            separateFiles(item.commentData.files)
                        }
                    } else {
                        binding.onlyComment.text =
                            context.resources.getString(R.string.task_has_been_closed)
                        binding.onlyComment.visibility = View.VISIBLE
                    }
//                    binding.root.setBackgroundColor(context.resources.getColor(R.color.appMidGreen))
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
                            phoneNumber = ""
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
                            phoneNumber = ""
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
                val filesAdapter = EventsFilesRVAdapter()
                binding.filesRV.adapter = filesAdapter
                filesAdapter.setList(document)
                binding.filesRV.visibility = View.VISIBLE
            }
        }
    }
}