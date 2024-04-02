package com.zstronics.ceibro.ui.tasks.v3.canceled

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentTasksCanceledV3Binding
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.ApprovalTypeBottomSheet
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.ProjectListCanceledBottomSheet
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.TagsCanceledBottomSheet
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.TaskSortingV3BottomSheet
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.TaskTypeBottomSheet
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.UsersCanceledBottomSheet
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
class TasksCanceledV3Fragment :
    BaseNavViewModelFragment<FragmentTasksCanceledV3Binding, ITasksCanceledV3.State, TasksCanceledV3VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TasksCanceledV3VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_tasks_canceled_v3
    override fun toolBarVisibility(): Boolean = false
    var onceTabIndexSet = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                navigateBack()
            }

            R.id.clearSearch -> {
                mViewDataBinding.taskSearchBar.setQuery(null, true)
                mViewDataBinding.taskSearchBar.clearFocus()
                mViewDataBinding.taskSearchBar.hideKeyboard()
            }

            R.id.userFilter -> {
                chooseUserType(
                    viewModel,
                    { userConnectionAndRoleList ->
                        viewModel.userConnectionAndRoleList = userConnectionAndRoleList

                        val size =
                            viewModel.selectedGroups.size + viewModel.userConnectionAndRoleList.first.size
                        mViewDataBinding.userFilterCounter.text = size.toString()
                        viewModel.userFilterCounter = size.toString()
                    }
                ) { groupsList ->
                    viewModel.selectedGroups = groupsList

                    val size =
                        viewModel.selectedGroups.size + viewModel.userConnectionAndRoleList.first.size
                    mViewDataBinding.userFilterCounter.text = size.toString()
                    viewModel.userFilterCounter = size.toString()

                }
            }

            R.id.taskType -> {
                chooseTaskType(viewModel.selectedTaskTypeCanceledState.value ?: "") { type ->
                    if (viewModel.selectedTaskTypeCanceledState.value != type) {
                        viewModel._selectedTaskTypeCanceledState.value = type
                        if (type.equals(TaskRootStateTags.All.tagValue, true)) {
                            viewModel.typeToShowCanceled = "All"
                        } else if (type.equals(TaskRootStateTags.FromMe.tagValue, true)) {
                            viewModel.typeToShowCanceled = "From Me"
                        } else if (type.equals(TaskRootStateTags.ToMe.tagValue, true)) {
                            viewModel.typeToShowCanceled = "To Me "
                        }
                        mViewDataBinding.taskTypeText.text = viewModel.typeToShowCanceled
                    }
                }
            }

            R.id.imgSearchFilter -> {

                viewModel._applyFilter.value = true
            }

            R.id.ivSort -> {
                sortTasksBottomSheet()
            }

            R.id.projectFilter -> {

                chooseProjectFromList(viewModel) {
                    mViewDataBinding.projectFilterCounter.text = it
                    viewModel.projectFilterCounter = it
                }
            }

            R.id.tagFilter -> {

                chooseTagsType(viewModel) {
                    mViewDataBinding.tagFilterCounter.text = it
                    viewModel.tagFilterCounter = it
                }
            }

            R.id.imgSearchFilter -> {
//                mViewDataBinding.tasksSearchCard.visibility = View.VISIBLE
            }

            R.id.cancelTaskSearch -> {
                mViewDataBinding.taskSearchBar.setQuery(null, true)
                mViewDataBinding.taskSearchBar.clearFocus()
                mViewDataBinding.taskSearchBar.hideKeyboard()
            }
        }
    }

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
//            val instances = countActivitiesInBackStack(requireContext())
//            if (instances <= 1) {
//                launchActivityWithFinishAffinity<NavHostPresenterActivity>(
//                    options = Bundle(),
//                    clearPrevious = true
//                ) {
//                    putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
//                    putExtra(
//                        NAVIGATION_Graph_START_DESTINATION_ID,
//                        R.id.homeFragment
//                    )
//                }
//            } else {
//                //finish is called so that second instance of app will be closed and only one last instance will remain
//                finish()
//            }
        }
    }


    @Inject
    lateinit var adapter: CancelledTasksV3Adapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.taskOngoingRV.adapter = adapter


        viewModel.canceledAllTasks.observe(viewLifecycleOwner) {
            if (viewModel.applyFilter.value == true) {
                viewModel._applyFilter.value = true
            } else {
                if (viewModel.selectedTaskTypeCanceledState.value.equals(
                        TaskRootStateTags.All.tagValue,
                        true
                    )
                ) {
                    viewModel.isFirstStartOfCanceledFragment = false
                    viewModel.filteredCanceledTasks = it
                    if (!it.isNullOrEmpty()) {
                        adapter.setList(
                            it,
                            viewModel.selectedTaskTypeCanceledState.value ?: ""
                        )
                        mViewDataBinding.taskOngoingRV.visibility = View.VISIBLE
                        mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                        mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
                    } else {
                        adapter.setList(
                            listOf(),
                            viewModel.selectedTaskTypeCanceledState.value ?: ""
                        )
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
        }


        viewModel.setFilteredDataToCanceledAdapter.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {

                adapter.setList(list, viewModel.selectedTaskTypeCanceledState.value ?: "")
                mViewDataBinding.taskOngoingRV.visibility = View.VISIBLE
                mViewDataBinding.noTaskInAllLayout.visibility = View.GONE
                mViewDataBinding.searchWithNoResultLayout.visibility = View.GONE
            } else {
                adapter.setList(
                    listOf(),
                    viewModel.selectedTaskTypeCanceledState.value ?: ""
                )
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

        viewModel.lastSortingType.observe(viewLifecycleOwner) { sortingType ->
            if (viewModel.isFirstStartOfCanceledFragment.not()) {
                val list: MutableList<CeibroTaskV2> = viewModel.filteredCanceledTasks
                viewModel.viewModelScope.launch {
                    viewModel.loading(true, "")
                    val sortedList = async { viewModel.sortList(list) }.await()
                    val orderedList =
                        async { viewModel.applySortingOrder(sortedList) }.await()

                    viewModel.filteredCanceledTasks = orderedList

                    viewModel.filterTasksList(viewModel.searchedText)
                    viewModel.loading(false, "")
                }
            }
        }


        viewModel.selectedTaskTypeCanceledState.observe(viewLifecycleOwner) { taskType ->
            if (viewModel.isFirstStartOfCanceledFragment.not()) {
                var list: MutableList<CeibroTaskV2> = mutableListOf()

                if (taskType.equals(TaskRootStateTags.All.tagValue, true)) {
                    list = viewModel.originalCanceledAllTasks

                } else if (taskType.equals(TaskRootStateTags.FromMe.tagValue, true)) {
                    list = viewModel.originalCanceledFromMeTasks

                } else if (taskType.equals(TaskRootStateTags.ToMe.tagValue, true)) {
                    list = viewModel.originalCanceledToMeTasks
                }

                viewModel.viewModelScope.launch {
                    val sortedList = async { viewModel.sortList(list) }.await()
                    val orderedList =
                        async { viewModel.applySortingOrder(sortedList) }.await()

                    viewModel.filteredCanceledTasks = orderedList

                    viewModel.filterTasksList(viewModel.searchedText)
                }
            }
        }

        viewModel.applyFilter.observe(viewLifecycleOwner) {
            if (it == true) {
                if (viewModel.isFirstStartOfCanceledFragment.not()) {
                    val taskType = viewModel.selectedTaskTypeCanceledState.value

                    var list: MutableList<CeibroTaskV2> = mutableListOf()

                    if (taskType.equals(TaskRootStateTags.All.tagValue, true)) {
                        list = viewModel.originalCanceledAllTasks

                    } else if (taskType.equals(TaskRootStateTags.FromMe.tagValue, true)) {
                        list = viewModel.originalCanceledFromMeTasks

                    } else if (taskType.equals(TaskRootStateTags.ToMe.tagValue, true)) {
                        list = viewModel.originalCanceledToMeTasks
                    }

                    viewModel.viewModelScope.launch {
                        val sortedList = async { viewModel.sortList(list) }.await()
                        val orderedList =
                            async { viewModel.applySortingOrder(sortedList) }.await()

                        viewModel.filteredCanceledTasks = orderedList

                        viewModel.filterTasksList(viewModel.searchedText)
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
                        viewModel.selectedTaskTypeCanceledState.value
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
                    if (menuTag.equals("unCancelTask", true)) {
                        viewModel.showUnCancelTaskDialog(requireContext(), data)
                    }
                }
            }


        mViewDataBinding.taskSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.filterTasksList(query.trim())
                    viewModel.searchedText = query.trim()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewModel.filterTasksList(newText.trim())
                    viewModel.searchedText = newText.trim()
                }
                return true
            }
        })

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


        val unCancelTaskBtn: TextView = view.findViewById(R.id.unCancelTaskBtn)

        unCancelTaskBtn.visibility = View.VISIBLE

        unCancelTaskBtn.setOnClickListener {
            callback.invoke("unCancelTask")
            popupWindow.dismiss()
        }

        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        if (positionOfIcon > height) {
            popupWindow.showAsDropDown(v, -221, -170)
        } else {
            popupWindow.showAsDropDown(v, -221, -10)
        }


        return popupWindow
    }

    private fun chooseTaskType(type: String, callback: (String) -> Unit) {
        val sheet = TaskTypeBottomSheet(type) {
            callback.invoke(it)
        }

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "TaskTypeBottomSheet")
    }

    private fun chooseApprovalType(type: String, callback: (String) -> Unit) {
        val sheet = ApprovalTypeBottomSheet(type) {
            callback.invoke(it)
        }

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "TaskTypeBottomSheet")
    }

    private fun chooseUserType(
        viewModel: TasksCanceledV3VM,
        userConnectionAndRoleCallBack: (Pair<ArrayList<AllCeibroConnections.CeibroConnection>, ArrayList<String>>) -> Unit,
        groupsCallBack: (ArrayList<CeibroConnectionGroupV2>) -> Unit
    ) {
        val sheet = UsersCanceledBottomSheet(viewModel, {
            userConnectionAndRoleCallBack.invoke(it)
        }) {
            groupsCallBack.invoke(it)
        }

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "UsersBottomSheet")
    }

    private fun chooseTagsType(model: TasksCanceledV3VM, callback: (String) -> Unit) {
        val sheet = TagsCanceledBottomSheet(model) {

            callback.invoke(viewModel.selectedTagsForFilter.size.toString())
        }

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "TagsBottomSheet")
    }

    private fun chooseProjectFromList(
        viewModel: TasksCanceledV3VM,
        callback: (String) -> Unit
    ) {
        val sheet = ProjectListCanceledBottomSheet(viewModel) {
            callback.invoke(it.size.toString())
        }

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "ProjectListBottomSheet")
    }

    private fun sortTasksBottomSheet() {
        val sheet = TaskSortingV3BottomSheet(viewModel.lastSortingType.value ?: "SortByActivity")

        sheet.onChangeSortingType = { latestSortingType ->
            viewModel.lastSortingType.value = latestSortingType
//            if (viewModel.isUserSearching) {
//                mViewDataBinding.taskSearchBar.setQuery("", false)
//            }
            //  CeibroApplication.CookiesManager.inboxTasksSortingType.postValue(latestSortingType)
            //  viewModel.changeSortingOrder(latestSortingType)
            //     changeSortingText(latestSortingType)
        }

        sheet.isCancelable = false
        sheet.show(childFragmentManager, "TaskSortingV3BottomSheet")
    }

    private fun showTaskInfoBottomSheet() {
//        val sheet = TaskInfoBottomSheet(
//            _rootState = viewModel.rootState,
//            _selectedState = viewModel.selectedState,
//            _userId = viewModel.user?.id ?: "",
//            _taskDetail = viewModel.taskDetail.value
//        )
////        sheet.dialog?.window?.setSoftInputMode(
////            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
////                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
////        );
//
//        sheet.isCancelable = true
//        sheet.show(childFragmentManager, "TaskInfoBottomSheet")
    }


//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        EventBus.getDefault().register(this)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        EventBus.getDefault().unregister(this)
//    }

    override fun onResume() {
        super.onResume()
        mViewDataBinding.taskTypeText.text = viewModel.typeToShowCanceled

        mViewDataBinding.userFilterCounter.text = viewModel.userFilterCounter
        mViewDataBinding.tagFilterCounter.text = viewModel.tagFilterCounter
        mViewDataBinding.projectFilterCounter.text = viewModel.projectFilterCounter
        if (viewModel.isSearchingTasks) {
            viewModel.filterTasksList(viewModel.searchedText)

        }


        if (viewModel.isFirstTimeUICreated) {

            viewModel.loadAllTasks {

            }
            viewModel.isFirstTimeUICreated = false
        }

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.isFirstTimeUICreated = true
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshProjectsData(event: LocalEvents.RefreshProjectsData?) {
        viewModel.reloadData()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshTasksData(event: LocalEvents.RefreshTasksData?) {

        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.loadAllTasks {
            }
        }, 100)
    }


}