package com.zstronics.ceibro.ui.tasks

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentMainTasksBinding
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskFragment
import com.zstronics.ceibro.ui.tasks.task.FragmentTaskFilterSheet
import com.zstronics.ceibro.ui.tasks.task.TasksFragment
import com.zstronics.ceibro.ui.works.WorksFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainTasksFragment :
    BaseNavViewModelFragment<FragmentMainTasksBinding, IMainTasks.State, MainTasksVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: MainTasksVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_main_tasks
    var selectedFragment = "TasksFragment"
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.taskHeading -> {
                if (selectedFragment.equals("SubTaskFragment")) {
                    childFragmentManager.beginTransaction().replace(R.id.task_fragment_container, TasksFragment())
                        .commit()
                    selectedFragment = "TasksFragment"
                    mViewDataBinding.subTaskHeading.setBackgroundResource(0)
                    mViewDataBinding.taskHeading.setBackgroundResource(R.drawable.taskselectedback)
                }
            }
            R.id.subTaskHeading -> {
                if (selectedFragment.equals("TasksFragment")) {
                    childFragmentManager.beginTransaction().replace(R.id.task_fragment_container, SubTaskFragment())
                        .commit()
                    selectedFragment = "SubTaskFragment"
                    mViewDataBinding.taskHeading.setBackgroundResource(0)
                    mViewDataBinding.subTaskHeading.setBackgroundResource(R.drawable.taskselectedback)
                }
            }
            R.id.taskFilterBtn -> showTaskFilterSheet()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.beginTransaction().replace(R.id.task_fragment_container, TasksFragment())
            .commit()
        selectedFragment = "TasksFragment"
        mViewDataBinding.taskHeading.setBackgroundResource(R.drawable.taskselectedback)
    }

    private fun showTaskFilterSheet() {
//        viewModel.task.value?.let {
        val fragment = FragmentTaskFilterSheet()

//            fragment.onSeeAttachment = {
//                navigateToAttachments(it._id)
//            }
        fragment.show(childFragmentManager, "FragmentTaskFilterSheet")
//        }
    }
}