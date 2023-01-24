package com.zstronics.ceibro.ui.tasks.taskdetailview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTaskDetailBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskAdapter
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TaskDetailFragment :
    BaseNavViewModelFragment<FragmentTaskDetailBinding, ITaskDetail.State, TaskDetailVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskDetailVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_detail
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> navigateBack()
            R.id.createSubTaskBtn -> navigateToNewSubTaskCreation()
        }
    }

    @Inject
    lateinit var adapter: SubTaskAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.subTasks.observe(viewLifecycleOwner) {
            adapter.setList(it)
            mViewDataBinding.allSubTaskCount.text = it.size.toString()
        }
        mViewDataBinding.taskDetailSubTasksRV.adapter = adapter

        viewModel.task.observe(viewLifecycleOwner) { item ->
            with(mViewDataBinding) {
                val taskStatusNameBg: Pair<Int, String> = when (item.state.uppercase()) {
                    TaskStatus.NEW.name -> Pair(
                        R.drawable.status_assigned_outline,
                        requireContext().getString(R.string.new_heading)
                    )
                    TaskStatus.ACTIVE.name -> Pair(
                        R.drawable.status_ongoing_outline,
                        requireContext().getString(R.string.active_heading)
                    )
                    TaskStatus.DRAFT.name -> Pair(
                        R.drawable.status_draft_outline,
                        requireContext().getString(R.string.draft_heading)
                    )
                    TaskStatus.DONE.name -> Pair(
                        R.drawable.status_done_outline,
                        requireContext().getString(R.string.done_heading)
                    )
                    else -> Pair(
                        R.drawable.status_draft_outline,
                        item.state
                    )
                }

                val (background, stringRes) = taskStatusNameBg
                taskDetailStatusName.setBackgroundResource(background)
                taskDetailStatusName.text = stringRes

                taskDetailDueDate.text = DateUtils.reformatStringDate(
                    date = item.dueDate,
                    DateUtils.FORMAT_YEAR_MON_DATE,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                )
                if (taskDetailDueDate.text == "") {                              // Checking if date format was not yyyy-MM-dd then it will be empty
                    taskDetailDueDate.text = DateUtils.reformatStringDate(
                        date = item.dueDate,
                        DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                        DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                    )
                    if (taskDetailDueDate.text == "") {                          // Checking if date format was not dd-MM-yyyy then still it is empty
                        taskDetailDueDate.text = "Invalid due date"
                    }
                }

                taskTitle.text = item.title

                taskDetailProjectName.text = item.project.title
                taskDetailSubTaskCount.text = "${item.totalSubTaskCount}/${item.totalSubTaskCount}"

            }
        }
    }

    private fun navigateToNewSubTaskCreation() {
        navigate(R.id.newSubTaskFragment, arguments)
    }
}