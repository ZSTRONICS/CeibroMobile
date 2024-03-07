package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentTaskV3OngoingBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.v3.ITasksParentTabV3
import com.zstronics.ceibro.ui.tasks.v3.TasksParentTabV3VM
import com.zstronics.ceibro.ui.tasks.v3.fragments.TasksV3Adapter
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class TaskV3OngoingFragment :
    BaseNavViewModelFragment<FragmentTaskV3OngoingBinding, ITasksParentTabV3.State, TasksParentTabV3VM>() {
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TasksParentTabV3VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_v3_ongoing
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {


        }
    }

    @Inject
    lateinit var adapter: TasksV3Adapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.taskOngoingRV.adapter = adapter


        viewModel.selectedTaskTypeState.observe(viewLifecycleOwner) { tag ->
            var list: MutableList<CeibroTaskV2> = mutableListOf()

            if (tag.equals(TaskRootStateTags.All.tagValue, true)) {
                viewModel.ongoingToMeTasks.value?.let {
                    list = it
                }
            } else if (tag.equals(TaskRootStateTags.FromMe.tagValue, true)) {
                viewModel.ongoingFromMeTasks.value?.let {
                    list = it
                }
            } else if (tag.equals(TaskRootStateTags.ToMe.tagValue, true)) {
                viewModel.ongoingToMeTasks.value?.let {
                    list = it
                }
            }
            if (!list.isNullOrEmpty()) {

                adapter.setList(list, viewModel.selectedTaskTypeState.value ?: "")
                mViewDataBinding.taskOngoingRV.visibility = View.VISIBLE
                mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
            } else {
                adapter.setList(listOf(), viewModel.selectedTaskTypeState.value ?: "")
                mViewDataBinding.taskOngoingRV.visibility = View.GONE
                if (viewModel.isSearchingTasks) {
                    mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                    mViewDataBinding.searchWithNoResultLayout.visibility = View.VISIBLE
                } else {
                    mViewDataBinding.noTaskInAllLayout.visibility = View.VISIBLE
                    mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
                }
            }
        }



    viewModel.ongoingAllTasks.observe(viewLifecycleOwner)
    {
        if (viewModel.selectedTaskTypeState.value.equals(TaskRootStateTags.All.tagValue, true)) {
            if (!it.isNullOrEmpty()) {
                adapter.setList(it, viewModel.selectedTaskTypeState.value ?: "")
                mViewDataBinding.taskOngoingRV.visibility = View.VISIBLE
                mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
            } else {
                adapter.setList(listOf(), viewModel.selectedTaskTypeState.value ?: "")
                mViewDataBinding.taskOngoingRV.visibility = View.GONE
                if (viewModel.isSearchingTasks) {
                    mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                    mViewDataBinding.searchWithNoResultLayout.visibility = View.VISIBLE
                } else {
                    mViewDataBinding.noTaskInAllLayout.visibility = View.VISIBLE
                    mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
                }
            }
        }
    }


    if (viewModel.isFirstStartOfOngoingFragment)
    {
        viewModel.isFirstStartOfOngoingFragment = false
//            viewModel.ongoingAllTasks.value?.let {
//                if (viewModel.selectedTaskTypeState.equals(TaskRootStateTags.All.tagValue, true)) {
//                    if (it.isNotEmpty()) {
//                        adapter.setList(it, viewModel.selectedTaskTypeState)
//                        mViewDataBinding.taskOngoingRV.visibility = View.VISIBLE
//                        mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
//                        mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
//                    } else {
//                        adapter.setList(listOf(), viewModel.selectedTaskTypeState)
//                        mViewDataBinding.taskOngoingRV.visibility = View.GONE
//                        if (viewModel.isSearchingTasks) {
//                            mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
//                            mViewDataBinding.searchWithNoResultLayout.visibility = View.VISIBLE
//                        } else {
//                            mViewDataBinding.noTaskInAllLayout.visibility = View.VISIBLE
//                            mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
//                        }
//                    }
//
//                }
//            } ?: kotlin.run {
//                shortToastNow("All tasks list is empty ${viewModel.ongoingAllTasks.value?.size}")
//            }
    }

}


@Subscribe(threadMode = ThreadMode.MAIN)
fun onRefreshTasksData(event: LocalEvents.RefreshTasksData?) {
    viewModel.loadAllTasks {

    }
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