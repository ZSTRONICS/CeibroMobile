package com.zstronics.ceibro.ui.tasks.subtaskdetailview

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.base.extensions.toast
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.auth.login.User
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.databinding.FragmentSubTaskDetailBinding
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment
import com.zstronics.ceibro.ui.tasks.newsubtask.AttachmentAdapter
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus.Companion.stateToHeadingAndBg
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubTaskDetailFragment :
    BaseNavViewModelFragment<FragmentSubTaskDetailBinding, ISubTaskDetail.State, SubTaskDetailVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: SubTaskDetailVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_sub_task_detail
    override fun toolBarVisibility(): Boolean = false
    lateinit var userData: User
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
            R.id.subTaskViewCommentsBtn -> navigateToAllComments()
            R.id.subTaskRejectionsBtn -> navigateToRejections()
            R.id.subTaskAttachmentsBtn -> viewModel.subtask.value?.id?.let {
                navigateToAttachments(
                    it
                )
            }
            R.id.subTaskDescriptionShowMoreBtn -> {
                if (mViewDataBinding.subTaskDescriptionText.maxLines == 4) {
                    mViewDataBinding.subTaskDescriptionText.maxLines = Integer.MAX_VALUE
                    mViewDataBinding.subTaskDescriptionShowMoreBtn.text =
                        resources.getString(R.string.show_less_heading)
                } else {
                    mViewDataBinding.subTaskDescriptionText.maxLines = 4
                    mViewDataBinding.subTaskDescriptionShowMoreBtn.text =
                        resources.getString(R.string.show_more_heading)
                }
            }
            R.id.subTaskCommentAttachmentBtn -> pickAttachment(true)
            R.id.sendCommentBtn -> {
                val message: String = mViewDataBinding.subTaskCommentField.text.toString()
                if (viewModel.fileUriList.value?.isNotEmpty() == true || message.isNotEmpty())
                    viewModel.postComment(message, requireContext()) {
                        mViewDataBinding.subTaskCommentField.setText("")

                        if (viewModel.fileUriList.value?.isNotEmpty() == true) {
                            createNotification(
                                it?.id,
                                AttachmentModules.SubTaskComments.name,
                                "Uploading files in comment",
                                isOngoing = true,
                                indeterminate = true
                            )
                        }

                    }
                else {
                    toast("You need to write something in comments or attach a file")
                }
            }
        }
    }


    @Inject
    lateinit var attachmentAdapter: AttachmentAdapter

    @Inject
    lateinit var commentsAdapter: CommentsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.commentAttachmentRecyclerView.adapter = attachmentAdapter
        mViewDataBinding.subTaskCommentsRV.adapter = commentsAdapter

        viewModel.fileUriList.observe(viewLifecycleOwner) { list ->
            if (list != null) {
                attachmentAdapter.setList(list)
            }
        }
        attachmentAdapter.itemClickListener =
            { _: View, position: Int, data: SubtaskAttachment? ->
                viewModel.removeFile(position)
            }

        commentsAdapter.itemClickListener =
            { _: View, position: Int, data: SubTaskComments ->
                arguments?.putString("moduleType", AttachmentModules.SubTaskComments.name)
                arguments?.putString("moduleId", data.id)
                arguments?.putParcelable("SubTaskComments", data)
                navigate(R.id.attachmentFragment, arguments)
            }

        viewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                userData = it
            }
        }

        viewModel.recentComments.observe(viewLifecycleOwner) { comments ->
            commentsAdapter.setList(comments)
        }
        viewModel.subtask.observe(viewLifecycleOwner) { item ->
            with(mViewDataBinding) {
                if (item != null) {
                    taskTitle.text = item.taskData?.title

                    val state =
                        item.state?.find { it.userId == userData.id }?.userState?.uppercase()

                    val subTaskStatusNameBg: Pair<Int, SubTaskStatus>? =
                        state?.stateToHeadingAndBg()
                    subTaskStatusNameBg?.let {
                        val (background, heading) = subTaskStatusNameBg
                        subTaskStatusName.setBackgroundResource(background)
                        subTaskStatusName.text = heading.name.toCamelCase()
                    }


                    subTaskDueDate.text = DateUtils.reformatStringDate(
                        date = item.dueDate,
                        DateUtils.FORMAT_YEAR_MON_DATE,
                        DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                    )
                    if (subTaskDueDate.text == "") {                              // Checking if date format was not yyyy-MM-dd then it will be empty
                        subTaskDueDate.text = DateUtils.reformatStringDate(
                            date = item.dueDate,
                            DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                            DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                        )
                        if (subTaskDueDate.text == "") {                          // Checking if date format was not dd-MM-yyyy then still it is empty
                            subTaskDueDate.text =
                                requireContext().getString(R.string.invalid_due_date_text)
                        }
                    }
                    subTaskCreationDate.text = DateUtils.reformatStringDate(
                        date = item.createdAt,
                        DateUtils.SERVER_DATE_FULL_FORMAT,
                        DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                    )

                    subTaskTitleName.text = item.title
                    subTaskProjectName.text = item.taskData?.project?.title
                    subTaskCreatorName.text = item.creator.firstName + " " + item.creator.surName


                    val assignMembers: ArrayList<TaskMember> = ArrayList()
                    for (assign in item.assignedTo) {
                        for (member in assign.members) {
                            assignMembers.add(member)
                        }
                    }
                    if (assignMembers.isNotEmpty()) {
                        subTaskAssignToName.text = ""
                        var count = 0
                        for (member in assignMembers) {
                            count++
                            if (count == assignMembers.size) {
                                subTaskAssignToName.append("${member.firstName} ${member.surName}")
                            } else {
                                subTaskAssignToName.append("${member.firstName} ${member.surName}, ")
                            }
                        }
                    } else {
                        subTaskAssignToName.text =
                            requireContext().getString(R.string.no_user_assigned_text)
                    }

                    if (item.description?.isNotEmpty() == true) {
                        subTaskDescriptionText.text = item.description
                    } else {
                        subTaskDescriptionText.text =
                            requireContext().getString(R.string.no_description_added_by_creator_text)
                    }
                    subTaskDescriptionText.post(Runnable {
                        if (subTaskDescriptionText.lineCount <= 4) {
                            subTaskDescriptionShowMoreBtn.visibility = View.GONE
                        } else {
                            subTaskDescriptionShowMoreBtn.visibility = View.VISIBLE
                        }
                    })
                }
            }
        }


        mViewDataBinding.subTaskCommentField.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
            return@setOnTouchListener false
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.onFirsTimeUiCreate(arguments)
    }

    private fun navigateToAllComments() {
        navigate(R.id.subTaskCommentsFragment)
    }

    private fun navigateToRejections() {
        navigate(R.id.subTaskRejectionFragment)
    }

    private fun navigateToAttachments(moduleId: String) {
        arguments?.putString("moduleType", AttachmentModules.SubTask.name)
        arguments?.putString("moduleId", moduleId)
        navigate(R.id.attachmentFragment, arguments)
    }
}