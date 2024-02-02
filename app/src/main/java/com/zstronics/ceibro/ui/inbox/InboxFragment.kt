package com.zstronics.ceibro.ui.inbox

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentInboxBinding
import com.zstronics.ceibro.ui.inbox.adapter.InboxAdapter
import com.zstronics.ceibro.ui.inbox.adapter.SwipeRecyclerItemFromLeft
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject


@AndroidEntryPoint
class InboxFragment :
    BaseNavViewModelFragment<FragmentInboxBinding, IInbox.State, InboxVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: InboxVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_inbox
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.ivSort -> {
                sortInboxBottomSheet()
            }
        }
    }

    @Inject
    lateinit var adapter: InboxAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.isUserSearching = false
        mViewDataBinding.inboxSearchBar.setQuery("", false)

        mViewDataBinding.taskRV.adapter = adapter

        viewModel.inboxTasks.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                mViewDataBinding.inboxSearchBar.isEnabled = false
                mViewDataBinding.taskRV.visibility = View.GONE
                mViewDataBinding.sortedByText.visibility = View.GONE
                mViewDataBinding.inboxInfoLayout.visibility = View.VISIBLE
                mViewDataBinding.inboxLogoBackground.visibility = View.VISIBLE
            } else {
                adapter.setList(it)
                mViewDataBinding.inboxSearchBar.isEnabled = true
                mViewDataBinding.taskRV.visibility = View.VISIBLE
                mViewDataBinding.sortedByText.visibility = View.GONE
                changeSortingText(viewModel.lastSortingType)
                mViewDataBinding.inboxInfoLayout.visibility = View.GONE
                mViewDataBinding.inboxLogoBackground.visibility = View.GONE
            }
        }

        viewModel.filteredInboxTasks.observe(viewLifecycleOwner) {
            if (viewModel.isUserSearching) {
                if (it.isNullOrEmpty()) {
                    mViewDataBinding.taskRV.visibility = View.GONE
                    mViewDataBinding.sortedByText.visibility = View.GONE
                    mViewDataBinding.inboxInfoLayout.visibility = View.VISIBLE
                    mViewDataBinding.inboxLogoBackground.visibility = View.VISIBLE
                } else {
                    adapter.setList(it)
                    mViewDataBinding.taskRV.visibility = View.VISIBLE
                    mViewDataBinding.sortedByText.visibility = View.GONE        //make it visible if you wanna see sorted by text
                    changeSortingText(viewModel.lastSortingType)
                    mViewDataBinding.inboxInfoLayout.visibility = View.GONE
                    mViewDataBinding.inboxLogoBackground.visibility = View.GONE
                }
            }
        }

        adapter.itemClickListener = { _, position, inboxData ->
            GlobalScope.launch {
//                viewModel.loading(true)
                val task = viewModel.taskDao.getTaskByID(inboxData.taskId)
                val allEvents = viewModel.taskDao.getEventsOfTask(inboxData.taskId)

                CeibroApplication.CookiesManager.taskDataForDetails = task
                CeibroApplication.CookiesManager.taskDetailEvents = allEvents
                CeibroApplication.CookiesManager.taskDetailRootState = if (task != null) {
                    if (task.isCreator) {
                        TaskRootStateTags.FromMe.tagValue.lowercase()
                    } else {
                        TaskRootStateTags.ToMe.tagValue.lowercase()
                    }
                } else {
                    TaskRootStateTags.ToMe.tagValue.lowercase()
                }

                withContext(Dispatchers.Main) {
                    // Update the UI here
//                    viewModel.loading(false, "")
                    navigate(R.id.taskDetailV2Fragment)
                }
            }
        }


        mViewDataBinding.inboxSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.searchInboxTasks(query.trim())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewModel.searchInboxTasks(newText.trim())
                }
                return true
            }
        })


        val swipeHelperRight: SwipeRecyclerItemFromLeft =
            object : SwipeRecyclerItemFromLeft(context, mViewDataBinding.taskRV) {
                override fun instantiateUnderlayButton(
                    viewHolder: RecyclerView.ViewHolder?,
                    underlayButtons: MutableList<UnderlayButton?>
                ) {
                    /*underlayButtons.add(UnderlayButton(
                        context,
                        "Archive",
                        R.drawable.delete,
                        Color.parseColor("#BBBBC3"),
                        object : UnderlayButtonClickListener {
                            override fun onClick(pos: Int) {
                                Toast.makeText(context, "Archive $pos", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ))*/

                    underlayButtons.add(UnderlayButton(
                        context,
                        "Delete",
                        R.drawable.icon_delete_white,
                        Color.parseColor("#E42116"),
                        object : UnderlayButtonClickListener {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onClick(pos: Int) {
                                //    Toast.makeText(context, "Delete $pos", Toast.LENGTH_SHORT).show()
                                if (viewModel.isUserSearching) {
                                    val filterList = viewModel.filteredInboxTasks.value
                                    if (!filterList.isNullOrEmpty()) {
                                        val taskToRemove = filterList[pos]
                                        filterList.removeAt(pos)
                                        viewModel._filteredInboxTasks.postValue(filterList)

                                        val originalTaskToRemove =
                                            viewModel.originalInboxTasks.find {
                                                it._id == taskToRemove._id
                                            }
                                        if (originalTaskToRemove != null) {
                                            val index = viewModel.originalInboxTasks.indexOf(
                                                originalTaskToRemove
                                            )
                                            viewModel.originalInboxTasks.removeAt(index)
                                            viewModel.deleteInboxTaskFromDB(originalTaskToRemove)

                                            viewModel.taskSeen(originalTaskToRemove.taskId) { }
                                        }
                                    }
                                } else {
                                    if (pos < viewModel.originalInboxTasks.size) {
                                        val originalList = viewModel.originalInboxTasks
                                        val originalTaskToRemove = originalList[pos]
                                        val index = originalList.indexOf(originalTaskToRemove)
                                        originalList.removeAt(index)
                                        viewModel.originalInboxTasks = originalList

                                        val adapterItemIndex = adapter.listItems.indexOf(originalTaskToRemove)
                                        adapter.listItems.removeAt(adapterItemIndex)
                                        adapter.notifyItemRemoved(adapterItemIndex)

//                                        viewModel._inboxTasks.postValue(originalList)
                                        viewModel.deleteInboxTaskFromDB(originalTaskToRemove)
                                        viewModel.taskSeen(originalTaskToRemove.taskId) { }
                                    }
                                }
//                                Handler().postDelayed({
//                                    adapter.notifyDataSetChanged()
//                                }, 100)
                            }
                        }
                    ))
                }
            }
    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            val allInboxTasks = CeibroApplication.CookiesManager.allInboxTasks.value
            val inboxSorting = CeibroApplication.CookiesManager.inboxTasksSortingType.value

            if (viewModel.fragmentFirstRun || allInboxTasks.isNullOrEmpty()) {
                viewModel.lastSortingType = "SortByActivity"
                viewModel.fragmentFirstRun = false
                val allInboxTasks1 = viewModel.inboxV2Dao.getAllInboxItems().toMutableList()

                viewModel._inboxTasks.postValue(allInboxTasks1)
                viewModel.originalInboxTasks = allInboxTasks1
                withContext(Dispatchers.Main) {
                    CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks1)
                    CeibroApplication.CookiesManager.inboxTasksSortingType.postValue("SortByActivity")
                }
            } else {
                viewModel.lastSortingType = inboxSorting ?: "SortByActivity"
                viewModel.fragmentFirstRun = false
                viewModel._inboxTasks.postValue(allInboxTasks)
                viewModel.originalInboxTasks = allInboxTasks
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.isUserSearching = false
        mViewDataBinding.inboxSearchBar.setQuery("", true)
    }

    private fun sortInboxBottomSheet() {
        val sheet = InboxSortingBottomSheet(viewModel.lastSortingType)

        sheet.onChangeSortingType = { latestSortingType ->
            viewModel.lastSortingType = latestSortingType
            if (viewModel.isUserSearching) {
                mViewDataBinding.inboxSearchBar.setQuery("", false)
            }
            CeibroApplication.CookiesManager.inboxTasksSortingType.postValue(latestSortingType)
            viewModel.changeSortingOrder(latestSortingType)
            changeSortingText(latestSortingType)
        }

        sheet.isCancelable = false
        sheet.show(childFragmentManager, "InboxSortingBottomSheet")
    }

    private fun changeSortingText(latestSortingType: String) {
        mViewDataBinding.sortedByText.text = if (latestSortingType.equals("SortByActivity", true)) {
            "Sorted by: Last activity on top"

        } else if (latestSortingType.equals("SortByUnread", true)) {
            "Sorted by: Unread on top"

        } else if (latestSortingType.equals("SortByDueDate", true)) {
            "Sorted by: Due date"

        } else {
            mViewDataBinding.sortedByText.visibility = View.GONE
            ""
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshInboxData(event: LocalEvents.RefreshInboxData) {
        GlobalScope.launch {
            val allInboxTasks = CeibroApplication.CookiesManager.allInboxTasks.value
            viewModel.changeSortingOrderWhenNewItemAdded(allInboxTasks ?: mutableListOf(), viewModel.lastSortingType)
//            allInboxTasks?.sortByDescending { it.createdAt }
//            viewModel._inboxTasks.postValue(allInboxTasks)
//            viewModel.originalInboxTasks = allInboxTasks ?: mutableListOf()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateInboxItemSeen(event: LocalEvents.UpdateInboxItemSeen?) {
        GlobalScope.launch {
            val inboxUpdatedTask = event?.inboxTask
            if (inboxUpdatedTask != null) {
                val allInboxTasks = CeibroApplication.CookiesManager.allInboxTasks.value
                val taskToUpdate = allInboxTasks?.find { it.taskId == inboxUpdatedTask.taskId }
                if (taskToUpdate != null) {
                    val index = allInboxTasks.indexOf(taskToUpdate)
                    allInboxTasks[index] = inboxUpdatedTask
                    CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks)
//                    viewModel._inboxTasks.postValue(allInboxTasks)
                    viewModel.originalInboxTasks = allInboxTasks ?: mutableListOf()
                }
                withContext(Dispatchers.Main) {
                    val listItem = adapter.listItems.find { it.taskId == inboxUpdatedTask.taskId }
                    if (listItem != null) {
                        val index = adapter.listItems.indexOf(listItem)
                        adapter.listItems[index] = inboxUpdatedTask
                        adapter.notifyItemChanged(index)
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshInboxSingleEvent(event: LocalEvents.RefreshInboxSingleEvent?) {
        GlobalScope.launch {
            val inboxUpdatedTask = event?.inboxTask
            if (inboxUpdatedTask != null) {
                val allInboxTasks = CeibroApplication.CookiesManager.allInboxTasks.value

                withContext(Dispatchers.Main) {
                    val listItem = adapter.listItems.find { it.taskId == inboxUpdatedTask.taskId }
                    if (listItem != null) {
                        val index = adapter.listItems.indexOf(listItem)
                        adapter.listItems[index] = inboxUpdatedTask
                        adapter.notifyItemChanged(index)
                    }
                }

                val taskToUpdate = allInboxTasks?.find { it.taskId == inboxUpdatedTask.taskId }
                if (taskToUpdate != null) {
                    val index = allInboxTasks.indexOf(taskToUpdate)
                    allInboxTasks[index] = inboxUpdatedTask
                    CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks)
//                    viewModel._inboxTasks.postValue(allInboxTasks)
                    viewModel.originalInboxTasks = allInboxTasks ?: mutableListOf()
                } else {
                    allInboxTasks?.add(0, inboxUpdatedTask)
                    viewModel.changeSortingOrderWhenNewItemAdded(allInboxTasks ?: mutableListOf(), viewModel.lastSortingType)
                }

            }
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            EventBus.getDefault().register(this)
        } catch (exception: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}