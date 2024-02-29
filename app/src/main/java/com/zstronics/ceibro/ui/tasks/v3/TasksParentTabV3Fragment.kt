package com.zstronics.ceibro.ui.tasks.v3

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTasksParentTabV3Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksParentTabV3Fragment :
    BaseNavViewModelFragment<FragmentTasksParentTabV3Binding, ITasksParentTabV3.State, TasksParentTabV3VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TasksParentTabV3VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_tasks_parent_tab_v3
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> {

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
    val tabIcons = arrayOf(R.drawable.icon_inbox_blue, R.drawable.icon_task_ongoing_tab, R.drawable.icon_approval, R.drawable.icon_tick_mark_blue)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TasksParentV3TabLayoutAdapter(requireActivity(), tabIcons)
        mViewDataBinding.taskViewPager.adapter = adapter

        tabTitles.add(getString(R.string.activity_heading))
        tabTitles.add(getString(R.string.ongoing_heading))
        tabTitles.add(getString(R.string.approval_heading))
        tabTitles.add(getString(R.string.closed_heading))


        mViewDataBinding.taskViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
//                    0 -> {
//                        mViewDataBinding.bottomFooterLayout.visibility = View.VISIBLE
//                    }
//
//                    1 -> {
//                        mViewDataBinding.bottomFooterLayout.visibility = View.GONE
//                    }
//
//                    2 -> {
//                        mViewDataBinding.bottomFooterLayout.visibility = View.GONE
//                    }
                }
            }
        })



        TabLayoutMediator(mViewDataBinding.taskTabLayout, mViewDataBinding.taskViewPager) { tab, position ->
            tab.text = tabTitles[position]
            adapter.getTabIcon(position).let { tab.setIcon(it) }     //This default view shows icon on top of text, not on start as desired

//            val customTab = LayoutInflater.from(requireContext()).inflate(R.layout.layout_task_tab_item, null)
//            val tabIcon = customTab.findViewById<ImageView>(R.id.taskTabIcon)
//            val tabText = customTab.findViewById<TextView>(R.id.taskTabText)
//
//            tabText.text = tabTitles[position]
//
//            // Set icon if available
//            val iconResId = adapter.getTabIcon(position)
//            if (iconResId != null) {
//                tabIcon.setImageResource(iconResId)
//                tabIcon.visibility = View.VISIBLE
//            } else {
//                tabIcon.visibility = View.GONE
//            }
//
//            tab.customView = customTab

        }.attach()

        val tabTextColors = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf()),
            intArrayOf(resources.getColor(R.color.black), resources.getColor(R.color.appBlue))
        )
        mViewDataBinding.taskTabLayout.tabTextColors = tabTextColors
        mViewDataBinding.taskTabLayout.tabIconTint = tabTextColors
        mViewDataBinding.taskTabLayout.setSelectedTabIndicatorColor(Color.BLACK)

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


}