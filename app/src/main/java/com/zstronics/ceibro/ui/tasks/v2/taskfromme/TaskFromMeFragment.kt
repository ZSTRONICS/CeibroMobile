package com.zstronics.ceibro.ui.tasks.v2.taskfromme

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentTaskFromMeBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.v2.taskfromme.adapter.TaskFromMeRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class TaskFromMeFragment :
    BaseNavViewModelFragment<FragmentTaskFromMeBinding, ITaskFromMe.State, TaskFromMeVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskFromMeVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_from_me
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.createNewTaskBtn -> {
                navigate(R.id.newTaskV2Fragment)
            }
            R.id.unreadStateText -> {
                viewModel.selectedState = "unread"
                val unreadTask = viewModel.unreadTasks.value
                if (!unreadTask.isNullOrEmpty()) {
                    adapter.setList(unreadTask)
                } else {
                    adapter.setList(listOf())
                }
                mViewDataBinding.unreadStateText.background =
                    resources.getDrawable(R.drawable.status_new_filled_new)
                mViewDataBinding.ongoingStateText.background =
                    resources.getDrawable(R.drawable.status_ongoing_outline_new)
                mViewDataBinding.doneStateText.background =
                    resources.getDrawable(R.drawable.status_done_outline_new)
            }
            R.id.ongoingStateText -> {
                viewModel.selectedState = "ongoing"
                val ongoingTask = viewModel.ongoingTasks.value
                if (!ongoingTask.isNullOrEmpty()) {
                    adapter.setList(ongoingTask)
                } else {
                    adapter.setList(listOf())
                }
                mViewDataBinding.unreadStateText.background =
                    resources.getDrawable(R.drawable.status_new_outline_new)
                mViewDataBinding.ongoingStateText.background =
                    resources.getDrawable(R.drawable.status_ongoing_filled_new)
                mViewDataBinding.doneStateText.background =
                    resources.getDrawable(R.drawable.status_done_outline_new)
            }
            R.id.doneStateText -> {
                viewModel.selectedState = "done"
                val doneTask = viewModel.doneTasks.value
                if (!doneTask.isNullOrEmpty()) {
                    adapter.setList(doneTask)
                } else {
                    adapter.setList(listOf())
                }
                mViewDataBinding.unreadStateText.background =
                    resources.getDrawable(R.drawable.status_new_outline_new)
                mViewDataBinding.ongoingStateText.background =
                    resources.getDrawable(R.drawable.status_ongoing_outline_new)
                mViewDataBinding.doneStateText.background =
                    resources.getDrawable(R.drawable.status_done_filled_new)
            }
        }
    }


    @Inject
    lateinit var adapter: TaskFromMeRVAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.allTasks.observe(viewLifecycleOwner) {
            mViewDataBinding.unreadStateCount.text = it.unread.size.toString()
            mViewDataBinding.ongoingStateCount.text = it.ongoing.size.toString()
            mViewDataBinding.doneStateCount.text = it.done.size.toString()

        }

        viewModel.unreadTasks.observe(viewLifecycleOwner) {
            if (viewModel.selectedState.equals("unread", true)) {
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
                bundle.putString("rootState", TaskRootStateTags.FromMe.tagValue.lowercase())
                bundle.putString("selectedState", viewModel.selectedState)
                navigate(R.id.taskDetailV2Fragment, bundle)
            }
        adapter.itemLongClickListener =
            { _: View, position: Int, data: CeibroTaskV2 ->
                //creator cannot cancel a task which is already in done state
                if (viewModel.selectedState.equals("unread", true) || viewModel.selectedState.equals("ongoing", true)) {
                    viewModel.showCancelTaskDialog(requireContext(), data)
                }
            }


        mViewDataBinding.taskFromMeSearchBar.setOnQueryTextListener(object :
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

}