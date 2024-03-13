package com.zstronics.ceibro.ui.tasks.v3.hidden.fragments.closed

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentTaskV3HiddenClosedBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.v3.TasksParentTabV3VM
import com.zstronics.ceibro.ui.tasks.v3.fragments.TasksV3Adapter
import com.zstronics.ceibro.ui.tasks.v3.hidden.TasksHiddenParentTabV3VM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class TaskV3HiddenClosedFragment :
    BaseNavViewModelFragment<FragmentTaskV3HiddenClosedBinding, ITaskV3HiddenClosed.State, TaskV3HiddenClosedVM>() {
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskV3HiddenClosedVM by viewModels()
    private lateinit var parentViewModel: TasksHiddenParentTabV3VM
    override val layoutResId: Int = R.layout.fragment_task_v3_hidden_closed
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {


        }
    }

    companion object {
        fun newInstance(viewModel: TasksHiddenParentTabV3VM): TaskV3HiddenClosedFragment {
            val fragment = TaskV3HiddenClosedFragment()
            fragment.parentViewModel = viewModel
            return fragment
        }
    }

    @Inject
    lateinit var adapter: TasksV3Adapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.taskOngoingRV.adapter = adapter


        parentViewModel.closedAllTasks.observe(viewLifecycleOwner) {
            if (parentViewModel.applyFilter.value == true) {
                parentViewModel._applyFilter.value = true
            } else {
                if (parentViewModel.selectedTaskTypeClosedState.value.equals(
                        TaskRootStateTags.All.tagValue,
                        true
                    )
                ) {
                    parentViewModel.isFirstStartOfClosedFragment = false
                    parentViewModel.filteredClosedTasks = it
                    if (!it.isNullOrEmpty()) {
                        adapter.setList(it, parentViewModel.selectedTaskTypeClosedState.value ?: "")
                        mViewDataBinding.taskOngoingRV.visibility = View.VISIBLE
                        mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                        mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
                    } else {
                        adapter.setList(
                            listOf(),
                            parentViewModel.selectedTaskTypeClosedState.value ?: ""
                        )
                        mViewDataBinding.taskOngoingRV.visibility = View.GONE
                        if (parentViewModel.isSearchingTasks) {
                            mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                            mViewDataBinding.searchWithNoResultLayout.visibility = View.VISIBLE
                        } else {
                            mViewDataBinding.noTaskInAllLayout.visibility = View.VISIBLE
                            mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
                        }
                    }
                }
            }
        }

        parentViewModel.setFilteredDataToCloseAdapter.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {

                adapter.setList(list, parentViewModel.selectedTaskTypeClosedState.value ?: "")
                mViewDataBinding.taskOngoingRV.visibility = View.VISIBLE
                mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
            } else {
                adapter.setList(
                    listOf(),
                    parentViewModel.selectedTaskTypeClosedState.value ?: ""
                )
                mViewDataBinding.taskOngoingRV.visibility = View.GONE
                if (parentViewModel.isSearchingTasks) {
                    mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                    mViewDataBinding.searchWithNoResultLayout.visibility = View.VISIBLE
                } else {
                    mViewDataBinding.noTaskInAllLayout.visibility = View.VISIBLE
                    mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
                }
            }
        }


        parentViewModel.lastSortingType.observe(viewLifecycleOwner) { sortingType ->
            if (parentViewModel.isFirstStartOfClosedFragment.not()) {
                val list: MutableList<CeibroTaskV2> = parentViewModel.filteredClosedTasks
                parentViewModel.viewModelScope.launch {
                    viewModel.loading(true, "")
                    val sortedList = async { parentViewModel.sortList(list) }.await()
                    val orderedList =
                        async { parentViewModel.applySortingOrder(sortedList) }.await()

                    parentViewModel.filteredClosedTasks = orderedList

                    parentViewModel.filterTasksList(parentViewModel.searchedText)
                    viewModel.loading(false, "")
                }
            }
        }

        parentViewModel.selectedTaskTypeClosedState.observe(viewLifecycleOwner) { taskType ->
            parentViewModel.isFirstStartOfClosedFragment = false
            var list: MutableList<CeibroTaskV2> = mutableListOf()

            if (taskType.equals(TaskRootStateTags.All.tagValue, true)) {
                list = parentViewModel.originalClosedAllTasks

            } else if (taskType.equals(TaskRootStateTags.ToMe.tagValue, true)) {
                list = parentViewModel.originalClosedToMeTasks

            } else if (taskType.equals(TaskRootStateTags.FromMe.tagValue, true)) {
                list = parentViewModel.originalClosedFromMeTasks
            }

            parentViewModel.viewModelScope.launch {
                val sortedList = async { parentViewModel.sortList(list) }.await()
                val orderedList = async { parentViewModel.applySortingOrder(sortedList) }.await()

                parentViewModel.filteredClosedTasks = orderedList

                parentViewModel.filterTasksList(parentViewModel.searchedText)
            }
        }

        parentViewModel.applyFilter.observe(viewLifecycleOwner) {
            if (parentViewModel.isFirstStartOfClosedFragment.not()) {
                if (it == true) {

                    val taskType = parentViewModel.selectedTaskTypeClosedState.value

                    var list: MutableList<CeibroTaskV2> = mutableListOf()

                    if (taskType.equals(TaskRootStateTags.All.tagValue, true)) {
                        list = parentViewModel.originalClosedAllTasks

                    } else if (taskType.equals(TaskRootStateTags.ToMe.tagValue, true)) {
                        list = parentViewModel.originalClosedToMeTasks

                    } else if (taskType.equals(TaskRootStateTags.FromMe.tagValue, true)) {
                        list = parentViewModel.originalClosedFromMeTasks
                    }

                    parentViewModel.viewModelScope.launch {
                        val sortedList = async { parentViewModel.sortList(list) }.await()
                        val orderedList =
                            async { parentViewModel.applySortingOrder(sortedList) }.await()

                        parentViewModel.filteredClosedTasks = orderedList

                        parentViewModel.filterTasksList(parentViewModel.searchedText)
                    }

                }
            }
        }




        adapter.itemClickListener =
            { _: View, position: Int, data: CeibroTaskV2 ->
                if (data.eventsCount > 30) {
                    viewModel.loading(true, "")
                }
                viewModel.launch {
                    val allEvents = viewModel.taskDao.getEventsOfTask(data.id)
                    CeibroApplication.CookiesManager.taskDataForDetails = data
                    CeibroApplication.CookiesManager.taskDetailEvents = allEvents
                    CeibroApplication.CookiesManager.taskDetailRootState =
                        parentViewModel.selectedTaskTypeClosedState.value
                    CeibroApplication.CookiesManager.taskDetailSelectedSubState = ""
//                    bundle.putParcelable("taskDetail", data)
//                    bundle.putParcelableArrayList("eventsArray", ArrayList(allEvents))
//                    bundle.putString("rootState", TaskRootStateTags.ToMe.tagValue.lowercase())
//                    bundle.putString("selectedState", viewModel.selectedState)
                    withContext(Dispatchers.Main) {
                        // Update the UI here
                        navigate(R.id.taskDetailTabV2Fragment)
                        viewModel.loading(false, "")
                    }
                }
            }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshTasksData(event: LocalEvents.RefreshTasksData?) {
        parentViewModel.loadAllTasks {

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }


}