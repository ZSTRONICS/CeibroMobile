package com.zstronics.ceibro.ui.tasks.v2.tasktome

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.zstronics.ceibro.databinding.FragmentTaskToMeBinding
import com.zstronics.ceibro.ui.dashboard.SearchDataSingleton
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.tasktome.adapter.TaskToMeRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
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
            R.id.newStateText -> {
                viewModel.selectedState = TaskStatus.NEW.name.lowercase()
                val newTask = viewModel.newTasks.value
                if (!newTask.isNullOrEmpty()) {
                    adapter.setList(newTask)
                    mViewDataBinding.taskRV.visibility = View.VISIBLE
                } else {
                    adapter.setList(listOf())
                    mViewDataBinding.taskRV.visibility = View.GONE
                }
                mViewDataBinding.toMeOngoingInfoLayout.visibility = View.GONE
                mViewDataBinding.toMeDoneInfoLayout.visibility = View.GONE
                changeSelectedUserState()
                preSearch()
            }
            R.id.ongoingStateText -> {
                viewModel.selectedState = TaskStatus.ONGOING.name.lowercase()
                val ongoingTask = viewModel.ongoingTasks.value
                if (!ongoingTask.isNullOrEmpty()) {
                    adapter.setList(ongoingTask)
                    mViewDataBinding.taskRV.visibility = View.VISIBLE
                    mViewDataBinding.toMeOngoingInfoLayout.visibility = View.GONE
                    mViewDataBinding.toMeDoneInfoLayout.visibility = View.GONE
                } else {
                    adapter.setList(listOf())
                    mViewDataBinding.taskRV.visibility = View.GONE
                    mViewDataBinding.toMeOngoingInfoLayout.visibility = View.VISIBLE
                    mViewDataBinding.toMeDoneInfoLayout.visibility = View.GONE
                }
                changeSelectedUserState()
                preSearch()
            }
            R.id.doneStateText -> {
                viewModel.selectedState = TaskStatus.DONE.name.lowercase()
                val doneTask = viewModel.doneTasks.value
                if (!doneTask.isNullOrEmpty()) {
                    adapter.setList(doneTask)
                    mViewDataBinding.taskRV.visibility = View.VISIBLE
                    mViewDataBinding.toMeOngoingInfoLayout.visibility = View.GONE
                    mViewDataBinding.toMeDoneInfoLayout.visibility = View.GONE
                } else {
                    adapter.setList(listOf())
                    mViewDataBinding.taskRV.visibility = View.GONE
                    mViewDataBinding.toMeOngoingInfoLayout.visibility = View.GONE
                    mViewDataBinding.toMeDoneInfoLayout.visibility = View.VISIBLE
                }
                changeSelectedUserState()
                preSearch()
            }
        }
    }


    @Inject
    lateinit var adapter: TaskToMeRVAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeSelectedUserState()
        viewModel.allTasks.observe(viewLifecycleOwner) {
            if (it != null) {
                updateCount(it)
            }
        }

        viewModel.newTasks.observe(viewLifecycleOwner) {
            if (viewModel.selectedState.equals(TaskStatus.NEW.name.lowercase(), true)) {
                if (!it.isNullOrEmpty()) {
                    adapter.setList(it)
                    mViewDataBinding.taskRV.visibility = View.VISIBLE
                } else {
                    adapter.setList(listOf())
                    mViewDataBinding.taskRV.visibility = View.GONE
                }
                mViewDataBinding.toMeOngoingInfoLayout.visibility = View.GONE
                mViewDataBinding.toMeDoneInfoLayout.visibility = View.GONE
                changeSelectedUserState()
            }
        }

        viewModel.ongoingTasks.observe(viewLifecycleOwner) {
            if (viewModel.selectedState.equals(TaskStatus.ONGOING.name.lowercase(), true)) {
                if (!it.isNullOrEmpty()) {
                    adapter.setList(it)
                    mViewDataBinding.taskRV.visibility = View.VISIBLE
                    mViewDataBinding.toMeOngoingInfoLayout.visibility = View.GONE
                    mViewDataBinding.toMeDoneInfoLayout.visibility = View.GONE
                } else {
                    adapter.setList(listOf())
                    mViewDataBinding.taskRV.visibility = View.GONE
                    mViewDataBinding.toMeOngoingInfoLayout.visibility = View.VISIBLE
                    mViewDataBinding.toMeDoneInfoLayout.visibility = View.GONE
                }
                changeSelectedUserState()
            }
        }

        viewModel.doneTasks.observe(viewLifecycleOwner) {
            if (viewModel.selectedState.equals(TaskStatus.DONE.name.lowercase(), true)) {
                if (!it.isNullOrEmpty()) {
                    adapter.setList(it)
                    mViewDataBinding.taskRV.visibility = View.VISIBLE
                    mViewDataBinding.toMeOngoingInfoLayout.visibility = View.GONE
                    mViewDataBinding.toMeDoneInfoLayout.visibility = View.GONE
                } else {
                    adapter.setList(listOf())
                    mViewDataBinding.taskRV.visibility = View.GONE
                    mViewDataBinding.toMeOngoingInfoLayout.visibility = View.GONE
                    mViewDataBinding.toMeDoneInfoLayout.visibility = View.VISIBLE
                }
                changeSelectedUserState()
            }
        }

        viewModel.disabledNewState.observe(viewLifecycleOwner) {
            if (it) {
                if (viewModel.selectedState.equals(
                        TaskStatus.NEW.name.lowercase(),
                        true
                    )
                ) {  //if new state was selected then we have to change it because it is disabled now
                    viewModel.selectedState = TaskStatus.ONGOING.name.lowercase()
                }
                mViewDataBinding.newStateText.isEnabled = false
                mViewDataBinding.newStateText.isClickable = false
                mViewDataBinding.newStateText.setTextColor(resources.getColor(R.color.appGrey2))
            } else {
                mViewDataBinding.newStateText.isEnabled = true
                mViewDataBinding.newStateText.isClickable = true
                mViewDataBinding.newStateText.setTextColor(resources.getColor(R.color.black))
            }
        }

        mViewDataBinding.taskRV.adapter = adapter
        adapter.itemClickListener =
            { _: View, position: Int, data: CeibroTaskV2 ->
                val bundle = Bundle()
                bundle.putParcelable("taskDetail", data)
                bundle.putString("rootState", TaskRootStateTags.ToMe.tagValue.lowercase())
                bundle.putString("selectedState", viewModel.selectedState)
                navigate(R.id.taskDetailV2Fragment, bundle)
            }
        adapter.itemLongClickListener =
            { _: View, position: Int, data: CeibroTaskV2 ->
                //user cannot hide a task of new state
                if (viewModel.selectedState.equals(
                        TaskStatus.ONGOING.name,
                        true
                    ) || viewModel.selectedState.equals(TaskStatus.DONE.name, true)
                ) {
                    viewModel.showHideTaskDialog(requireContext(), data)
                }
            }
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
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        sharedViewModel.isToMeUnread.value = false
        viewModel.saveToMeUnread(false)
        preSearch()
    }

    private fun preSearch() {
        SearchDataSingleton.searchString.value?.let { searchedString ->
            mViewDataBinding.taskToMeSearchBar.setQuery(searchedString, true)
            mViewDataBinding.taskToMeSearchBar.clearFocus()

            // Post a delayed task to trigger the search after UI update
            mViewDataBinding.taskToMeSearchBar.post {
                viewModel.searchTasks(searchedString)
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
                    SearchDataSingleton.searchString.value = newText
                    viewModel.searchTasks(newText)
                }
                return true
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshTasksEvent(event: LocalEvents.RefreshTasksEvent?) {
//        showToast("New Task Created")
        loadTasks(false)
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        sharedViewModel.isToMeUnread.value = false
        viewModel.saveToMeUnread(false)
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
        val newCount = allTasks.new.count { task -> viewModel.user?.id !in task.seenBy }
        val ongoingCount = allTasks.ongoing.count { task -> viewModel.user?.id !in task.seenBy }
        val doneCount = allTasks.done.count { task -> viewModel.user?.id !in task.seenBy }
        mViewDataBinding.newStateCount.text =
            if (newCount > 99) {
                "99+"
            } else {
                "+$newCount"
            }
        mViewDataBinding.ongoingStateCount.text =
            if (ongoingCount > 99) {
                "99+"
            } else {
                "+$ongoingCount"
            }
        mViewDataBinding.doneStateCount.text =
            if (doneCount > 99) {
                "99+"
            } else {
                "+$doneCount"
            }

        mViewDataBinding.newStateCount.visibility =
            if (newCount == 0) {
                View.GONE
            } else {
                View.VISIBLE
            }
        mViewDataBinding.ongoingStateCount.visibility =
            if (ongoingCount == 0) {
                View.GONE
            } else {
                View.VISIBLE
            }
        mViewDataBinding.doneStateCount.visibility =
            if (doneCount == 0) {
                View.GONE
            } else {
                View.VISIBLE
            }

        if (allTasks.new.isEmpty()) {
            if (viewModel.selectedState.equals(
                    TaskStatus.NEW.name.lowercase(),
                    true
                )
            ) {  //if new state was selected then we have to change it because it is disabled now
                viewModel.selectedState = TaskStatus.ONGOING.name.lowercase()
            }
            mViewDataBinding.newStateText.isEnabled = false
            mViewDataBinding.newStateText.isClickable = false
            mViewDataBinding.newStateText.setTextColor(resources.getColor(R.color.appGrey2))
        } else {
            mViewDataBinding.newStateText.isEnabled = true
            mViewDataBinding.newStateText.isClickable = true
            mViewDataBinding.newStateText.setTextColor(resources.getColor(R.color.black))
        }
    }

    private fun changeSelectedUserState() {
        if (viewModel.selectedState.equals(TaskStatus.NEW.name.lowercase(), true)) {
            mViewDataBinding.newStateText.elevation = 15F
            mViewDataBinding.ongoingStateText.elevation = 0F
            mViewDataBinding.doneStateText.elevation = 0F

            mViewDataBinding.newStateText.background =
                resources.getDrawable(R.drawable.status_new_filled_new)
            mViewDataBinding.ongoingStateText.background =
                resources.getDrawable(R.drawable.status_ongoing_outline_new)
            mViewDataBinding.doneStateText.background =
                resources.getDrawable(R.drawable.status_done_outline_new)
        }
        if (viewModel.selectedState.equals(TaskStatus.ONGOING.name.lowercase(), true)) {
            mViewDataBinding.newStateText.elevation = 0F
            mViewDataBinding.ongoingStateText.elevation = 15F
            mViewDataBinding.doneStateText.elevation = 0F

            mViewDataBinding.newStateText.background =
                resources.getDrawable(R.drawable.status_new_outline_new)
            mViewDataBinding.ongoingStateText.background =
                resources.getDrawable(R.drawable.status_ongoing_filled_new)
            mViewDataBinding.doneStateText.background =
                resources.getDrawable(R.drawable.status_done_outline_new)
        }
        if (viewModel.selectedState.equals(TaskStatus.DONE.name.lowercase(), true)) {
            mViewDataBinding.newStateText.elevation = 0F
            mViewDataBinding.ongoingStateText.elevation = 0F
            mViewDataBinding.doneStateText.elevation = 15F

            mViewDataBinding.newStateText.background =
                resources.getDrawable(R.drawable.status_new_outline_new)
            mViewDataBinding.ongoingStateText.background =
                resources.getDrawable(R.drawable.status_ongoing_outline_new)
            mViewDataBinding.doneStateText.background =
                resources.getDrawable(R.drawable.status_done_filled_new)
        }
    }
}