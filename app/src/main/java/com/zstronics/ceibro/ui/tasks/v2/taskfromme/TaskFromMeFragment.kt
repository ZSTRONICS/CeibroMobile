package com.zstronics.ceibro.ui.tasks.v2.taskfromme

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
import com.zstronics.ceibro.databinding.FragmentTaskFromMeBinding
import com.zstronics.ceibro.ui.dashboard.SearchDataSingleton
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
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
            R.id.unreadStateText -> {
                viewModel.selectedState = TaskStatus.UNREAD.name.lowercase()
                val unreadTask = viewModel.unreadTasks.value
                if (!unreadTask.isNullOrEmpty()) {
                    adapter.setList(unreadTask)
                    mViewDataBinding.taskRV.visibility = View.VISIBLE
                    mViewDataBinding.fromMeLogoBackground.visibility = View.GONE
                } else {
                    adapter.setList(listOf())
                    mViewDataBinding.taskRV.visibility = View.GONE
                    mViewDataBinding.fromMeLogoBackground.visibility = View.VISIBLE
                }
                mViewDataBinding.fromMeOngoingInfoLayout.visibility = View.GONE
                mViewDataBinding.fromMeDoneInfoLayout.visibility = View.GONE
                changeSelectedUserState()
                preSearch()
            }
            R.id.ongoingStateText -> {
                viewModel.selectedState = TaskStatus.ONGOING.name.lowercase()
                val ongoingTask = viewModel.ongoingTasks.value
                if (!ongoingTask.isNullOrEmpty()) {
                    adapter.setList(ongoingTask)
                    mViewDataBinding.taskRV.visibility = View.VISIBLE
                    mViewDataBinding.fromMeOngoingInfoLayout.visibility = View.GONE
                    mViewDataBinding.fromMeDoneInfoLayout.visibility = View.GONE
                    mViewDataBinding.fromMeLogoBackground.visibility = View.GONE
                } else {
                    adapter.setList(listOf())
                    mViewDataBinding.taskRV.visibility = View.GONE
                    mViewDataBinding.fromMeOngoingInfoLayout.visibility = View.VISIBLE
                    mViewDataBinding.fromMeDoneInfoLayout.visibility = View.GONE
                    mViewDataBinding.fromMeLogoBackground.visibility = View.VISIBLE
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
                    mViewDataBinding.fromMeOngoingInfoLayout.visibility = View.GONE
                    mViewDataBinding.fromMeDoneInfoLayout.visibility = View.GONE
                    mViewDataBinding.fromMeLogoBackground.visibility = View.GONE
                } else {
                    adapter.setList(listOf())
                    mViewDataBinding.taskRV.visibility = View.GONE
                    mViewDataBinding.fromMeOngoingInfoLayout.visibility = View.GONE
                    mViewDataBinding.fromMeDoneInfoLayout.visibility = View.VISIBLE
                    mViewDataBinding.fromMeLogoBackground.visibility = View.VISIBLE
                }
                changeSelectedUserState()
                preSearch()
            }
        }
    }


    @Inject
    lateinit var adapter: TaskFromMeRVAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeSelectedUserState()
        viewModel.allTasks.observe(viewLifecycleOwner) {
            if (it != null) {
                updateCount(it)
            }
        }

        viewModel.unreadTasks.observe(viewLifecycleOwner) {
            if (viewModel.selectedState.equals(TaskStatus.UNREAD.name.lowercase(), true)) {
                if (!it.isNullOrEmpty()) {
                    adapter.setList(it)
                    mViewDataBinding.taskRV.visibility = View.VISIBLE
                    mViewDataBinding.fromMeLogoBackground.visibility = View.GONE
                } else {
                    adapter.setList(listOf())
                    mViewDataBinding.taskRV.visibility = View.GONE
                    mViewDataBinding.fromMeLogoBackground.visibility = View.VISIBLE
                }
                mViewDataBinding.fromMeOngoingInfoLayout.visibility = View.GONE
                mViewDataBinding.fromMeDoneInfoLayout.visibility = View.GONE
                changeSelectedUserState()
            }
        }

        viewModel.ongoingTasks.observe(viewLifecycleOwner) {
            if (viewModel.selectedState.equals(TaskStatus.ONGOING.name.lowercase(), true)) {
                if (!it.isNullOrEmpty()) {
                    adapter.setList(it)
                    mViewDataBinding.taskRV.visibility = View.VISIBLE
                    mViewDataBinding.fromMeOngoingInfoLayout.visibility = View.GONE
                    mViewDataBinding.fromMeLogoBackground.visibility = View.GONE
                } else {
                    adapter.setList(listOf())
                    mViewDataBinding.taskRV.visibility = View.GONE
                    mViewDataBinding.fromMeOngoingInfoLayout.visibility = View.VISIBLE
                    mViewDataBinding.fromMeLogoBackground.visibility = View.VISIBLE
                }
                mViewDataBinding.fromMeDoneInfoLayout.visibility = View.GONE
                changeSelectedUserState()
            }
        }

        viewModel.doneTasks.observe(viewLifecycleOwner) {
            if (viewModel.selectedState.equals(TaskStatus.DONE.name.lowercase(), true)) {
                if (!it.isNullOrEmpty()) {
                    adapter.setList(it)
                    mViewDataBinding.taskRV.visibility = View.VISIBLE
                    mViewDataBinding.fromMeDoneInfoLayout.visibility = View.GONE
                    mViewDataBinding.fromMeLogoBackground.visibility = View.GONE
                } else {
                    adapter.setList(listOf())
                    mViewDataBinding.taskRV.visibility = View.GONE
                    mViewDataBinding.fromMeDoneInfoLayout.visibility = View.VISIBLE
                    mViewDataBinding.fromMeLogoBackground.visibility = View.VISIBLE
                }
                mViewDataBinding.fromMeOngoingInfoLayout.visibility = View.GONE
                changeSelectedUserState()
            }
        }

        viewModel.disabledUnreadState.observe(viewLifecycleOwner) {
            if (it) {
                if (viewModel.selectedState.equals(
                        TaskStatus.UNREAD.name.lowercase(),
                        true
                    )
                ) {  //if unread state was selected then we have to change it because it is disabled now
                    viewModel.selectedState = TaskStatus.ONGOING.name.lowercase()
                }
                mViewDataBinding.unreadStateText.isEnabled = false
                mViewDataBinding.unreadStateText.isClickable = false
                mViewDataBinding.unreadStateText.setTextColor(resources.getColor(R.color.appGrey2))
            } else {
                mViewDataBinding.unreadStateText.isEnabled = true
                mViewDataBinding.unreadStateText.isClickable = true
                mViewDataBinding.unreadStateText.setTextColor(resources.getColor(R.color.black))
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
                if (viewModel.selectedState.equals(
                        TaskStatus.UNREAD.name,
                        true
                    ) || viewModel.selectedState.equals(TaskStatus.ONGOING.name, true)
                ) {
                    viewModel.showCancelTaskDialog(requireContext(), data) { }
                }
            }
    }

    private fun preSearch() {
        SearchDataSingleton.searchString.value?.let { searchedString ->
            mViewDataBinding.taskFromMeSearchBar.setQuery(searchedString, true)
            mViewDataBinding.taskFromMeSearchBar.clearFocus()

            // Post a delayed task to trigger the search after UI update
            mViewDataBinding.taskFromMeSearchBar.post {
                viewModel.searchTasks(searchedString)
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
                    SearchDataSingleton.searchString.value = newText
                    viewModel.searchTasks(newText)
                }
                return true
            }
        })
    }

    private fun loadTasks(skeletonVisible: Boolean) {
        viewModel.loadAllTasks(skeletonVisible, mViewDataBinding.taskRV) {
            mViewDataBinding.taskRV.hideSkeleton()
            preSearch()
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks(true)
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        sharedViewModel.isFromMeUnread.value = false
        viewModel.saveFromMeUnread(false)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshTasksEvent(event: LocalEvents.RefreshTasksEvent?) {
        loadTasks(false)
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        sharedViewModel.isFromMeUnread.value = false
        viewModel.saveFromMeUnread(false)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun updateCount(allTasks: MutableList<CeibroTaskV2>) {
        val unreadTaskList = allTasks.filter { it.fromMeState == TaskStatus.UNREAD.name.lowercase() }
        val unreadCount = allTasks.filter { it.fromMeState == TaskStatus.UNREAD.name.lowercase() }.count { task -> viewModel.user?.id !in task.seenBy }
        val ongoingCount = allTasks.filter { it.fromMeState == TaskStatus.ONGOING.name.lowercase() }.count { task -> viewModel.user?.id !in task.seenBy }
        val doneCount = allTasks.filter { it.fromMeState == TaskStatus.DONE.name.lowercase() }.count { task -> viewModel.user?.id !in task.seenBy }

        mViewDataBinding.unreadStateCount.text =
            if (unreadCount > 99) {
                "99+"
            } else {
                "$unreadCount"
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

        mViewDataBinding.unreadStateCount.visibility =
            if (unreadCount == 0) {
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

        if (unreadTaskList.isEmpty()) {
            if (viewModel.selectedState.equals(
                    TaskStatus.UNREAD.name.lowercase(),
                    true
                )
            ) {  //if unread state was selected then we have to change it because it is disabled now
                viewModel.selectedState = TaskStatus.ONGOING.name.lowercase()
            }
            mViewDataBinding.unreadStateText.isEnabled = false
            mViewDataBinding.unreadStateText.isClickable = false
            mViewDataBinding.unreadStateText.setTextColor(resources.getColor(R.color.appGrey2))
        } else {
            mViewDataBinding.unreadStateText.isEnabled = true
            mViewDataBinding.unreadStateText.isClickable = true
            mViewDataBinding.unreadStateText.setTextColor(resources.getColor(R.color.black))
        }
    }

    private fun changeSelectedUserState() {
        if (viewModel.selectedState.equals(TaskStatus.UNREAD.name.lowercase(), true)) {
            mViewDataBinding.unreadStateText.elevation = 15F
            mViewDataBinding.ongoingStateText.elevation = 0F
            mViewDataBinding.doneStateText.elevation = 0F

            mViewDataBinding.unreadStateText.background =
                resources.getDrawable(R.drawable.status_new_filled_new)
            mViewDataBinding.ongoingStateText.background =
                resources.getDrawable(R.drawable.status_ongoing_outline_new)
            mViewDataBinding.doneStateText.background =
                resources.getDrawable(R.drawable.status_done_outline_new)
        }
        if (viewModel.selectedState.equals(TaskStatus.ONGOING.name.lowercase(), true)) {
            mViewDataBinding.unreadStateText.elevation = 0F
            mViewDataBinding.ongoingStateText.elevation = 15F
            mViewDataBinding.doneStateText.elevation = 0F

            mViewDataBinding.unreadStateText.background =
                resources.getDrawable(R.drawable.status_new_outline_new)
            mViewDataBinding.ongoingStateText.background =
                resources.getDrawable(R.drawable.status_ongoing_filled_new)
            mViewDataBinding.doneStateText.background =
                resources.getDrawable(R.drawable.status_done_outline_new)
        }
        if (viewModel.selectedState.equals(TaskStatus.DONE.name.lowercase(), true)) {
            mViewDataBinding.unreadStateText.elevation = 0F
            mViewDataBinding.ongoingStateText.elevation = 0F
            mViewDataBinding.doneStateText.elevation = 15F

            mViewDataBinding.unreadStateText.background =
                resources.getDrawable(R.drawable.status_new_outline_new)
            mViewDataBinding.ongoingStateText.background =
                resources.getDrawable(R.drawable.status_ongoing_outline_new)
            mViewDataBinding.doneStateText.background =
                resources.getDrawable(R.drawable.status_done_filled_new)
        }
    }
}