package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
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
import com.zstronics.ceibro.databinding.FragmentTaskV3OngoingBinding
import com.zstronics.ceibro.ui.tasks.v3.TasksParentTabV3VM
import com.zstronics.ceibro.ui.tasks.v3.fragments.TasksV3Adapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class TaskV3OngoingFragment :
    BaseNavViewModelFragment<FragmentTaskV3OngoingBinding, ITaskV3Ongoing.State, TaskV3OngoingVM>() {
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskV3OngoingVM by viewModels()
    private var parentViewModel: TasksParentTabV3VM? = null
    override val layoutResId: Int = R.layout.fragment_task_v3_ongoing
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.clearSearch -> {
                clearSearchCallback?.invoke("clear")
            }

        }
    }

    companion object {
        fun newInstance(viewModel: TasksParentTabV3VM): TaskV3OngoingFragment {
            val fragment = TaskV3OngoingFragment()
            fragment.parentViewModel = viewModel
            return fragment
        }
    }

    @Inject
    lateinit var adapter: TasksV3Adapter


    private var clearSearchCallback: ((String) -> Unit)? = null
    fun clearSearchCallbackMethod(callback: (String) -> Unit) {
        this.clearSearchCallback = callback
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.taskOngoingRV.adapter = adapter


        parentViewModel?.ongoingAllTasks?.observe(viewLifecycleOwner) {
            if (parentViewModel!!.applyFilter.value == true) {
                parentViewModel!!._applyFilter.value = true
            } else {
                if (parentViewModel!!.isFirstStartOfOngoingFragment) {
                    if (parentViewModel!!.selectedTaskTypeOngoingState.value.equals(
                            TaskRootStateTags.All.tagValue,
                            true
                        )
                    ) {
                        parentViewModel!!.isFirstStartOfOngoingFragment = false
                        parentViewModel!!.filteredOngoingTasks = it
                        if (!it.isNullOrEmpty()) {
                            adapter.setList(
                                it,
                                parentViewModel!!.selectedTaskTypeOngoingState.value ?: ""
                            )
                            mViewDataBinding.taskOngoingRV.visibility = View.VISIBLE
                            mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                            mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
                        } else {
                            adapter.setList(
                                listOf(),
                                parentViewModel!!.selectedTaskTypeOngoingState.value ?: ""
                            )
                            mViewDataBinding.taskOngoingRV.visibility = View.GONE
                            if (parentViewModel!!.isSearchingTasks) {
                                mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                                mViewDataBinding.searchWithNoResultLayout.visibility = View.VISIBLE
                            } else {
                                mViewDataBinding.noTaskInAllLayout.visibility = View.VISIBLE
                                mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
                            }
                        }
                    }
                } else {
                    val type = parentViewModel!!.selectedTaskTypeOngoingState.value
                    parentViewModel!!._selectedTaskTypeOngoingState.value = type
                }
            }
        }


        parentViewModel?.setFilteredDataToOngoingAdapter?.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {

                adapter.setList(list, parentViewModel!!.selectedTaskTypeOngoingState.value ?: "")
                mViewDataBinding.taskOngoingRV.visibility = View.VISIBLE
                mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
            } else {
                adapter.setList(
                    listOf(),
                    parentViewModel!!.selectedTaskTypeOngoingState.value ?: ""
                )
                mViewDataBinding.taskOngoingRV.visibility = View.GONE
                if (parentViewModel!!.isSearchingTasks) {
                    mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                    mViewDataBinding.searchWithNoResultLayout.visibility = View.VISIBLE
                } else {
                    mViewDataBinding.noTaskInAllLayout.visibility = View.VISIBLE
                    mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
                }
            }
        }

        parentViewModel?.lastSortingType?.observe(viewLifecycleOwner) { sortingType ->
            if (parentViewModel!!.isFirstStartOfOngoingFragment.not()) {
                val list: MutableList<CeibroTaskV2> = parentViewModel!!.filteredOngoingTasks
                parentViewModel!!.viewModelScope.launch {
                    viewModel.loading(true, "")
                    val sortedList = async { parentViewModel!!.sortList(list) }.await()
                    val orderedList =
                        async { parentViewModel!!.applySortingOrder(sortedList) }.await()

                    parentViewModel!!.filteredOngoingTasks = orderedList

                    parentViewModel!!.filterTasksList(parentViewModel!!.searchedText)
                    viewModel.loading(false, "")
                }
            }
        }


        parentViewModel?.selectedTaskTypeOngoingState?.observe(viewLifecycleOwner) { taskType ->
            if (parentViewModel!!.isFirstStartOfOngoingFragment.not()) {
                var list: MutableList<CeibroTaskV2> = mutableListOf()

                if (taskType.equals(TaskRootStateTags.All.tagValue, true)) {
                    list = parentViewModel!!.originalOngoingAllTasks

                } else if (taskType.equals(TaskRootStateTags.AllWithoutViewOnly.tagValue, true)) {
                    list = parentViewModel!!.originalOngoingAllTasks.filter { allTasksList -> !allTasksList.isTaskViewer }.toMutableList()

                } else if (taskType.equals(TaskRootStateTags.FromMe.tagValue, true)) {
                    list = parentViewModel!!.originalOngoingFromMeTasks

                } else if (taskType.equals(TaskRootStateTags.ToMe.tagValue, true)) {
                    list = parentViewModel!!.originalOngoingToMeTasks

                } else if (taskType.equals(TaskRootStateTags.ViewOnly.tagValue, true)) {
                    list = parentViewModel!!.originalOngoingAllTasks.filter { allTasksList -> allTasksList.isTaskViewer }.toMutableList()

                } else if (taskType.equals(TaskRootStateTags.Approver.tagValue, true)) {
                    list = parentViewModel!!.originalOngoingAllTasks.filter { allTasksList -> allTasksList.isTaskConfirmer }.toMutableList()

                }

                parentViewModel!!.viewModelScope.launch {
                    val sortedList = async { parentViewModel!!.sortList(list) }.await()
                    val orderedList =
                        async { parentViewModel!!.applySortingOrder(sortedList) }.await()

                    parentViewModel!!.filteredOngoingTasks = orderedList

                    parentViewModel!!.filterTasksList(parentViewModel!!.searchedText)
                }
            }
        }

        parentViewModel?.applyFilter?.observe(viewLifecycleOwner) {
            if (it == true) {
                if (parentViewModel!!.isFirstStartOfOngoingFragment.not()) {
                    val taskType = parentViewModel!!.selectedTaskTypeOngoingState.value

                    var list: MutableList<CeibroTaskV2> = mutableListOf()

                    if (taskType.equals(TaskRootStateTags.All.tagValue, true)) {
                        list = parentViewModel!!.originalOngoingAllTasks

                    } else if (taskType.equals(TaskRootStateTags.AllWithoutViewOnly.tagValue, true)) {
                        list = parentViewModel!!.originalOngoingAllTasks.filter { allTasksList -> !allTasksList.isTaskViewer }.toMutableList()

                    } else if (taskType.equals(TaskRootStateTags.FromMe.tagValue, true)) {
                        list = parentViewModel!!.originalOngoingFromMeTasks

                    } else if (taskType.equals(TaskRootStateTags.ToMe.tagValue, true)) {
                        list = parentViewModel!!.originalOngoingToMeTasks

                    } else if (taskType.equals(TaskRootStateTags.ViewOnly.tagValue, true)) {
                        list = parentViewModel!!.originalOngoingAllTasks.filter { allTasksList -> allTasksList.isTaskViewer }.toMutableList()

                    } else if (taskType.equals(TaskRootStateTags.Approver.tagValue, true)) {
                        list = parentViewModel!!.originalOngoingAllTasks.filter { allTasksList -> allTasksList.isTaskConfirmer }.toMutableList()

                    }

                    parentViewModel!!.viewModelScope.launch {
                        val sortedList = async { parentViewModel!!.sortList(list) }.await()
                        val orderedList =
                            async { parentViewModel!!.applySortingOrder(sortedList) }.await()

                        parentViewModel!!.filteredOngoingTasks = orderedList

                        parentViewModel!!.filterTasksList(parentViewModel!!.searchedText)
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
                    CeibroApplication.CookiesManager.taskDataForDetailsFromNotification = null
                    CeibroApplication.CookiesManager.taskDataForDetails = data
                    CeibroApplication.CookiesManager.taskDetailEvents = allEvents
                    CeibroApplication.CookiesManager.taskDetailRootState =
                        parentViewModel?.selectedTaskTypeOngoingState?.value
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

                try {
                    createPopupWindow(v, data) { menuTag ->
                        if (menuTag.equals("hideTask", true)) {
                            parentViewModel?.showHideTaskDialog(requireContext(), data)
                        } else if (menuTag.equals("cancelTask", true)) {
                            parentViewModel?.showCancelTaskDialog(requireContext(), data)
                        }
                    }
                } catch (error: Exception) {
                    print("error :${error.message} ")
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
        val view: View = inflater.inflate(R.layout.task_v3_hide_cancel_menu_dialog, null)

        val popupWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true


        val hideTaskBtn: TextView = view.findViewById(R.id.hideTaskBtn)
        val cancelTaskBtn: TextView = view.findViewById(R.id.cancelTaskBtn)

        hideTaskBtn.visibility = View.VISIBLE
        if (data.isCreator) {
            cancelTaskBtn.visibility = View.VISIBLE
        }


        hideTaskBtn.setOnClickListener {
            callback.invoke("hideTask")
            popupWindow.dismiss()
        }
        cancelTaskBtn.setOnClickListener {
            callback.invoke("cancelTask")
            popupWindow.dismiss()
        }

        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        if (positionOfIcon > height) {
            if (data.isCreator) {
                popupWindow.showAsDropDown(v, -177, -280)
            } else {
                popupWindow.showAsDropDown(v, -145, -175)
            }
        } else {
            if (data.isCreator) {
                popupWindow.showAsDropDown(v, -177, -10)
            } else {
                popupWindow.showAsDropDown(v, -145, -10)
            }
        }


        return popupWindow
    }


}