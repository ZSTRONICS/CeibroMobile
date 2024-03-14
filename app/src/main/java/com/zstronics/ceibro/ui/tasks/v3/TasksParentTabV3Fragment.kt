package com.zstronics.ceibro.ui.tasks.v3

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentTasksParentTabV3Binding
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.ApprovalTypeBottomSheet
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.ProjectListBottomSheet
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.TagsBottomSheet
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.TaskSortingV3BottomSheet
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.TaskTypeBottomSheet
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.UsersBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class TasksParentTabV3Fragment :
    BaseNavViewModelFragment<FragmentTasksParentTabV3Binding, ITasksParentTabV3.State, TasksParentTabV3VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TasksParentTabV3VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_tasks_parent_tab_v3
    override fun toolBarVisibility(): Boolean = false
    var onceTabIndexSet = false
    override fun onClick(id: Int) {
        when (id) {
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
                chooseTaskType(viewModel.selectedTaskTypeOngoingState.value ?: "") { type ->
                    if (viewModel.selectedTaskTypeOngoingState.value != type) {
                        viewModel._selectedTaskTypeOngoingState.value = type
                        if (type.equals(TaskRootStateTags.All.tagValue, true)) {
                            viewModel.typeToShowOngoing = "All"
                        } else if (type.equals(TaskRootStateTags.FromMe.tagValue, true)) {
                            viewModel.typeToShowOngoing = "From Me"
                        } else if (type.equals(TaskRootStateTags.ToMe.tagValue, true)) {
                            viewModel.typeToShowOngoing = "To Me "
                        }
                        mViewDataBinding.taskTypeText.text = viewModel.typeToShowOngoing
                    }
                }
            }

            R.id.closedType -> {
                chooseTaskType(viewModel.selectedTaskTypeClosedState.value ?: "") { type ->
                    if (viewModel.selectedTaskTypeClosedState.value != type) {
                        viewModel._selectedTaskTypeClosedState.value = type
                        if (type.equals(TaskRootStateTags.All.tagValue, true)) {
                            viewModel.typeToShowClosed = "All"
                        } else if (type.equals(TaskRootStateTags.FromMe.tagValue, true)) {
                            viewModel.typeToShowClosed = "From Me"
                        } else if (type.equals(TaskRootStateTags.ToMe.tagValue, true)) {
                            viewModel.typeToShowClosed = "To Me "
                        }
                        mViewDataBinding.closedTypeText.text = viewModel.typeToShowClosed
                    }
                }
            }

            R.id.approvalType -> {
                chooseApprovalType(viewModel.selectedTaskTypeApprovalState.value ?: "") { type ->
                    if (viewModel.selectedTaskTypeApprovalState.value != type) {
                        viewModel._selectedTaskTypeApprovalState.value = type

                        if (type.equals(TaskRootStateTags.All.tagValue, true)) {
                            viewModel.typeToShowApproval = "All"
                        } else if (type.equals(TaskRootStateTags.ToReview.tagValue, true)) {
                            viewModel.typeToShowApproval = "To Review"
                        } else if (type.equals(TaskRootStateTags.InReview.tagValue, true)) {
                            viewModel.typeToShowApproval = "Pending"
                        }
                        mViewDataBinding.approvalTypeText.text = viewModel.typeToShowApproval
                    }
                }
            }

            R.id.imgSearchFilter -> {

                viewModel._applyFilter.value = true
            }

            R.id.ivSort -> {
                sortInboxBottomSheet()
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

    lateinit var adapter: TasksParentV3TabLayoutAdapter

    val tabTitles = ArrayList<String>()
    val tabIcons = arrayOf(
        R.drawable.icon_inbox_blue,
        R.drawable.icon_task_ongoing_tab,
        R.drawable.icon_approval,
        R.drawable.icon_tick_mark_blue
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TasksParentV3TabLayoutAdapter(requireActivity(), tabIcons, viewModel)
        mViewDataBinding.taskViewPager.adapter = adapter

        tabTitles.add(getString(R.string.activity_heading))
        tabTitles.add(getString(R.string.ongoing_heading))
        tabTitles.add(getString(R.string.approval_heading))
        tabTitles.add(getString(R.string.closed_heading))




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



        mViewDataBinding.taskViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        mViewDataBinding.filterAndSearchLayout.visibility = View.GONE
                        mViewDataBinding.approvalType.visibility = View.GONE
                        mViewDataBinding.taskType.visibility = View.GONE
                    }

                    1 -> {
                        mViewDataBinding.filterAndSearchLayout.visibility = View.VISIBLE
                        mViewDataBinding.approvalType.visibility = View.GONE
                        mViewDataBinding.closedType.visibility = View.GONE
                        mViewDataBinding.taskType.visibility = View.VISIBLE
                    }

                    2 -> {
                        mViewDataBinding.filterAndSearchLayout.visibility = View.VISIBLE
                        mViewDataBinding.approvalType.visibility = View.VISIBLE
                        mViewDataBinding.closedType.visibility = View.GONE
                        mViewDataBinding.taskType.visibility = View.GONE
                    }

                    3 -> {
                        mViewDataBinding.filterAndSearchLayout.visibility = View.VISIBLE
                        mViewDataBinding.approvalType.visibility = View.GONE
                        mViewDataBinding.closedType.visibility = View.VISIBLE
                        mViewDataBinding.taskType.visibility = View.GONE
                    }
                }
            }
        })

        val tabTextColors = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf()),
            intArrayOf(resources.getColor(R.color.black), resources.getColor(R.color.appBlue))
        )
        mViewDataBinding.taskTabLayout.tabTextColors = tabTextColors
        mViewDataBinding.taskTabLayout.tabIconTint = tabTextColors
        mViewDataBinding.taskTabLayout.setSelectedTabIndicatorColor(Color.BLACK)


        TabLayoutMediator(
            mViewDataBinding.taskTabLayout,
            mViewDataBinding.taskViewPager
        ) { tab, position ->
//            tab.text = tabTitles[position]
//            adapter.getTabIcon(position).let { tab.setIcon(it) }     //This default view shows icon on top of text, not on start as desired

            val customTab =
                LayoutInflater.from(requireContext()).inflate(R.layout.layout_task_tab_item, null)
            val tabIcon = customTab.findViewById<ImageView>(R.id.taskTabIcon)
            val tabText = customTab.findViewById<TextView>(R.id.taskTabText)

            tabText.text = tabTitles[position]

            // Set icon if available
            val iconResId = adapter.getTabIcon(position)
            tabIcon.setImageResource(iconResId)
            tabIcon.visibility = View.VISIBLE

            tab.customView = customTab

            mViewDataBinding.taskTabLayout.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    // Update selected tab text and icon colors
                    if (tab != null && tab.position == position) {
                        tabText.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.black
                            )
                        )
                        tabIcon.setColorFilter(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.black
                            ), PorterDuff.Mode.SRC_IN
                        )
                    } else {
                        tabText.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.appBlue
                            )
                        )
                        tabIcon.setColorFilter(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.appBlue
                            ), PorterDuff.Mode.SRC_IN
                        )
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            // Update colors for the initially selected tab
            if (tab == mViewDataBinding.taskTabLayout.getTabAt(mViewDataBinding.taskTabLayout.selectedTabPosition)) {
                tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tabIcon.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.black),
                    PorterDuff.Mode.SRC_IN
                )
            }

        }.attach()

        if (onceTabIndexSet.not()) {
            adapter.setDefaultTab(mViewDataBinding.taskViewPager)
            onceTabIndexSet = true
        }
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
        viewModel: TasksParentTabV3VM,
        userConnectionAndRoleCallBack: (Pair<ArrayList<AllCeibroConnections.CeibroConnection>, ArrayList<String>>) -> Unit,
        groupsCallBack: (ArrayList<CeibroConnectionGroupV2>) -> Unit
    ) {
        val sheet = UsersBottomSheet(viewModel, {
            userConnectionAndRoleCallBack.invoke(it)
        }) {
            groupsCallBack.invoke(it)
        }

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "UsersBottomSheet")
    }

    private fun chooseTagsType(model: TasksParentTabV3VM, callback: (String) -> Unit) {
        val sheet = TagsBottomSheet(model) {

            callback.invoke(viewModel.selectedTagsForFilter.size.toString())
        }

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "TagsBottomSheet")
    }

    private fun chooseProjectFromList(viewModel: TasksParentTabV3VM, callback: (String) -> Unit) {
        val sheet = ProjectListBottomSheet(viewModel) {
            callback.invoke(it.size.toString())
        }

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "ProjectListBottomSheet")
    }

    private fun sortInboxBottomSheet() {
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
        mViewDataBinding.taskTypeText.text = viewModel.typeToShowOngoing
        mViewDataBinding.closedTypeText.text = viewModel.typeToShowClosed
        mViewDataBinding.approvalTypeText.text = viewModel.typeToShowApproval

        mViewDataBinding.userFilterCounter.text = viewModel.userFilterCounter
        mViewDataBinding.tagFilterCounter.text = viewModel.tagFilterCounter
        mViewDataBinding.projectFilterCounter.text = viewModel.projectFilterCounter
        if (viewModel.isSearchingTasks) {
            viewModel.filterTasksList(viewModel.searchedText)

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshProjectsData(event: LocalEvents.RefreshProjectsData?) {
        viewModel.reloadProjectData()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshTasksData(event: LocalEvents.RefreshTasksData?) {
        viewModel.loadAllTasks {

        }
    }

}