package com.zstronics.ceibro.ui.tasks.v2.taskdetail

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BackNavigationResult
import com.zstronics.ceibro.base.navgraph.BackNavigationResultListener
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardTaskV2Request
import com.zstronics.ceibro.databinding.FragmentTaskDetailV2Binding
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.EventsRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.FilesRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.ImageWithCommentRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.OnlyImageRVAdapter
import com.zstronics.ceibro.utils.DateUtils
import dagger.hilt.android.AndroidEntryPoint
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
    val FORWARD_REQUEST_CODE = 105
    val COMMENT_REQUEST_CODE = 106
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> navigateBack()
            R.id.taskInfoBtn -> showTaskInfoBottomSheet()
            R.id.taskCommentBtn -> {
                val bundle = Bundle()
                bundle.putParcelable("taskData", viewModel.taskDetail.value)
                navigateForResult(R.id.commentFragment, COMMENT_REQUEST_CODE, bundle)
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
                navigateForResult(R.id.forwardFragment, FORWARD_REQUEST_CODE, bundle)
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

            mViewDataBinding.taskDetailCreationDate.text = DateUtils.reformatStringDate(
                date = item.createdAt,
                DateUtils.SERVER_DATE_FULL_FORMAT,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DAY
            )

            mViewDataBinding.taskDetailDueDate.text = DateUtils.reformatStringDate(
                date = item.dueDate,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
            )
            if (mViewDataBinding.taskDetailDueDate.text == "") {                              // Checking if date format was not dd-MM-yyyy then it will be empty
                mViewDataBinding.taskDetailDueDate.text = DateUtils.reformatStringDate(
                    date = item.dueDate,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
                )
                if (mViewDataBinding.taskDetailDueDate.text == "") {
                    mViewDataBinding.taskDetailDueDate.text = "N/A"
                }
            }

            mViewDataBinding.taskTitle.text =
                if (item.topic != null) {
                    item.topic.topic.ifEmpty {
                        "- - - - -"
                    }
                } else {
                    "- - - - -"
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

    }


    private fun showTaskInfoBottomSheet() {
        val sheet = TaskInfoBottomSheet(viewModel.taskDetail.value)
//        sheet.dialog?.window?.setSoftInputMode(
//            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
//                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
//        );

        sheet.isCancelable = false
        sheet.show(childFragmentManager, "TaskInfoBottomSheet")
    }


    override fun onNavigationResult(result: BackNavigationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            when (result.requestCode) {
                FORWARD_REQUEST_CODE -> {
                    val selectedContact = result.data?.getParcelableArray("forwardContacts")
                    val selectedContactList =
                        selectedContact?.map { it as AllCeibroConnections.CeibroConnection }
                            ?.toMutableList()
                    val taskData = viewModel.taskDetail.value

                    if (!selectedContactList.isNullOrEmpty()) {
                        var state = "new"
                        if (taskData != null) {
                            state = if (viewModel.user?.id == taskData.creator.id) {
                                taskData.creatorState
                            } else {
                                taskData.assignedToState.find { it.userId == viewModel.user?.id }?.state
                                    ?: "new"
                            }
                        }
                        if (state.equals(TaskStatus.UNREAD.name, true)) {
                            state = "new"
                        }

                        val assignedToCeibroUsers =
                            selectedContactList.filter { it.isCeiborUser }
                                .map {
                                    ForwardTaskV2Request.AssignedToStateRequest(
                                        phoneNumber = it.phoneNumber,
                                        userId = it.userCeibroData?.id.toString(),
                                        state = state
                                    )
                                } ?: listOf()
                        val invitedNumbers = selectedContactList.filter { !it.isCeiborUser }
                            .map { it.phoneNumber } ?: listOf()


                        val forwardTaskRequest = ForwardTaskV2Request(
                            assignedToState = assignedToCeibroUsers,
                            invitedNumbers = invitedNumbers
                        )

                        viewModel.forwardTask(taskData?.id ?: "", forwardTaskRequest) { task ->

                        }
                    }
                }

                COMMENT_REQUEST_CODE -> {
                    val updatedTask = result.data?.getParcelable<CeibroTaskV2>("taskData")
                    if (updatedTask != null) {
                        viewModel._taskDetail.postValue(updatedTask)
                    }

                }
            }
        }
    }

}