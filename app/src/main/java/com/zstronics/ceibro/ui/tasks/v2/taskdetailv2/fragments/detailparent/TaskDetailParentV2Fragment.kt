package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailparent

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.ahmadullahpk.alldocumentreader.activity.All_Document_Reader_Activity
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroDownloadDrawingV2
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.EventFiles
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentTags
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.databinding.FragmentTaskDetailParentV2Binding
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawings.DrawingsV2Fragment
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.FilesRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.ImageWithCommentRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.OnlyImageRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.PinnedEventsRVAdapter
import com.zstronics.ceibro.utils.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class TaskDetailParentV2Fragment :
    BaseNavViewModelFragment<FragmentTaskDetailParentV2Binding, ITaskDetailParentV2.State, TaskDetailParentV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskDetailParentV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_detail_parent_v2
    override fun toolBarVisibility(): Boolean = false
    var taskSeenRequest = false
    private var manager: DownloadManager? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionList13 = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    private val permissionList10 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onClick(id: Int) {
        when (id) {
            R.id.viewMoreBtn -> {
                if (mViewDataBinding.taskDescription.maxLines == 15) {
                    mViewDataBinding.taskDescription.maxLines = Int.MAX_VALUE
                    mViewDataBinding.viewMoreLessLayout.visibility = View.VISIBLE
                    mViewDataBinding.viewMoreBtn.visibility = View.GONE
                    mViewDataBinding.viewLessBtn.visibility = View.VISIBLE
                }
                viewModel.descriptionExpanded.postValue(true)


            }

            R.id.viewLessBtn -> {
                if (mViewDataBinding.taskDescription.maxLines > 15) {
                    mViewDataBinding.taskDescription.maxLines = 15
                    mViewDataBinding.viewMoreLessLayout.visibility = View.VISIBLE
                    mViewDataBinding.viewMoreBtn.visibility = View.VISIBLE
                    mViewDataBinding.viewLessBtn.visibility = View.GONE
                }
                viewModel.descriptionExpanded.postValue(false)
            }

            R.id.filesHeaderLayout -> {
                if (mViewDataBinding.filesRV.visibility == View.VISIBLE) {
                    mViewDataBinding.filesRV.visibility = View.GONE
                    mViewDataBinding.filesDownUpIcon.setImageResource(R.drawable.icon_navigate_down)
                } else {
                    mViewDataBinding.filesRV.visibility = View.VISIBLE
                    mViewDataBinding.filesDownUpIcon.setImageResource(R.drawable.icon_navigate_up)
                }
            }

            R.id.pinnedCommentsHeaderLayout -> {
                if (mViewDataBinding.pinnedCommentsRV.visibility == View.VISIBLE) {
                    mViewDataBinding.pinnedCommentsRV.visibility = View.GONE
                    mViewDataBinding.pinnedCommentsDownUpIcon.setImageResource(R.drawable.icon_navigate_down)
                } else {
                    mViewDataBinding.pinnedCommentsRV.visibility = View.VISIBLE
                    mViewDataBinding.pinnedCommentsDownUpIcon.setImageResource(R.drawable.icon_navigate_up)
                }
            }
        }
    }

    @Inject
    lateinit var onlyImageAdapter: OnlyImageRVAdapter

    @Inject
    lateinit var imageWithCommentAdapter: ImageWithCommentRVAdapter

    @Inject
    lateinit var onlyDrawingAdapter: OnlyImageRVAdapter

    lateinit var filesAdapter: FilesRVAdapter

    private lateinit var pinnedEventsAdapter: PinnedEventsRVAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manager =
            mViewDataBinding.root.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        filesAdapter =
            FilesRVAdapter(
                networkConnectivityObserver,
                requireContext(),
                viewModel.downloadedDrawingV2Dao
            )

        pinnedEventsAdapter = PinnedEventsRVAdapter(
            networkConnectivityObserver,
            requireContext(),
            viewModel.downloadedDrawingV2Dao
        )

        viewModel.descriptionExpanded.observe(viewLifecycleOwner) { isExpanded ->
            if (isExpanded) {
                mViewDataBinding.taskDescription.maxLines =
                    mViewDataBinding.taskDescription.lineCount
                mViewDataBinding.viewMoreLessLayout.visibility = View.VISIBLE
                mViewDataBinding.viewMoreBtn.visibility = View.GONE
                mViewDataBinding.viewLessBtn.visibility = View.VISIBLE
            } else {
                mViewDataBinding.taskDescription.maxLines = 15
                mViewDataBinding.viewMoreLessLayout.visibility = View.VISIBLE
                mViewDataBinding.viewMoreBtn.visibility = View.VISIBLE
                mViewDataBinding.viewLessBtn.visibility = View.GONE
            }
        }


        viewModel.taskDetail.observe(viewLifecycleOwner) { item ->
            if (taskSeenRequest) {
                taskSeenRequest = false
            }
            setTaskData(item, mViewDataBinding)
        }


        viewModel.onlyImages.observe(viewLifecycleOwner) { imagesList ->
            if (!imagesList.isNullOrEmpty()) {
                onlyImageAdapter.setList(imagesList)
            } else {
                onlyImageAdapter.setList(listOf())
            }
        }
        mViewDataBinding.onlyImagesRV.adapter = onlyImageAdapter
        onlyImageAdapter.openImageClickListener =
            { _: View, position: Int, fileUrl: String ->
                val bundle = Bundle()
                bundle.putParcelableArray("images", viewModel.onlyImages.value?.toTypedArray())
                bundle.putInt("position", position)
                bundle.putBoolean("fromServerUrl", true)
                navigate(R.id.imageViewerFragment, bundle)
            }


        viewModel.drawingFile.observe(viewLifecycleOwner) { drawingList ->
            if (!drawingList.isNullOrEmpty()) {
                onlyDrawingAdapter.setList(drawingList)
            } else {
                onlyDrawingAdapter.setList(listOf())
            }
        }
        mViewDataBinding.onlyDrawingRV.adapter = onlyDrawingAdapter
        onlyDrawingAdapter.openImageClickListener =
            { _: View, position: Int, fileUrl: String ->
                val bundle = Bundle()
                bundle.putParcelableArray("images", viewModel.drawingFile.value?.toTypedArray())
                bundle.putInt("position", position)
                bundle.putBoolean("fromServerUrl", true)
                navigate(R.id.imageViewerFragment, bundle)
            }


        viewModel.imagesWithComments.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                imageWithCommentAdapter.setList(it)
            } else {
                imageWithCommentAdapter.setList(listOf())
            }
        }
        mViewDataBinding.imagesWithCommentRV.adapter = imageWithCommentAdapter
        imageWithCommentAdapter.openImageClickListener =
            { _: View, position: Int, fileUrl: String ->
                val bundle = Bundle()
                bundle.putParcelableArray(
                    "images",
                    viewModel.imagesWithComments.value?.toTypedArray()
                )
                bundle.putInt("position", position)
                bundle.putBoolean("fromServerUrl", true)
                navigate(R.id.imageViewerFragment, bundle)
            }


        viewModel.documents.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                filesAdapter.setList(it)
            } else {
                filesAdapter.setList(listOf())
            }

            if (it.size <= 1) {
                mViewDataBinding.filesCount.text = "${it.size} File"
            } else {
                mViewDataBinding.filesCount.text = "${it.size} Files"
            }
        }
        mViewDataBinding.filesRV.adapter = filesAdapter

        filesAdapter.fileClickListener =
            { _: View, position: Int, data: TaskFiles, downloadedData: CeibroDownloadDrawingV2 ->
                val bundle = Bundle()
                bundle.putParcelable("taskFile", data)
                bundle.putParcelable("downloadedFile", downloadedData)

                val file = File(downloadedData.localUri)
                val fileUri = Uri.fromFile(file)
                val fileDetails = getPickedFileDetail(requireContext(), fileUri)
                if (fileDetails.attachmentType == AttachmentTypes.Pdf) {
                    navigate(R.id.fileViewerFragment, bundle)
                } else {
                    openFile(file, requireContext())
                    //    shortToastNow("File format not supported yet.")
                }
            }

        filesAdapter.requestPermissionCallBack {
            checkDownloadFilePermission()
        }

        filesAdapter.downloadFileCallBack { textView, ivDownload, downloaded, data, tag ->
            val triplet = Triple(data.id, data.fileName, data.fileUrl)

            checkDownloadFilePermission(triplet, viewModel.downloadedDrawingV2Dao) { }
        }

        mViewDataBinding.pinnedCommentsRV.adapter = pinnedEventsAdapter

        viewModel.taskPinnedEvents.observe(viewLifecycleOwner) { events ->
            if (!events.isNullOrEmpty()) {
                pinnedEventsAdapter.setList(
                    events,
                    viewModel.user?.id ?: viewModel.sessionManager.getUserObj()?.id ?: ""
                )
            } else {
                pinnedEventsAdapter.setList(
                    mutableListOf(),
                    viewModel.user?.id ?: viewModel.sessionManager.getUserObj()?.id ?: ""
                )
            }

            mViewDataBinding.pinnedCommentsCount.text = if (events.size > 1) {
                "${events.size} comments"
            } else {
                "${events.size} comment"
            }

            mViewDataBinding.pinnedCommentsLayout.visibility =
                if (events.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    mViewDataBinding.pinnedCommentsDownUpIcon.setImageResource(R.drawable.icon_navigate_up)
                    View.GONE
                }
        }

        pinnedEventsAdapter.requestPermissionCallBack {
            checkDownloadFilePermission()
        }
        pinnedEventsAdapter.downloadFileCallBack { textView, ivDownload, downloaded, triplet, tag ->
            checkDownloadFilePermission(triplet, viewModel.downloadedDrawingV2Dao) {
                MainScope().launch {
                    /*
                          if (it.trim().equals("100%", true)) {
                              textView.visibility = View.GONE
                              downloaded.visibility = View.VISIBLE
                              textView.text = it
                              //    detailAdapter.notifyDataSetChanged()
                          } else if (it == "retry" || it == "failed") {
                              downloaded.visibility = View.GONE
                              textView.visibility = View.GONE
                              ivDownload.visibility = View.VISIBLE
                          } else {

                              println("progress: $it textView.text = ${textView.text}")
                              textView.text = it
                              textView.visibility = View.VISIBLE
                          }
                          */
                }
            }
        }

        pinnedEventsAdapter.fileClickListener =
            { view: View, position: Int, data: EventFiles, drawingFile ->
                val bundle = Bundle()
                bundle.putParcelable("eventFile", data)
                bundle.putParcelable("downloadedFile", drawingFile)

                val file = File(drawingFile.localUri)
                val fileUri = Uri.fromFile(file)
                val fileDetails = getPickedFileDetail(requireContext(), fileUri)
                if (fileDetails.attachmentType == AttachmentTypes.Pdf) {
                    navigate(R.id.fileViewerFragment, bundle)
                } else {
                    openFile(file, requireContext())
                    //    shortToastNow("File format not supported yet.")
                }
            }


        pinnedEventsAdapter.openEventImageClickListener =
            { _: View, position: Int, imageFiles: List<TaskFiles> ->
                val bundle = Bundle()
                bundle.putParcelableArray("images", imageFiles.toTypedArray())
                bundle.putInt("position", position)
                bundle.putBoolean("fromServerUrl", true)
                navigate(R.id.imageViewerFragment, bundle)
            }

        pinnedEventsAdapter.pinClickListener =
            { position, event, isPinned ->
                viewModel.pinOrUnpinComment(
                    event.taskId,
                    event.id,
                    isPinned
                ) { isSuccess, updatedEvent ->
                    if (updatedEvent != null) {
                        val originalEvents = viewModel.originalPinnedEvents.value
                        if (!originalEvents.isNullOrEmpty()) {
                            val foundEvent = originalEvents.find { it.id == updatedEvent.id }
                            if (foundEvent != null) {
                                val index = originalEvents.indexOf(foundEvent)
                                originalEvents.removeAt(index)
                                viewModel.originalPinnedEvents.postValue(originalEvents)
                                viewModel._taskPinnedEvents.postValue(originalEvents)
                            }
                        }

//                        if (pinnedEventsAdapter.listItems.isNotEmpty()) {
//                            val adapterEvent =
//                                pinnedEventsAdapter.listItems.find { it.id == updatedEvent.id }
//                            if (adapterEvent != null) {
//                                val index = pinnedEventsAdapter.listItems.indexOf(adapterEvent)
//                                pinnedEventsAdapter.listItems[index] = updatedEvent
//                                pinnedEventsAdapter.notifyItemChanged(index)
//                            }
//                        }
                    }
                }
            }

    }

    private fun setTaskData(task: CeibroTaskV2, binding: FragmentTaskDetailParentV2Binding) {
        binding.viewMoreLessLayout.visibility = View.GONE
        binding.filesLayout.visibility = View.GONE

        binding.onlyImagesRV.visibility = View.GONE
        binding.onlyImages.visibility = View.GONE

        binding.onlyDrawingRV.visibility = View.GONE
        binding.onlyDrawings.visibility = View.GONE

        binding.imagesWithCommentRV.visibility = View.GONE
        binding.imagesWithComment.visibility = View.GONE

        binding.onlyImagesRV.isNestedScrollingEnabled = false
        binding.onlyDrawingRV.isNestedScrollingEnabled = false
        binding.imagesWithCommentRV.isNestedScrollingEnabled = false

        binding.filesRV.isNestedScrollingEnabled = false

        val context = binding.taskDetailStatusName.context
        var state = ""
        state =
            if (task.isCreator || task.isTaskViewer) {
                task.creatorState
            } else if (viewModel.rootState == TaskRootStateTags.Hidden.tagValue && viewModel.selectedState.equals(
                    TaskStatus.CANCELED.name,
                    true
                )
            ) {
                task.creatorState
            } else {
                task.userSubState
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

            TaskRootStateTags.InReview.tagValue.uppercase() -> Pair(
                R.drawable.status_in_review_outline,
                state.toCamelCase()
            )

            TaskRootStateTags.ToReview.tagValue.uppercase() -> Pair(
                R.drawable.status_in_review_outline,
                state.toCamelCase()
            )

            TaskStatus.DONE.name -> Pair(
                R.drawable.status_done_filled_more_corners,
                context.getString(R.string.done_heading)
            )

            TaskStatus.CANCELED.name -> Pair(
                R.drawable.status_cancelled_filled_more_corners,
                context.getString(R.string.canceled)
            )

            TaskDetailEvents.REJECT_CLOSED.eventValue.uppercase() -> Pair(
                R.drawable.status_reject_filled_more_full_corners,
                state.toCamelCase()
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
            if (task.title != null) {
                task.title.ifEmpty {
                    "N/A"
                }
            } else {
                "N/A"
            }

        if (viewModel.descriptionExpanded.value == true) {
            binding.taskDescription.maxLines = binding.taskDescription.lineCount
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
                if (viewModel.descriptionExpanded.value == true) {
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


    }


    private fun separateFiles(files: List<TaskFiles>) {
        val onlyImage: ArrayList<TaskFiles> = arrayListOf()
        val onlyDrawingImage: ArrayList<TaskFiles> = arrayListOf()
        val imagesWithComment: ArrayList<TaskFiles> = arrayListOf()
        val document: ArrayList<TaskFiles> = arrayListOf()

        for (item in files) {
            when (item.fileTag) {
                AttachmentTags.Image.tagValue -> {
                    onlyImage.add(item)
                }

                AttachmentTags.Drawing.tagValue -> {
                    onlyDrawingImage.add(item)
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
            mViewDataBinding.onlyImagesRV.visibility = View.VISIBLE
            mViewDataBinding.onlyImages.visibility = View.VISIBLE
        } else {
            mViewDataBinding.onlyImagesRV.visibility = View.GONE
            mViewDataBinding.onlyImages.visibility = View.GONE
        }
        viewModel._onlyImages.postValue(onlyImage)


        if (onlyDrawingImage.isNotEmpty()) {
            mViewDataBinding.onlyDrawingRV.visibility = View.VISIBLE
            mViewDataBinding.onlyDrawings.visibility = View.VISIBLE

        } else {
            mViewDataBinding.onlyDrawingRV.visibility = View.GONE
            mViewDataBinding.onlyDrawings.visibility = View.GONE
        }
        viewModel._drawingFile.postValue(onlyDrawingImage)


        if (imagesWithComment.isNotEmpty()) {
            mViewDataBinding.imagesWithCommentRV.visibility = View.VISIBLE
            mViewDataBinding.imagesWithComment.visibility = View.VISIBLE
        } else {
            mViewDataBinding.imagesWithCommentRV.visibility = View.GONE
            mViewDataBinding.imagesWithComment.visibility = View.GONE
        }
        viewModel._imagesWithComments.postValue(imagesWithComment)


        mViewDataBinding.filesLayout.visibility =
            if (document.isNotEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

        viewModel._documents.postValue(document)

    }


    private fun getPickedFileDetail(context: Context, fileUri: Uri?): PickedImages {
        val mimeType = FileUtils.getMimeType(context, fileUri)
        val fileName = FileUtils.getFileName(context, fileUri)
        val fileSize = FileUtils.getFileSizeInBytes(context, fileUri)
        val fileSizeReadAble = FileUtils.getReadableFileSize(fileSize)
        println("mimeTypeFound: ${mimeType} - ${fileName}")
        val attachmentType = when {
            mimeType == null -> {
                AttachmentTypes.Doc
            }

            mimeType == "application/pdf" -> {
                AttachmentTypes.Pdf
            }

            mimeType == "application/x-rar-compressed" || mimeType == "application/zip" -> {
                AttachmentTypes.Zip
            }

            mimeType.equals("text/plain", true) ||
                    mimeType.equals("text/csv", true) ||
                    mimeType.equals("application/rtf", true) ||
                    mimeType.equals("application/zip", true) ||
                    mimeType.equals("application/x-rar-compressed", true) ||
                    mimeType.equals("application/vnd.oasis.opendocument.text", true) ||
                    mimeType.equals("application/vnd.oasis.opendocument.spreadsheet", true) ||
                    mimeType.equals("application/vnd.oasis.opendocument.presentation", true) ||
                    mimeType.equals("application/vnd.android.package-archive", true) ||
                    mimeType.equals("application/msword", true) ||
                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        true
                    ) ||
                    mimeType.equals(
                        "application/vnd.ms-word.document.macroEnabled.12",
                        true
                    ) ||
                    mimeType.equals("application/vnd.ms-excel", true) ||
                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        true
                    ) ||
                    mimeType.equals(
                        "application/vnd.ms-excel.sheet.macroEnabled.12",
                        true
                    ) ||
                    mimeType.equals("application/vnd.ms-powerpoint", true) ||
                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        true
                    ) ||
                    mimeType.equals(
                        "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
                        true
                    ) ||
                    mimeType.equals(
                        "image/vnd.dwg",
                        true
                    ) ||
                    mimeType.equals(
                        "application/acad",
                        true
                    ) -> {
                AttachmentTypes.Doc
            }

            mimeType.contains("image/vnd") -> {
                AttachmentTypes.Doc
            }

            mimeType.startsWith("image") -> {
                AttachmentTypes.Image
            }

            mimeType.startsWith("video") -> {
                AttachmentTypes.Video
            }

            else -> AttachmentTypes.Doc
        }
        return PickedImages(
            fileUri = fileUri,
            attachmentType = attachmentType,
            fileName = fileName,
            fileSizeReadAble = fileSizeReadAble,
            file = FileUtils.getFile(requireContext(), fileUri)
        )
    }

    private fun openFile(file: File, context: Context) {

        val intent = Intent(context, All_Document_Reader_Activity::class.java)
        intent.putExtra("path", file.absolutePath)
        intent.putExtra("fromAppActivity", true)
        context.startActivity(intent)
        return

    }


    private fun checkDownloadFilePermission(
        url: Triple<String, String, String>,
        downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
        itemClickListener: ((tag: String) -> Unit)?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkPermissions(permissionList13)) {
                downloadFile(url, downloadedDrawingV2Dao) {
                    itemClickListener?.invoke(it)
                }
            } else {
                requestPermissions(permissionList13)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {

            downloadFile(url, downloadedDrawingV2Dao) {
                itemClickListener?.invoke(it)
            }
        } else {
            if (checkPermissions(permissionList10)) {
                downloadFile(url, downloadedDrawingV2Dao) {
                    itemClickListener?.invoke(it)
                }
            } else {

                downloadFile(url, downloadedDrawingV2Dao) {
                    itemClickListener?.invoke(it)
                }
                requestPermissions(permissionList10)
            }
        }
    }

    private fun checkDownloadFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(permissionList13)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {


        } else {
            requestPermissions(permissionList10)
        }
    }


    private fun checkPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(
            requireActivity(), permissions,
            DrawingsV2Fragment.permissionRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == DrawingsV2Fragment.permissionRequestCode) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted
            } else {
                handleDeniedPermissions(permissions, grantResults)
            }
        }
    }

    private fun handleDeniedPermissions(permissions: Array<out String>, grantResults: IntArray) {
        for (i in permissions.indices) {
            val permission = permissions[i]
            val result = grantResults[i]

            if (result == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    showToast("Permission denied: $permission")
                } else {
                    showToast("Permission denied: $permission. Please enable it in the app settings.")
                    navigateToAppSettings(context)
                    return
                }
            }
        }
    }

    private fun navigateToAppSettings(context: Context?) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", context?.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun downloadFile(
        triplet: Triple<String, String, String>,
        downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
        itemClickListener: ((tag: String) -> Unit)?
    ) {

        manager?.let {
            downloadGenericFile(triplet, downloadedDrawingV2Dao, it) { downloadId ->
                Handler(Looper.getMainLooper()).postDelayed({
                    getDownloadProgress(context, downloadId) { tag ->
                        GlobalScope.launch(Dispatchers.Main) {
                            if (tag == "retry" || tag == "failed") {
                                downloadedDrawingV2Dao.deleteByDrawingID(downloadId.toString())
                            } else if (tag.trim().equals("100%", true)) {

                                shortToastNow("Downloaded")
                            }
                        }
                        itemClickListener?.invoke(tag)
                    }
                }, 1000)
            }
        } ?: kotlin.run {

            manager =
                requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager?.let {
                downloadGenericFile(triplet, downloadedDrawingV2Dao, it) { downloadId ->
                    Handler(Looper.getMainLooper()).postDelayed({
                        getDownloadProgress(context, downloadId) { tag ->
                            GlobalScope.launch(Dispatchers.Main) {
                                if (tag == "retry" || tag == "failed") {
                                    downloadedDrawingV2Dao.deleteByDrawingID(downloadId.toString())
                                } else if (tag.trim().equals("100%", true)) {

                                    shortToastNow("Downloaded")
                                }
                            }
                            itemClickListener?.invoke(tag)
                        }
                    }, 1000)
                }
            }
        }

        /*
        shortToastNow("Downloading file...")
        val uri = Uri.parse(triplet.third)
        val fileName = triplet.second
        val folder = File(
            context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            DrawingsV2Fragment.folderName
        )
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val destinationUri = Uri.fromFile(File(folder, fileName))

        val request: DownloadManager.Request? =
            DownloadManager
                .Request(uri)
                .setDestinationUri(destinationUri)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setVisibleInDownloadsUi(true)

        val downloadId = manager?.enqueue(request)

        println("progress id   $downloadId")
        println("progress   $manager")

        val ceibroDownloadDrawingV2 = downloadId?.let {
            CeibroDownloadDrawingV2(
                fileName = triplet.second,
                downloading = true,
                isDownloaded = false,
                downloadId = it,
                drawing = null,
                drawingId = triplet.first,
                groupId = "",
                localUri = ""
            )
        }


        GlobalScope.launch {
            ceibroDownloadDrawingV2?.let {
                downloadedDrawingV2Dao.insertDownloadDrawing(it)
                println("progress  object  $it")
            }

        }

        Handler(Looper.getMainLooper()).postDelayed({
            getDownloadProgress(context, downloadId!!) {
                GlobalScope.launch(Dispatchers.Main) {
                    if (it == "retry" || it == "failed") {
                        downloadedDrawingV2Dao.deleteByDrawingID(downloadId.toString())
                    } else if (it.trim().equals("100%", true)) {

                        shortToastNow("Downloaded")
                    }
                }
                itemClickListener?.invoke(it)
            }
        }, 1000)

        println("id: ${id} Folder name: ${folder} uri:${uri} destinationUri:${destinationUri}")
*/
    }

    @SuppressLint("Range")
    private fun getDownloadProgress(
        context: Context?,
        downloadId: Long,
        itemClickListener: ((tag: String) -> Unit)?
    ) {
        GlobalScope.launch {
            while (true) {
                println("progress : checking")
                val query = DownloadManager.Query().setFilterById(downloadId)
                val manager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val cursor = manager.query(query)

                if (cursor.moveToFirst()) {
                    val bytesDownloaded =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytesTotal =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                    val status =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    println("Status: $status")

                    println("progress status : $status")

                    if (status.toInt() == DownloadManager.STATUS_FAILED) {

                        println("Status failed: $status")
                        itemClickListener?.invoke("failed")
                        break
                    } else if (status.toInt() == DownloadManager.STATUS_SUCCESSFUL) {
                        itemClickListener?.invoke("100%")
                        break
                    }

                    val downloadedPercent = ((bytesDownloaded * 100L) / bytesTotal).toInt()

                    println("StatusProgress %: $downloadedPercent")
                    println("StatusDownloaded: $bytesDownloaded")
                    println("StatusTotal: $bytesTotal")

                    itemClickListener?.invoke("$downloadedPercent %")
                    if (bytesTotal > 0) {
                        println("Progress: " + ((bytesDownloaded * 100L) / bytesTotal).toInt())
                        println("progress downloaded" + ((bytesDownloaded * 100L) / bytesTotal).toInt())
                    }
                } else {
                    println("progress retry ")
                    itemClickListener?.invoke("retry")
                    break
                }
                cursor.close()

                delay(500)
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            EventBus.getDefault().register(this)
        } catch (_:Exception) { }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            EventBus.getDefault().unregister(this)
        } catch (_:Exception) { }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskSeenEvent(event: LocalEvents.TaskSeenEvent?) {
        val task = event?.task
        if (task != null) {
            viewModel.taskDetail.value?.let { taskDetail ->
                if (task.id == taskDetail.id) {
                    task.let { it1 ->
                        taskSeenRequest = true
                        viewModel.originalTask.postValue(it1)
                        viewModel._taskDetail.postValue(it1)
                    }
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskDoneEvent(event: LocalEvents.TaskDoneEvent?) {
        val task = event?.task
        if (task != null) {
            viewModel.taskDetail.value?.let { taskDetail ->
                if (task.id == taskDetail.id) {
                    task.let { it1 ->
                        taskSeenRequest = true
                        viewModel.originalTask.postValue(it1)
                        viewModel._taskDetail.postValue(it1)
                    }
                }
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskEventUpdate(
        event: LocalEvents.TaskEventUpdate?
    ) {
        val updatedEvent = event?.events
        val taskDetail = viewModel.taskDetail.value
        if (taskDetail != null && updatedEvent != null && updatedEvent.taskId == taskDetail.id) {

            if (updatedEvent.isPinned == true) {
                val originalEvents = viewModel.originalPinnedEvents.value ?: mutableListOf()
                if (originalEvents.isNotEmpty()) {
                    val foundEvent = originalEvents.find { it.id == updatedEvent.id }
                    if (foundEvent != null) {
                        val index = originalEvents.indexOf(foundEvent)
                        originalEvents[index] = updatedEvent
                        viewModel.originalPinnedEvents.postValue(originalEvents)
                    } else {
                        originalEvents.add(updatedEvent)
                        viewModel.originalPinnedEvents.postValue(originalEvents)
                    }

                } else {
                    originalEvents.add(updatedEvent)
                    viewModel.originalPinnedEvents.postValue(originalEvents)
                    viewModel._taskPinnedEvents.postValue(originalEvents)
                }
                mViewDataBinding.pinnedCommentsCount.text = if (originalEvents.size > 1) {
                    "${originalEvents.size} comments"
                } else {
                    "${originalEvents.size} comment"
                }

                mViewDataBinding.pinnedCommentsLayout.visibility =
                    if (originalEvents.isNotEmpty()) {
                        View.VISIBLE
                    } else {
                        mViewDataBinding.pinnedCommentsDownUpIcon.setImageResource(R.drawable.icon_navigate_up)
                        View.GONE
                    }


                if (pinnedEventsAdapter.listItems.isNotEmpty()) {
                    val adapterEvent =
                        pinnedEventsAdapter.listItems.find { it.id == updatedEvent.id }
                    if (adapterEvent != null) {
                        val index = pinnedEventsAdapter.listItems.indexOf(adapterEvent)
                        pinnedEventsAdapter.listItems[index] = updatedEvent
                        pinnedEventsAdapter.notifyItemChanged(index)
                    } else {
                        pinnedEventsAdapter.listItems.add(updatedEvent)
                        pinnedEventsAdapter.notifyItemInserted(pinnedEventsAdapter.listItems.size - 1)
                    }
                } else {
                    pinnedEventsAdapter.listItems.add(updatedEvent)
                    pinnedEventsAdapter.notifyItemInserted(pinnedEventsAdapter.listItems.size - 1)
                }
            } else {
                val originalEvents = viewModel.originalPinnedEvents.value
                if (!originalEvents.isNullOrEmpty()) {
                    val foundEvent = originalEvents.find { it.id == updatedEvent.id }
                    if (foundEvent != null) {
                        val index = originalEvents.indexOf(foundEvent)
                        originalEvents.removeAt(index)
                        viewModel.originalPinnedEvents.postValue(originalEvents)
                    }

                    mViewDataBinding.pinnedCommentsCount.text = if (originalEvents.size > 1) {
                        "${originalEvents.size} comments"
                    } else {
                        "${originalEvents.size} comment"
                    }

                    mViewDataBinding.pinnedCommentsLayout.visibility =
                        if (originalEvents.isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            mViewDataBinding.pinnedCommentsDownUpIcon.setImageResource(R.drawable.icon_navigate_up)
                            View.GONE
                        }
                }

                if (pinnedEventsAdapter.listItems.isNotEmpty()) {
                    val adapterEvent =
                        pinnedEventsAdapter.listItems.find { it.id == updatedEvent.id }
                    if (adapterEvent != null) {
                        val index = pinnedEventsAdapter.listItems.indexOf(adapterEvent)
                        pinnedEventsAdapter.listItems.removeAt(index)
                        pinnedEventsAdapter.notifyItemRemoved(index)
                    }
                }
            }

        }
    }


}