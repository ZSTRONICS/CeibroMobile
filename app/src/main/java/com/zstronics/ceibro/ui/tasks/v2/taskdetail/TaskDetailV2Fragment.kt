package com.zstronics.ceibro.ui.tasks.v2.taskdetail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BackNavigationResult
import com.zstronics.ceibro.base.navgraph.BackNavigationResultListener
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.databinding.FragmentTaskDetailV2Binding
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.EventsRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.FilesRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.ImageWithCommentRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.OnlyImageRVAdapter
import com.zstronics.ceibro.utils.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

@AndroidEntryPoint
class TaskDetailV2Fragment :
    BaseNavViewModelFragment<FragmentTaskDetailV2Binding, ITaskDetailV2.State, TaskDetailV2VM>(),
    BackNavigationResultListener {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskDetailV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_detail_v2
    override fun toolBarVisibility(): Boolean = false
    val FORWARD_TASK_REQUEST_CODE = 104
    val COMMENT_REQUEST_CODE = 106
    val DONE_REQUEST_CODE = 107
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> navigateBack()
            R.id.taskInfoBtn -> showTaskInfoBottomSheet()
            R.id.taskCommentBtn -> {
                val bundle = Bundle()
                bundle.putParcelable("taskData", viewModel.taskDetail.value)
                bundle.putString("action", TaskDetailEvents.Comment.eventValue)
                navigateForResult(R.id.commentFragment, COMMENT_REQUEST_CODE, bundle)
            }
            R.id.doneBtn -> {
                if (viewModel.taskDetail.value?.doneCommentsRequired == true || viewModel.taskDetail.value?.doneImageRequired == true) {
                    val bundle = Bundle()
                    bundle.putParcelable("taskData", viewModel.taskDetail.value)
                    bundle.putString("action", TaskDetailEvents.DoneTask.eventValue)
                    navigateForResult(R.id.commentFragment, DONE_REQUEST_CODE, bundle)
                } else {
                    viewModel.doneTask(viewModel.taskDetail.value?.id ?: "") { task ->
                        if (task != null) {
                            viewModel._taskDetail.postValue(task)
                        }
                    }
                }
            }
            R.id.taskForwardBtn -> {
                val assignTo = viewModel.taskDetail.value?.assignedToState?.map { it.phoneNumber }
                val invited = viewModel.taskDetail.value?.invitedNumbers?.map { it.phoneNumber }
                val combinedList = arrayListOf<String>()
                if (assignTo != null) {
                    combinedList.addAll(assignTo)
                }
                if (invited != null) {
                    combinedList.addAll(invited)
                }

                val bundle = Bundle()
                bundle.putStringArrayList(
                    "assignToContacts",
                    combinedList
                )
                bundle.putParcelable("taskDetail", viewModel.taskDetail.value)
                navigateForResult(R.id.forwardTaskFragment, FORWARD_TASK_REQUEST_CODE, bundle)
            }
            R.id.taskTitleBar -> {
                if (mViewDataBinding.taskDescriptionImageLayout.visibility == View.VISIBLE) {
                    mViewDataBinding.taskDescriptionImageLayout.visibility = View.GONE
                    mViewDataBinding.downUpIcon.setImageResource(R.drawable.icon_navigate_down)
                } else {
                    mViewDataBinding.taskDescriptionImageLayout.visibility = View.VISIBLE
                    mViewDataBinding.downUpIcon.setImageResource(R.drawable.icon_navigate_up)
                }
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

            R.id.eventsHeaderLayout -> {
                if (mViewDataBinding.eventsRV.visibility == View.VISIBLE) {
                    mViewDataBinding.eventsRV.visibility = View.GONE
                    mViewDataBinding.eventsDownUpIcon.setImageResource(R.drawable.icon_navigate_down)
                } else {
                    mViewDataBinding.eventsRV.visibility = View.VISIBLE
                    mViewDataBinding.eventsDownUpIcon.setImageResource(R.drawable.icon_navigate_up)
                }
            }
        }
    }


    @Inject
    lateinit var onlyImageAdapter: OnlyImageRVAdapter

    @Inject
    lateinit var imageWithCommentAdapter: ImageWithCommentRVAdapter

    @Inject
    lateinit var filesAdapter: FilesRVAdapter

    @Inject
    lateinit var eventsAdapter: EventsRVAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.confirmNeededBtn.visibility = View.GONE
        mViewDataBinding.filesLayout.visibility = View.GONE
        mViewDataBinding.onlyImagesRV.visibility = View.GONE
        mViewDataBinding.imagesWithCommentRV.visibility = View.GONE

        mViewDataBinding.onlyImagesRV.isNestedScrollingEnabled = false
        mViewDataBinding.imagesWithCommentRV.isNestedScrollingEnabled = false
        mViewDataBinding.filesRV.isNestedScrollingEnabled = false

        viewModel.taskDetail.observe(viewLifecycleOwner) { item ->
            if (item.creatorState.equals(TaskStatus.DONE.name, true) || item.creatorState.equals(TaskStatus.CANCELED.name, true) ||
                (viewModel.rootState == TaskRootStateTags.ToMe.tagValue && (item.assignedToState.find { it.userId == viewModel.user?.id }?.state).equals(TaskStatus.NEW.name, true))
            ) {
                mViewDataBinding.doneBtn.isEnabled = false
                mViewDataBinding.doneBtn.isClickable = false
                mViewDataBinding.doneBtn.alpha = 0.6f
                mViewDataBinding.taskForwardBtn.isEnabled = false
                mViewDataBinding.taskForwardBtn.isClickable = false
                mViewDataBinding.taskForwardBtn.alpha = 0.6f
            } else {
                mViewDataBinding.doneBtn.isEnabled = true
                mViewDataBinding.doneBtn.isClickable = true
                mViewDataBinding.doneBtn.alpha = 1f
                mViewDataBinding.taskForwardBtn.isEnabled = true
                mViewDataBinding.taskForwardBtn.isClickable = true
                mViewDataBinding.taskForwardBtn.alpha = 1f
            }
            if (item.creatorState.equals(TaskStatus.DONE.name, true) || item.creatorState.equals(TaskStatus.CANCELED.name, true)) {
                mViewDataBinding.doneRequirementBadge.visibility = View.GONE
            } else {
                if (item.doneCommentsRequired || item.doneImageRequired) {
                    mViewDataBinding.doneRequirementBadge.visibility = View.VISIBLE
                } else {
                    mViewDataBinding.doneRequirementBadge.visibility = View.GONE
                }
            }

            mViewDataBinding.detailViewHeading.text = item.taskUID

            var state = ""
            state = if (viewModel.rootState == TaskRootStateTags.FromMe.tagValue && viewModel.user?.id == item.creator.id) {
                item.creatorState
            } else if (viewModel.rootState == TaskRootStateTags.Hidden.tagValue && viewModel.selectedState.equals(TaskStatus.CANCELED.name, true)) {
                item.creatorState
            } else {
                item.assignedToState.find { it.userId == viewModel.user?.id }?.state ?: ""
            }
            val taskStatusNameBg: Pair<Int, String> = when (state.uppercase()) {
                TaskStatus.NEW.name -> Pair(
                    R.drawable.status_new_filled_more_corners,
                    requireContext().getString(R.string.new_heading)
                )

                TaskStatus.UNREAD.name -> Pair(
                    R.drawable.status_new_filled_more_corners,
                    requireContext().getString(R.string.unread_heading)
                )

                TaskStatus.ONGOING.name -> Pair(
                    R.drawable.status_ongoing_filled_more_corners,
                    requireContext().getString(R.string.ongoing_heading)
                )

                TaskStatus.DONE.name -> Pair(
                    R.drawable.status_done_filled_more_corners,
                    requireContext().getString(R.string.done_heading)
                )

                TaskStatus.CANCELED.name -> Pair(
                    R.drawable.status_cancelled_filled_more_corners,
                    requireContext().getString(R.string.canceled)
                )

                else -> Pair(
                    R.drawable.status_draft_outline,
                    state.ifEmpty {
                        "N/A"
                    }
                )
            }
            val (background, status) = taskStatusNameBg
            mViewDataBinding.taskDetailStatusName.setBackgroundResource(background)
            mViewDataBinding.taskDetailStatusName.text = status

            mViewDataBinding.taskDetailCreationDate.text = DateUtils.formatCreationUTCTimeToCustom(
                utcTime = item.createdAt,
                inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
            )

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
            mViewDataBinding.taskDetailDueDate.text = "Due Date: $dueDate"

            mViewDataBinding.taskTitle.text =
                if (item.topic != null) {
                    item.topic.topic.ifEmpty {
                        "N/A"
                    }
                } else {
                    "N/A"
                }

            if (item.description.isNotEmpty()) {
                mViewDataBinding.taskDescription.text = item.description
            } else {
                mViewDataBinding.taskDescription.visibility = View.GONE
            }


            if (item.files.isNotEmpty()) {
                viewModel.separateFiles(item.files)
            }

            if (item.events.isNotEmpty()) {
                mViewDataBinding.eventsLayout.visibility = View.VISIBLE
                viewModel.handleEvents(item.events)
            } else {
                mViewDataBinding.eventsLayout.visibility = View.GONE
            }
        }


        viewModel.onlyImages.observe(viewLifecycleOwner) {
            onlyImageAdapter.setList(it)
            mViewDataBinding.onlyImagesRV.visibility =
                if (it.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
        mViewDataBinding.onlyImagesRV.adapter = onlyImageAdapter
        onlyImageAdapter.openImageClickListener =
            { _: View, position: Int, fileUrl: String ->
                val fileUrls: ArrayList<String> = viewModel.onlyImages.value?.map { it.fileUrl } as ArrayList<String>
                viewModel.openImageViewer(requireContext(), fileUrls, position)
            }


        viewModel.imagesWithComments.observe(viewLifecycleOwner) {
            imageWithCommentAdapter.setList(it)
            mViewDataBinding.imagesWithCommentRV.visibility =
                if (it.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
        mViewDataBinding.imagesWithCommentRV.adapter = imageWithCommentAdapter
        imageWithCommentAdapter.openImageClickListener =
            { _: View, position: Int, fileUrl: String ->
                val fileUrls: ArrayList<String> = viewModel.imagesWithComments.value?.map { it.fileUrl } as ArrayList<String>
                viewModel.openImageViewer(requireContext(), fileUrls, position)
            }

        viewModel.documents.observe(viewLifecycleOwner) {
            filesAdapter.setList(it)
            mViewDataBinding.filesLayout.visibility =
                if (it.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            mViewDataBinding.filesCount.text = "${it.size} file(s)"
        }
        mViewDataBinding.filesRV.adapter = filesAdapter
        filesAdapter.fileClickListener = { _: View, position: Int, data: TaskFiles ->
            val bundle = Bundle()
            bundle.putParcelable("taskFile", data)
            navigate(R.id.fileViewerFragment, bundle)
//            val pdfUrl = data.fileUrl             // This following code downloads the file
//            val intent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
//                .addCategory(Intent.CATEGORY_BROWSABLE)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            context?.startActivity(intent)
        }


        viewModel.taskEvents.observe(viewLifecycleOwner) {
            eventsAdapter.setList(it)
            mViewDataBinding.eventsLayout.visibility =
                if (it.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
        mViewDataBinding.eventsRV.adapter = eventsAdapter
        eventsAdapter.openEventImageClickListener =
            { _: View, position: Int, fileUrls: ArrayList<String> ->
                viewModel.openImageViewer(requireContext(), fileUrls, position)
            }
        val layoutManager = LinearLayoutManager(context)
        layoutManager.isAutoMeasureEnabled = false      //to show all content in RV
        mViewDataBinding.eventsRV.layoutManager = layoutManager
    }


    private fun showTaskInfoBottomSheet() {
        val sheet = TaskInfoBottomSheet(
            _rootState = viewModel.rootState,
            _selectedState = viewModel.selectedState,
            _userId = viewModel.user?.id ?: "",
            _taskDetail = viewModel.taskDetail.value)
//        sheet.dialog?.window?.setSoftInputMode(
//            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
//                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
//        );

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "TaskInfoBottomSheet")
    }


    override fun onNavigationResult(result: BackNavigationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            when (result.requestCode) {
                FORWARD_TASK_REQUEST_CODE -> {
                    val updatedTask = result.data?.getParcelable<CeibroTaskV2>("taskData")
                    if (updatedTask != null) {
                        viewModel._taskDetail.postValue(updatedTask)
                    }
                }

                COMMENT_REQUEST_CODE -> {
                    val updatedTask = result.data?.getParcelable<CeibroTaskV2>("taskData")
                    if (updatedTask != null) {
                        viewModel._taskDetail.postValue(updatedTask)
                    }
                }

                DONE_REQUEST_CODE -> {
                    val updatedTask = result.data?.getParcelable<CeibroTaskV2>("taskData")
                    if (updatedTask != null) {
                        viewModel._taskDetail.postValue(updatedTask)
                    }
                }
            }
        }
    }

}