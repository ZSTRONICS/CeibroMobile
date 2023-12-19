package com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.EventFiles
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.LayoutCeibroTaskDetailBinding
import com.zstronics.ceibro.databinding.LayoutCeibroTaskDetailEventsBinding
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class TaskDetailV2RVAdapter @Inject constructor() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: Events) -> Unit)? =
        null
    var openEventImageClickListener: ((position: Int, bundle: Bundle) -> Unit)? =
        null

    var fileViewerClickListener: ((position: Int, bundle: Bundle) -> Unit)? =
        null
    var descriptionExpendedListener: ((descriptionExpanded: Boolean) -> Unit)? =
        null
    var listItems: MutableList<Any> = mutableListOf()


    var loggedInUserId: String = ""
    var rootState: String = ""
    var selectedState: String = ""
    var descriptionExpanded = false

    companion object {
        private const val VIEW_TYPE_SINGLE_ITEM = 1
        private const val VIEW_TYPE_MULTI_ITEM = 2
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SINGLE_ITEM -> {
                SingleItemViewHolder(
                    LayoutCeibroTaskDetailBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            VIEW_TYPE_MULTI_ITEM -> {
                MultiItemViewHolder(
                    LayoutCeibroTaskDetailEventsBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_SINGLE_ITEM -> {
                val singleItemViewHolder = holder as SingleItemViewHolder
                singleItemViewHolder.bind(listItems[position] as CeibroTaskV2)
            }

            VIEW_TYPE_MULTI_ITEM -> {
                val multiItemViewHolder = holder as MultiItemViewHolder
                multiItemViewHolder.bind(listItems[position] as MutableList<*>)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (listItems[position]) {
            is CeibroTaskV2 -> VIEW_TYPE_SINGLE_ITEM
            is MutableList<*> -> VIEW_TYPE_MULTI_ITEM
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setTaskAndEventList(
        listItems: MutableList<Any>,
        userId: String,
        descriptionExpanded: Boolean
    ) {
        this.loggedInUserId = userId
        this.descriptionExpanded = descriptionExpanded
        this.listItems.clear()
        this.listItems.addAll(listItems)
        notifyDataSetChanged()
    }

    fun updateTaskAndEventList(
        listItems: MutableList<Any>,
        userId: String,
        descriptionExpanded: Boolean
    ) {
        this.loggedInUserId = userId
        this.descriptionExpanded = descriptionExpanded
        this.listItems.clear()
        this.listItems.addAll(listItems)
        notifyDataSetChanged()
    }

    fun setOtherData(rootState: String, selectedState: String, descriptionExpanded: Boolean) {
        this.rootState = rootState
        this.selectedState = selectedState
        this.descriptionExpanded = descriptionExpanded
    }

    fun updateTaskData(task: CeibroTaskV2, descriptionExpanded: Boolean) {
        this.descriptionExpanded = descriptionExpanded
        this.listItems[0] = task
        notifyDataSetChanged()
    }

    inner class SingleItemViewHolder(private val binding: LayoutCeibroTaskDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: CeibroTaskV2) {
            binding.viewMoreLessLayout.visibility = View.GONE
            binding.filesLayout.visibility = View.GONE
            binding.onlyImagesRV.visibility = View.GONE
            binding.imagesWithCommentRV.visibility = View.GONE

            binding.onlyImagesRV.isNestedScrollingEnabled = false
            binding.imagesWithCommentRV.isNestedScrollingEnabled = false

            binding.filesRV.isNestedScrollingEnabled = false

            val context = binding.taskDetailStatusName.context
            var state = ""
            state =
                if (rootState == TaskRootStateTags.FromMe.tagValue && loggedInUserId == task.creator.id) {
                    task.creatorState
                } else if (rootState == TaskRootStateTags.Hidden.tagValue && selectedState.equals(
                        TaskStatus.CANCELED.name,
                        true
                    )
                ) {
                    task.creatorState
                } else {
                    task.assignedToState.find { it.userId == loggedInUserId }?.state ?: ""
                }
            val taskStatusNameBg: Pair<Int, String> = when (state.uppercase()) {
                TaskStatus.NEW.name -> Pair(
                    R.drawable.status_new_filled_more_corners,
                    context.getString(R.string.new_heading)
                )

                TaskStatus.UNREAD.name -> Pair(
                    R.drawable.status_new_filled_more_corners,
                    context.getString(R.string.unread_heading)
                )

                TaskStatus.ONGOING.name -> Pair(
                    R.drawable.status_ongoing_filled_more_corners,
                    context.getString(R.string.ongoing_heading)
                )

                TaskStatus.DONE.name -> Pair(
                    R.drawable.status_done_filled_more_corners,
                    context.getString(R.string.done_heading)
                )

                TaskStatus.CANCELED.name -> Pair(
                    R.drawable.status_cancelled_filled_more_corners,
                    context.getString(R.string.canceled)
                )

                else -> Pair(
                    R.drawable.status_draft_outline,
                    state.ifEmpty {
                        "N/A"
                    }
                )
            }
            val (background, status) = taskStatusNameBg
            binding.taskDetailStatusName.setBackgroundResource(background)
            binding.taskDetailStatusName.text = status

            binding.taskDetailCreationDate.text =
                DateUtils.formatCreationUTCTimeToCustom(
                    utcTime = task.createdAt,
                    inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
                )

            var dueDate = ""
            dueDate = DateUtils.reformatStringDate(
                date = task.dueDate,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
            )
            if (dueDate == "") {                              // Checking if date format was not dd-MM-yyyy then it will be empty
                dueDate = DateUtils.reformatStringDate(
                    date = task.dueDate,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
                )
                if (dueDate == "") {
                    dueDate = "N/A"
                }
            }
            binding.taskDetailDueDate.text = "Due Date: $dueDate"

            binding.taskTitle.text =
                if (task.topic != null) {
                    task.topic.topic.ifEmpty {
                        "N/A"
                    }
                } else {
                    "N/A"
                }

            if (descriptionExpanded) {
                binding.taskDescription.maxLines = Int.MAX_VALUE
//                binding.viewMoreLessLayout.visibility = View.VISIBLE
//                binding.viewMoreBtn.visibility = View.GONE
//                binding.viewLessBtn.visibility = View.VISIBLE
            } else {
                binding.taskDescription.maxLines = 15
//                binding.viewMoreLessLayout.visibility = View.VISIBLE
//                binding.viewMoreBtn.visibility = View.VISIBLE
//                binding.viewLessBtn.visibility = View.GONE
            }

            if (task.description.isNotEmpty()) {
                binding.taskDescription.text = task.description
            } else {
                binding.taskDescription.text = ""
                binding.taskDescription.visibility = View.GONE
            }
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                if (binding.taskDescription.lineCount > 15) {
                    binding.viewMoreLessLayout.visibility = View.VISIBLE
                    if (descriptionExpanded) {
                        binding.viewMoreBtn.visibility = View.GONE
                        binding.viewLessBtn.visibility = View.VISIBLE
                    } else {
                        binding.viewMoreBtn.visibility = View.VISIBLE
                        binding.viewLessBtn.visibility = View.GONE
                    }
                } else {
                    binding.viewMoreLessLayout.visibility = View.GONE
                    binding.viewMoreBtn.visibility = View.GONE
                    binding.viewLessBtn.visibility = View.GONE
                }
            }, 30)

            if (task.files.isNotEmpty()) {
                separateFiles(task.files)
            }


            binding.taskTitleBar.setOnClickListener {
                if (binding.taskDescriptionImageLayout.visibility == View.VISIBLE) {
                    binding.taskDescriptionImageLayout.visibility = View.GONE
                    binding.downUpIcon.setImageResource(R.drawable.icon_navigate_down)
                } else {
                    binding.taskDescriptionImageLayout.visibility = View.VISIBLE
                    binding.downUpIcon.setImageResource(R.drawable.icon_navigate_up)
                }
            }
            binding.filesHeaderLayout.setOnClickListener {
                if (binding.filesRV.visibility == View.VISIBLE) {
                    binding.filesRV.visibility = View.GONE
                    binding.filesDownUpIcon.setImageResource(R.drawable.icon_navigate_down)
                } else {
                    binding.filesRV.visibility = View.VISIBLE
                    binding.filesDownUpIcon.setImageResource(R.drawable.icon_navigate_up)
                }
            }
            binding.viewMoreBtn.setOnClickListener {
                if (binding.taskDescription.maxLines == 15) {
                    binding.taskDescription.maxLines = Int.MAX_VALUE
                    binding.viewMoreLessLayout.visibility = View.VISIBLE
                    binding.viewMoreBtn.visibility = View.GONE
                    binding.viewLessBtn.visibility = View.VISIBLE
                }
                descriptionExpendedListener?.invoke(true)
            }
            binding.viewLessBtn.setOnClickListener {
                if (binding.taskDescription.maxLines > 15) {
                    binding.taskDescription.maxLines = 15
                    binding.viewMoreLessLayout.visibility = View.VISIBLE
                    binding.viewMoreBtn.visibility = View.VISIBLE
                    binding.viewLessBtn.visibility = View.GONE
                }
                descriptionExpendedListener?.invoke(false)
            }

        }

        private fun separateFiles(files: List<TaskFiles>) {
            val onlyImage: ArrayList<TaskFiles> = arrayListOf()
            val imagesWithComment: ArrayList<TaskFiles> = arrayListOf()
            val document: ArrayList<TaskFiles> = arrayListOf()

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

            binding.onlyImagesRV.visibility =
                if (onlyImage.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            val onlyImageAdapter = OnlyImageRVAdapter()
            onlyImageAdapter.setList(onlyImage)
            binding.onlyImagesRV.adapter = onlyImageAdapter
            onlyImageAdapter.openImageClickListener =
                { _: View, position: Int, fileUrl: String ->
//                val fileUrls: ArrayList<String> = viewModel.onlyImages.value?.map { it.fileUrl } as ArrayList<String>
//                viewModel.openImageViewer(requireContext(), fileUrls, position)
                    val bundle = Bundle()
                    bundle.putParcelableArray("images", onlyImage.toTypedArray())
                    bundle.putInt("position", position)
                    bundle.putBoolean("fromServerUrl", true)
                    openEventImageClickListener?.invoke(position, bundle)
//                    navigate(R.id.imageViewerFragment, bundle)
                }


            binding.imagesWithCommentRV.visibility =
                if (imagesWithComment.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            val imageWithCommentAdapter = ImageWithCommentRVAdapter()
            imageWithCommentAdapter.setList(imagesWithComment)
            binding.imagesWithCommentRV.adapter = imageWithCommentAdapter
            imageWithCommentAdapter.openImageClickListener =
                { _: View, position: Int, fileUrl: String ->
//                val fileUrls: ArrayList<String> = viewModel.imagesWithComments.value?.map { it.fileUrl } as ArrayList<String>
//                viewModel.openImageViewer(requireContext(), fileUrls, position)
                    val bundle = Bundle()
                    bundle.putParcelableArray(
                        "images",
                        imagesWithComment.toTypedArray()
                    )
                    bundle.putInt("position", position)
                    bundle.putBoolean("fromServerUrl", true)
                    openEventImageClickListener?.invoke(position, bundle)
//                    navigate(R.id.imageViewerFragment, bundle)
                }


            binding.filesLayout.visibility =
                if (document.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            val filesAdapter = FilesRVAdapter()
            filesAdapter.setList(document)
            binding.filesRV.adapter = filesAdapter
            if (filesAdapter.itemCount <= 1) {
                binding.filesCount.text = "${filesAdapter.itemCount} File"
            } else {
                binding.filesCount.text = "${filesAdapter.itemCount} Files"
            }
            filesAdapter.fileClickListener = { _: View, position: Int, data: TaskFiles ->
                val bundle = Bundle()
                bundle.putParcelable("taskFile", data)
                fileViewerClickListener?.invoke(position, bundle)
//                navigate(R.id.fileViewerFragment, bundle)
//            val pdfUrl = data.fileUrl             // This following code downloads the file
//            val intent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
//                .addCategory(Intent.CATEGORY_BROWSABLE)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            context?.startActivity(intent)
            }
        }
    }

    inner class MultiItemViewHolder(private val binding: LayoutCeibroTaskDetailEventsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(events: MutableList<*>) {
            val eventsList = events as MutableList<Events>

            val eventsAdapter = EventsRVAdapter()
            eventsAdapter.setList(eventsList, loggedInUserId)
            binding.eventsRV.adapter = eventsAdapter

            binding.eventsParentLayout.visibility =
                if (eventsList.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            eventsAdapter.fileClickListener = { view: View, position: Int, data: EventFiles ->
                val bundle = Bundle()
                bundle.putParcelable("eventFile", data)
                fileViewerClickListener?.invoke(position, bundle)
            }


            eventsAdapter.openEventImageClickListener =
                { _: View, position: Int, imageFiles: List<TaskFiles> ->
//                viewModel.openImageViewer(requireContext(), fileUrls, position)
                    val bundle = Bundle()
                    bundle.putParcelableArray("images", imageFiles.toTypedArray())
                    bundle.putInt("position", position)
                    bundle.putBoolean("fromServerUrl", true)
                    openEventImageClickListener?.invoke(position, bundle)
//                navigate(R.id.imageViewerFragment, bundle)
                }



            binding.eventsHeaderLayout.setOnClickListener {
                if (binding.eventsRV.visibility == View.VISIBLE) {
                    binding.eventsRV.visibility = View.GONE
                    binding.eventsDownUpIcon.setImageResource(R.drawable.icon_navigate_down)
                } else {
                    binding.eventsRV.visibility = View.VISIBLE
                    binding.eventsDownUpIcon.setImageResource(R.drawable.icon_navigate_up)
                }
            }
        }

    }
}
