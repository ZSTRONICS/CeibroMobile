package com.zstronics.ceibro.ui.inbox

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentInboxBinding
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

        adapter.itemClickListener = { _, position, inboxData ->
            GlobalScope.launch {
//                viewModel.loading(true)
                val task = viewModel.taskDao.getTaskByID(inboxData.taskId)
                val allEvents = viewModel.taskDao.getEventsOfTask(inboxData.taskId)

                CeibroApplication.CookiesManager.taskDataForDetails = task
                CeibroApplication.CookiesManager.taskDetailEvents = allEvents
                CeibroApplication.CookiesManager.taskDetailRootState = TaskRootStateTags.ToMe.tagValue.lowercase()
                withContext(Dispatchers.Main) {
                    // Update the UI here
//                    viewModel.loading(false, "")
                    navigate(R.id.taskDetailV2Fragment)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            val allInboxTasks = viewModel.inboxV2Dao.getAllInboxItems().toMutableList()
            CeibroApplication.CookiesManager.allInboxTasks.postValue(allInboxTasks)
            viewModel._inboxTasks.postValue(allInboxTasks)
        }
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshInboxData(event: LocalEvents.RefreshInboxData) {
        GlobalScope.launch {
            val allInboxTasks = CeibroApplication.CookiesManager.allInboxTasks.value
            viewModel._inboxTasks.postValue(allInboxTasks)
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