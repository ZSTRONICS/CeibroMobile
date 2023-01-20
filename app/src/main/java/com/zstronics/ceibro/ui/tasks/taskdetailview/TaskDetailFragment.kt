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
                val taskStatusNameBg: Pair<Int, Int> = when (item.state.uppercase()) {
                    TaskStatus.DRAFT.name -> Pair(
                        R.drawable.status_draft_outline,
                        R.string.draft_heading
                    )
                    TaskStatus.ACTIVE.name, TaskStatus.ASSIGNED.name -> Pair(
                        R.drawable.status_ongoing_outline,
                        R.string.active_heading
                    )
                    TaskStatus.DONE.name -> Pair(
                        R.drawable.status_done_outline,
                        R.string.done_heading
                    )
                    else -> Pair(
                        R.drawable.status_draft_outline,
                        R.string.draft_heading
                    )
                }

                val (background, stringRes) = taskStatusNameBg
                taskDetailStatusName.setBackgroundResource(background)
                taskDetailStatusName.text = requireContext().getString(stringRes)

                taskDetailDueDate.text = DateUtils.reformatStringDate(
                    date = item.createdAt,
                    DateUtils.SERVER_DATE_FULL_FORMAT,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                )

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