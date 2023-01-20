package com.zstronics.ceibro.ui.tasks.task

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.databinding.FragmentTasksBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

    @Inject
    lateinit var adapter: TaskAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.tasks.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }
        mViewDataBinding.taskRV.adapter = adapter

        adapter.itemClickListener = { _: View, position: Int, data: CeibroTask ->
            navigateToTaskDetail(data)
        }

    }

    private fun navigateToTaskDetail(data: CeibroTask) {
        val bundle = Bundle()
        bundle.putParcelable("task", data)
        navigate(R.id.taskDetailFragment, bundle)
    }
}