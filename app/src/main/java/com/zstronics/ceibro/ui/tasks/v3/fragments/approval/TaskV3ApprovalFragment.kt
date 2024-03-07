package com.zstronics.ceibro.ui.tasks.v3.fragments.approval

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentTaskV3ApprovalBinding
import com.zstronics.ceibro.databinding.FragmentTaskV3OngoingBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.v3.TasksParentTabV3VM
import com.zstronics.ceibro.ui.tasks.v3.fragments.TasksV3Adapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class TaskV3ApprovalFragment :
    BaseNavViewModelFragment<FragmentTaskV3ApprovalBinding, ITaskV3Approval.State, TaskV3ApprovalVM>() {
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskV3ApprovalVM by viewModels()
    private lateinit var parentViewModel: TasksParentTabV3VM
    override val layoutResId: Int = R.layout.fragment_task_v3_approval
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {


        }
    }
    companion object {
        fun newInstance(viewModel: TasksParentTabV3VM): TaskV3ApprovalFragment {
            val fragment = TaskV3ApprovalFragment()
            fragment.parentViewModel = viewModel
            return fragment
        }
    }

    @Inject
    lateinit var adapter: TasksV3Adapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.taskOngoingRV.adapter = adapter



        parentViewModel.selectedTaskTypeApprovalState.observe(viewLifecycleOwner) { taskType ->
            var list: MutableList<CeibroTaskV2> = mutableListOf()

            if (taskType.equals(TaskRootStateTags.All.tagValue, true)) {
                list = parentViewModel.originalApprovalAllTasks

            } else if (taskType.equals(TaskRootStateTags.InReview.tagValue, true)) {
                list = parentViewModel.originalApprovalInReviewTasks

            } else if (taskType.equals(TaskRootStateTags.ToReview.tagValue, true)) {
                list = parentViewModel.originalApprovalToReviewTasks
            }

            list=  parentViewModel.sortList(list)

            if (list.isNotEmpty()) {

                adapter.setList(list, parentViewModel.selectedTaskTypeApprovalState.value ?: "")
                mViewDataBinding.taskOngoingRV.visibility = View.VISIBLE
                mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
            } else {
                adapter.setList(listOf(), parentViewModel.selectedTaskTypeApprovalState.value ?: "")
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



        parentViewModel.approvalAllTasks.observe(viewLifecycleOwner) {
            if (parentViewModel.selectedTaskTypeApprovalState.value.equals(
                    TaskRootStateTags.All.tagValue,
                    true
                )
            ) {
                if (!it.isNullOrEmpty()) {
                    adapter.setList(it, parentViewModel.selectedTaskTypeApprovalState.value ?: "")
                    mViewDataBinding.taskOngoingRV.visibility = View.VISIBLE
                    mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                    mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
                } else {
                    adapter.setList(listOf(), parentViewModel.selectedTaskTypeApprovalState.value ?: "")
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


        adapter.itemClickListener =
            { _: View, position: Int, data: CeibroTaskV2 ->
                if (data.eventsCount > 30) {
                    viewModel.loading(true, "")
                }
                viewModel.launch {
                    val allEvents = viewModel.taskDao.getEventsOfTask(data.id)
                    CeibroApplication.CookiesManager.taskDataForDetails = data
                    CeibroApplication.CookiesManager.taskDetailEvents = allEvents
                    CeibroApplication.CookiesManager.taskDetailRootState = parentViewModel.selectedTaskTypeApprovalState.value
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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

}