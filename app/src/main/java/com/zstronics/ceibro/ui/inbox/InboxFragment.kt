package com.zstronics.ceibro.ui.inbox

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentInboxBinding
import com.zstronics.ceibro.ui.dashboard.SearchDataSingleton
import com.zstronics.ceibro.ui.inbox.adapter.InboxAdapter
import com.zstronics.ceibro.ui.locationv2.LocationsV2Fragment
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
    }


    @Inject
    lateinit var adapter: InboxAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.taskRV.adapter = adapter

        viewModel.inboxTasks.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                mViewDataBinding.inboxSearchBar.isEnabled = false
                mViewDataBinding.taskRV.visibility = View.GONE
                mViewDataBinding.inboxInfoLayout.visibility = View.VISIBLE
                mViewDataBinding.inboxLogoBackground.visibility = View.VISIBLE
            } else {
                adapter.setList(it)
                mViewDataBinding.inboxSearchBar.isEnabled = true
                mViewDataBinding.taskRV.visibility = View.VISIBLE
                mViewDataBinding.inboxInfoLayout.visibility = View.GONE
                mViewDataBinding.inboxLogoBackground.visibility = View.GONE
            }
        }

        viewModel.filteredInboxTasks.observe(viewLifecycleOwner) {
            if (viewModel.isUserSearching) {
                if (it.isNullOrEmpty()) {
                    mViewDataBinding.taskRV.visibility = View.GONE
                    mViewDataBinding.inboxInfoLayout.visibility = View.VISIBLE
                    mViewDataBinding.inboxLogoBackground.visibility = View.VISIBLE
                } else {
                    adapter.setList(it)
                    mViewDataBinding.taskRV.visibility = View.VISIBLE
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
                CeibroApplication.CookiesManager.taskDetailRootState =
                    TaskRootStateTags.ToMe.tagValue.lowercase()
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

    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            val allInboxTasks = CeibroApplication.CookiesManager.allInboxTasks.value
            allInboxTasks?.sortByDescending { it.createdAt }

            if (allInboxTasks.isNullOrEmpty()) {
                val allInboxTasks1 = viewModel.inboxV2Dao.getAllInboxItems().toMutableList()
                CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks1)
                viewModel._inboxTasks.postValue(allInboxTasks1)
                viewModel.originalInboxTasks = allInboxTasks1
            } else {
                viewModel._inboxTasks.postValue(allInboxTasks)
                viewModel.originalInboxTasks = allInboxTasks
            }

//            if (viewModel.isUserSearching) {
//                if (mViewDataBinding.inboxSearchBar.query.toString().isNotEmpty()) {
//                    val searchQuery = mViewDataBinding.inboxSearchBar.query.toString()
//                    viewModel.isUserSearching = true
//                    mViewDataBinding.inboxSearchBar.setQuery(searchQuery, false)
//                    viewModel.searchInboxTasks(searchQuery.trim())
//                }
//            } else {
            viewModel.isUserSearching = false
            mViewDataBinding.inboxSearchBar.setQuery("", false)
//            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.isUserSearching = false
        mViewDataBinding.inboxSearchBar.setQuery("", true)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshInboxData(event: LocalEvents.RefreshInboxData) {
        GlobalScope.launch {
            val allInboxTasks = CeibroApplication.CookiesManager.allInboxTasks.value
            allInboxTasks?.sortByDescending { it.createdAt }
            viewModel._inboxTasks.postValue(allInboxTasks)
            viewModel.originalInboxTasks = allInboxTasks ?: mutableListOf()
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