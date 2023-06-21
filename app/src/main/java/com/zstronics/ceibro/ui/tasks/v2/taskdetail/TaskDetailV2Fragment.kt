package com.zstronics.ceibro.ui.tasks.v2.taskdetail

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTaskDetailV2Binding
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.FilesRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.ImageWithCommentRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.OnlyImageRVAdapter
import com.zstronics.ceibro.utils.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TaskDetailV2Fragment :
    BaseNavViewModelFragment<FragmentTaskDetailV2Binding, ITaskDetailV2.State, TaskDetailV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskDetailV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_detail_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> navigateBack()
            R.id.taskInfoBtn -> showTaskInfoBottomSheet()
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
        }
    }


    @Inject
    lateinit var onlyImageAdapter: OnlyImageRVAdapter

    @Inject
    lateinit var imageWithCommentAdapter: ImageWithCommentRVAdapter

    @Inject
    lateinit var filesAdapter: FilesRVAdapter

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
            state = if (viewModel.user?.id == item.creator.id) {
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

}