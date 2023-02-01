package com.zstronics.ceibro.ui.tasks.task

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.databinding.FragmentTasksBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskCreatedEvent(event: LocalEvents.TaskCreatedEvent?) {
        showToast("New Task Created")
        viewModel.getTasks()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}