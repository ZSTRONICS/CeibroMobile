package com.zstronics.ceibro.ui.tasks.task

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTasksBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksFragment :
    BaseNavViewModelFragment<FragmentTasksBinding, ITasks.State, TasksVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TasksVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_tasks
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.createTaskBtn -> navigateToNewTaskCreation()
        }
    }

    private fun navigateToNewTaskCreation() {
        navigate(R.id.newTaskFragment)
    }
}