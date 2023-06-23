package com.zstronics.ceibro.ui.tasks.v2.tasktome

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.databinding.FragmentTaskToMeBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskAdapter
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.tasktome.adapter.TaskToMeRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class TaskToMeFragment :
    BaseNavViewModelFragment<FragmentTaskToMeBinding, ITaskToMe.State, TaskToMeVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskToMeVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_to_me
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.createNewTaskBtn -> {
                navigate(R.id.newTaskV2Fragment)
            }
            R.id.newStateText -> {
                viewModel.selectedState = "new"
                val newTask = viewModel.newTasks.value
                if (!newTask.isNullOrEmpty()) {
                    adapter.setList(newTask)
                } else {
                    adapter.setList(listOf())
                }
                mViewDataBinding.newStateText.background = resources.getDrawable(R.drawable.status_new_filled_new)
                mViewDataBinding.ongoingStateText.background = resources.getDrawable(R.drawable.status_ongoing_outline_new)
                mViewDataBinding.doneStateText.background = resources.getDrawable(R.drawable.status_done_outline_new)
            }
            R.id.ongoingStateText -> {
                viewModel.selectedState = "ongoing"
                val ongoingTask = viewModel.ongoingTasks.value
                if (!ongoingTask.isNullOrEmpty()) {
                    adapter.setList(ongoingTask)
                } else {
                    adapter.setList(listOf())
                }
                mViewDataBinding.newStateText.background = resources.getDrawable(R.drawable.status_new_outline_new)
                mViewDataBinding.ongoingStateText.background = resources.getDrawable(R.drawable.status_ongoing_filled_new)
                mViewDataBinding.doneStateText.background = resources.getDrawable(R.drawable.status_done_outline_new)
            }
            R.id.doneStateText -> {
                viewModel.selectedState = "done"
                val doneTask = viewModel.doneTasks.value
                if (!doneTask.isNullOrEmpty()) {
                    adapter.setList(doneTask)
                } else {
                    adapter.setList(listOf())
                }
                mViewDataBinding.newStateText.background = resources.getDrawable(R.drawable.status_new_outline_new)
                mViewDataBinding.ongoingStateText.background = resources.getDrawable(R.drawable.status_ongoing_outline_new)
                mViewDataBinding.doneStateText.background = resources.getDrawable(R.drawable.status_done_filled_new)
            }
        }
    }




    @Inject
    lateinit var adapter: TaskToMeRVAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.allTasks.observe(viewLifecycleOwner) {
            mViewDataBinding.newStateCount.text = it.new.size.toString()
            mViewDataBinding.ongoingStateCount.text = it.ongoing.size.toString()
            mViewDataBinding.doneStateCount.text = it.done.size.toString()

        }

        viewModel.newTasks.observe(viewLifecycleOwner) {
            if (viewModel.selectedState.equals("new", true)) {
                if (!it.isNullOrEmpty()) {
                    adapter.setList(it)
                } else {
                    adapter.setList(listOf())
                }
            }
        }

        viewModel.ongoingTasks.observe(viewLifecycleOwner) {
            if (viewModel.selectedState.equals("ongoing", true)) {
                if (!it.isNullOrEmpty()) {
                    adapter.setList(it)
                } else {
                    adapter.setList(listOf())
                }
            }
        }

        viewModel.doneTasks.observe(viewLifecycleOwner) {
            if (viewModel.selectedState.equals("done", true)) {
                if (!it.isNullOrEmpty()) {
                    adapter.setList(it)
                } else {
                    adapter.setList(listOf())
                }
            }
        }

        mViewDataBinding.taskRV.adapter = adapter
        adapter.itemClickListener =
            { _: View, position: Int, data: CeibroTaskV2 ->
                val bundle = Bundle()
                bundle.putParcelable("taskDetail", data)
                navigate(R.id.taskDetailV2Fragment, bundle)
            }


        mViewDataBinding.taskToMeSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.searchTasks(query)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewModel.searchTasks(newText)
                }
                return true
            }
        })

    }





    private fun loadTasks(skeletonVisible: Boolean) {
        viewModel.loadAllTasks(skeletonVisible, mViewDataBinding.taskRV) {
            mViewDataBinding.taskRV.hideSkeleton()
//                val searchQuery = mViewDataBinding.projectSearchBar.query.toString()
//                if (searchQuery.isNotEmpty()) {
//                    viewModel.searchProject(searchQuery)
//                }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks(true)
    }




    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskCreatedEvent(event: LocalEvents.TaskCreatedEvent?) {
//        showToast("New Task Created")
        loadTasks(false)
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