package com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.EventFiles
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.databinding.LayoutCeibroTaskEventsBinding
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class EventsRVAdapter @Inject constructor() :
    RecyclerView.Adapter<EventsRVAdapter.EventsViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: Events) -> Unit)? =
        null
    var listItems: MutableList<Events> = mutableListOf()

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

    fun setList(list: List<Events>) {
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

            binding.eventName.text = ""
            binding.eventBy.text = "${item.initiator.firstName} ${item.initiator.surName}"
            binding.eventDate.text = DateUtils.reformatStringDate(
                date = item.createdAt,
                DateUtils.SERVER_DATE_FULL_FORMAT,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DAY
            )


            when (item.eventType) {
                TaskDetailEvents.ForwardTask.eventValue -> {
                    binding.onlyComment.visibility = View.GONE
                    binding.onlyImagesRV.visibility = View.GONE
                    binding.imagesWithCommentRV.visibility = View.GONE
                    binding.forwardedToNames.visibility = View.VISIBLE

                    binding.eventName.text = context.resources.getString(R.string.forwarded_by)

                    var forwardedToUsers = "To: "
                    if (!item.eventData.isNullOrEmpty()) {
                        forwardedToUsers += item.eventData.map {
                            if (it.firstName.isNullOrEmpty())
                                " ${it.phoneNumber} ;"
                            else
                                " ${it.firstName} ${it.surName} ;"
                        }
                            .toString().removeSurrounding("[", "]")
                        forwardedToUsers = forwardedToUsers.removeSuffix(";").replace(",", "")
                    }
                    binding.forwardedToNames.text = forwardedToUsers

                }
                TaskDetailEvents.InvitedUser.eventValue -> {
                    binding.onlyComment.visibility = View.GONE
                    binding.onlyImagesRV.visibility = View.GONE
                    binding.imagesWithCommentRV.visibility = View.GONE
                    binding.forwardedToNames.visibility = View.VISIBLE

                    binding.eventName.text = context.resources.getString(R.string.invited_by)

                    var invitedUsers = "To: "
                    if (!item.eventData.isNullOrEmpty()) {
                        invitedUsers += item.eventData.map {
                            if (it.firstName.isNullOrEmpty())
                                " ${it.phoneNumber} ;"
                            else
                                " ${it.firstName} ${it.surName} ;"
                        }.toString().removeSurrounding("[", "]")
                        invitedUsers = invitedUsers.removeSuffix(";").replace(",", "")
                    }
                    binding.forwardedToNames.text = invitedUsers
                }
                TaskDetailEvents.Comment.eventValue -> {
                    binding.onlyComment.visibility = View.GONE
                    binding.onlyImagesRV.visibility = View.GONE
                    binding.imagesWithCommentRV.visibility = View.GONE
                    binding.filesRV.visibility = View.GONE
                    binding.forwardedToNames.visibility = View.GONE

                    binding.eventName.text = context.resources.getString(R.string.comment_by)

                    if (item.commentData != null) {

                        if (item.commentData.message.isNotEmpty()) {
                            binding.onlyComment.text = item.commentData.message
                            binding.onlyComment.visibility = View.VISIBLE
                        } else {
                            binding.onlyComment.visibility = View.GONE
                        }

                        if (item.commentData.files.isNotEmpty()) {
                            separateFiles(item.commentData.files)
                        }
                    }
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
                val onlyImageAdapter = EventsOnlyImageRVAdapter()
                binding.onlyImagesRV.adapter = onlyImageAdapter
                onlyImageAdapter.setList(onlyImage)
                binding.onlyImagesRV.visibility = View.VISIBLE
            }
            if (imagesWithComment.isNotEmpty()) {
                val imageWithCommentAdapter = EventsImageWithCommentRVAdapter()
                binding.imagesWithCommentRV.adapter = imageWithCommentAdapter
                imageWithCommentAdapter.setList(imagesWithComment)
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