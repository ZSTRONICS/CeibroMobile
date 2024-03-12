package com.zstronics.ceibro.ui.tasks.v3.fragments.approval

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentTaskV3ApprovalBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.v3.TasksParentTabV3VM
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.TagsBottomSheet
import com.zstronics.ceibro.ui.tasks.v3.fragments.TasksV3Adapter
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

//        parentViewModel.approvalAllTasks.observe(viewLifecycleOwner) {
//            if (parentViewModel.selectedTaskTypeApprovalState.value.equals(
//                    TaskRootStateTags.All.tagValue,
//                    true
//                )
//            ) {
//                parentViewModel.isFirstStartOfApprovalFragment = false
//                parentViewModel.filteredApprovalTasks = it
//
//                if (!it.isNullOrEmpty()) {
//                    adapter.setList(it, parentViewModel.selectedTaskTypeApprovalState.value ?: "")
//                    mViewDataBinding.taskOngoingRV.visibility = View.VISIBLE
//                    mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
//                    mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
//                } else {
//                    adapter.setList(listOf(), parentViewModel.selectedTaskTypeApprovalState.value ?: "")
//                    mViewDataBinding.taskOngoingRV.visibility = View.GONE
//                    if (parentViewModel.isSearchingTasks) {
//                        mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
//                        mViewDataBinding.searchWithNoResultLayout.visibility = View.VISIBLE
//                    } else {
//                        mViewDataBinding.noTaskInAllLayout.visibility = View.VISIBLE
//                        mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
//                    }
//                }
//            }
//        }

        parentViewModel.setFilteredDataToApprovalAdapter.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {

                adapter.setList(list, parentViewModel.selectedTaskTypeApprovalState.value ?: "")
                mViewDataBinding.taskOngoingRV.visibility = View.VISIBLE
                mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
            } else {
                adapter.setList(
                    listOf(),
                    parentViewModel.selectedTaskTypeApprovalState.value ?: ""
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
            val list: MutableList<CeibroTaskV2> = parentViewModel.filteredApprovalTasks
            parentViewModel.viewModelScope.launch {
                parentViewModel.loading(true, "")
                val sortedList = async { parentViewModel.sortList(list) }.await()
                val orderedList = async { parentViewModel.applySortingOrder(sortedList) }.await()

                parentViewModel.filteredApprovalTasks = orderedList

                parentViewModel.filterTasksList(parentViewModel.searchedText)
                parentViewModel.loading(false, "")
            }
        }

        parentViewModel.selectedTaskTypeApprovalState.observe(viewLifecycleOwner) { taskType ->
            var list: MutableList<CeibroTaskV2> = mutableListOf()

            if (taskType.equals(TaskRootStateTags.All.tagValue, true)) {
                list = parentViewModel.originalApprovalAllTasks

            } else if (taskType.equals(TaskRootStateTags.InReview.tagValue, true)) {
                list = parentViewModel.originalApprovalInReviewTasks

            } else if (taskType.equals(TaskRootStateTags.ToReview.tagValue, true)) {
                list = parentViewModel.originalApprovalToReviewTasks
            }

            parentViewModel.viewModelScope.launch {
                val sortedList = async { parentViewModel.sortList(list) }.await()
                val orderedList = async { parentViewModel.applySortingOrder(sortedList) }.await()

                parentViewModel.filteredApprovalTasks = orderedList

                parentViewModel.filterTasksList(parentViewModel.searchedText)
            }

        }


        parentViewModel.applyFilter.observe(viewLifecycleOwner) {
            if (it == true) {

                val taskType = parentViewModel.selectedTaskTypeApprovalState.value

                var list: MutableList<CeibroTaskV2> = mutableListOf()

                if (taskType.equals(TaskRootStateTags.All.tagValue, true)) {
                    list = parentViewModel.originalApprovalAllTasks

                } else if (taskType.equals(TaskRootStateTags.InReview.tagValue, true)) {
                    list = parentViewModel.originalApprovalInReviewTasks

                } else if (taskType.equals(TaskRootStateTags.ToReview.tagValue, true)) {
                    list = parentViewModel.originalApprovalToReviewTasks
                }

                parentViewModel.viewModelScope.launch {
                    val sortedList = async { parentViewModel.sortList(list) }.await()
                    val orderedList = async { parentViewModel.applySortingOrder(sortedList) }.await()

                    parentViewModel.filteredApprovalTasks = orderedList

                    parentViewModel.filterTasksList(parentViewModel.searchedText)
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

        adapter.menuClickListener =
            { v: View, position: Int, data: CeibroTaskV2 ->
                createPopupWindow(v, data) { menuTag ->
                    if (menuTag.equals("approveClose", true)) {

                    }
                    if (menuTag.equals("rejectReOpen", true)) {

                    }
                    if (menuTag.equals("rejectClose", true)) {

                    }
                }
            }
    }

    private fun createPopupWindow(
        v: View,
        data: CeibroTaskV2,
        callback: (String) -> Unit
    ): PopupWindow {
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.task_v3_approval_menu_dialog, null)

        val popupWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true


        val approveCloseBtn: TextView = view.findViewById(R.id.approveCloseBtn)
        val rejectReOpenBtn: TextView = view.findViewById(R.id.rejectReOpenBtn)
        val rejectCloseBtn: TextView = view.findViewById(R.id.rejectCloseBtn)

        approveCloseBtn.setOnClickListener {
            callback.invoke("approveClose")
            popupWindow.dismiss()
        }
        rejectReOpenBtn.setOnClickListener {
            callback.invoke("rejectReOpen")
            popupWindow.dismiss()
        }
        rejectCloseBtn.setOnClickListener {
            callback.invoke("rejectClose")
            popupWindow.dismiss()
        }

        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        if (positionOfIcon > height) {
            popupWindow.showAsDropDown(v, 0, -390)
        } else {
            popupWindow.showAsDropDown(v, 5, -10)
        }


        return popupWindow
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