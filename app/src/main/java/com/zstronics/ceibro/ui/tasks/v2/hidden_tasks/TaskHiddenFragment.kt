package com.zstronics.ceibro.ui.tasks.v2.hidden_tasks

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.TaskV2Response
import com.zstronics.ceibro.databinding.FragmentTaskHiddenBinding
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.hidden_tasks.adapter.HiddenRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class TaskHiddenFragment :
    BaseNavViewModelFragment<FragmentTaskHiddenBinding, ITaskHidden.State, TaskHiddenVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskHiddenVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_hidden
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.cancelledStateText -> {
                viewModel.selectedState = TaskStatus.CANCELED.name.lowercase()
                val cancelledTasks = viewModel.cancelledTasks.value
                if (!cancelledTasks.isNullOrEmpty()) {
                    adapter.setList(cancelledTasks)
                } else {
                    adapter.setList(listOf())
                }
                changeSelectedUserState()
            }
            R.id.ongoingStateText -> {
                viewModel.selectedState = TaskStatus.ONGOING.name.lowercase()
                val ongoingTask = viewModel.ongoingTasks.value
                if (!ongoingTask.isNullOrEmpty()) {
                    adapter.setList(ongoingTask)
                } else {
                    adapter.setList(listOf())
                }
                changeSelectedUserState()
            }
            R.id.doneStateText -> {
                viewModel.selectedState = TaskStatus.DONE.name.lowercase()
                val doneTask = viewModel.doneTasks.value
                if (!doneTask.isNullOrEmpty()) {
                    adapter.setList(doneTask)
                } else {
                    adapter.setList(listOf())
                }
                changeSelectedUserState()
            }
        }
    }


    @Inject
    lateinit var adapter: HiddenRVAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeSelectedUserState()

        viewModel.allTasks.observe(viewLifecycleOwner) {
            updateCount(it)
        }

        viewModel.cancelledTasks.observe(viewLifecycleOwner) {
            if (viewModel.selectedState.equals(TaskStatus.CANCELED.name, true)) {
                if (!it.isNullOrEmpty()) {
                    adapter.setList(it)
                } else {
                    adapter.setList(listOf())
                }
            }
        }

        viewModel.ongoingTasks.observe(viewLifecycleOwner) {
            if (viewModel.selectedState.equals(TaskStatus.ONGOING.name, true)) {
                if (!it.isNullOrEmpty()) {
                    adapter.setList(it)
                } else {
                    adapter.setList(listOf())
                }
            }
        }

        viewModel.doneTasks.observe(viewLifecycleOwner) {
            if (viewModel.selectedState.equals(TaskStatus.DONE.name, true)) {
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
                bundle.putString("rootState", TaskRootStateTags.Hidden.tagValue.lowercase())
                bundle.putString("selectedState", viewModel.selectedState)
                navigate(R.id.taskDetailV2Fragment, bundle)
            }
        adapter.itemLongClickListener =
            { _: View, position: Int, data: CeibroTaskV2 ->
                //user cannot hide a task of new state
                if (viewModel.selectedState.equals(TaskStatus.ONGOING.name, true) || viewModel.selectedState.equals(TaskStatus.DONE.name, true)) {
                    viewModel.showUnHideTaskDialog(requireContext(), data)
                }
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
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks(true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshTasksEvent(event: LocalEvents.RefreshTasksEvent?) {
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

    private fun updateCount(allTasks: TaskV2Response.AllTasks) {
        val canceledCount = allTasks.canceled.count { task -> viewModel.user?.id !in task.seenBy }
        val ongoingCount = allTasks.ongoing.count { task -> viewModel.user?.id !in task.seenBy }
        val doneCount = allTasks.done.count { task -> viewModel.user?.id !in task.seenBy }
        mViewDataBinding.cancelledStateCount.text = canceledCount.toString()
        mViewDataBinding.ongoingStateCount.text = ongoingCount.toString()
        mViewDataBinding.doneStateCount.text = doneCount.toString()

        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        sharedViewModel.isHiddenUnread.value = !(canceledCount == 0 && ongoingCount == 0 && doneCount == 0)
    }

    private fun changeSelectedUserState() {
        if (viewModel.selectedState.equals(TaskStatus.CANCELED.name.lowercase(), true)) {
            mViewDataBinding.cancelledStateText.background =
                resources.getDrawable(R.drawable.status_cancelled_fill)
            mViewDataBinding.ongoingStateText.background =
                resources.getDrawable(R.drawable.status_ongoing_outline_new)
            mViewDataBinding.doneStateText.background =
                resources.getDrawable(R.drawable.status_done_outline_new)
        }
        if (viewModel.selectedState.equals(TaskStatus.ONGOING.name.lowercase(), true)) {
            mViewDataBinding.cancelledStateText.background =
                resources.getDrawable(R.drawable.status_cancelled_outline)
            mViewDataBinding.ongoingStateText.background =
                resources.getDrawable(R.drawable.status_ongoing_filled_new)
            mViewDataBinding.doneStateText.background =
                resources.getDrawable(R.drawable.status_done_outline_new)
        }
        if (viewModel.selectedState.equals(TaskStatus.DONE.name.lowercase(), true)) {
            mViewDataBinding.cancelledStateText.background =
                resources.getDrawable(R.drawable.status_cancelled_outline)
            mViewDataBinding.ongoingStateText.background =
                resources.getDrawable(R.drawable.status_ongoing_outline_new)
            mViewDataBinding.doneStateText.background =
                resources.getDrawable(R.drawable.status_done_filled_new)
        }
    }
}