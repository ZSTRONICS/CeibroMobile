package com.zstronics.ceibro.ui.tasks.subtask

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.databinding.FragmentSubTaskBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class SubTaskFragment :
    BaseNavViewModelFragment<FragmentSubTaskBinding, ISubTask.State, SubTaskVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: SubTaskVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_sub_task
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            115 -> shortToastNow("Edit Details")
            116 -> shortToastNow("Close Subtask")
        }
    }


    @Inject
    lateinit var adapter: SubTaskAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.subTasks.observe(viewLifecycleOwner) {
            adapter.setList(it)
            mViewDataBinding.allSubTaskCount.text = it.size.toString()
        }
        mViewDataBinding.subTaskRV.adapter = adapter

        adapter.itemClickListener = { _: View, position: Int, data: AllSubtask ->
            navigateToSubTaskDetail(data)
        }
        adapter.simpleChildItemClickListener = { childView: View, position: Int, data: AllSubtask ->
            viewModel.showSubtaskCardMenuPopup(childView)
        }

        adapter.childItemClickListener =
            { childView: View, position: Int, data: AllSubtask, callBack: (result: Triple<Boolean, Boolean, Boolean>) -> Unit ->
                when (childView.id) {
                    R.id.assignedStateRejectBtn ->
                        viewModel.rejectSubTask(data, SubTaskStatus.REJECTED, callBack)
                    R.id.acceptedStateRejectBtn ->
                        viewModel.rejectSubTask(data, SubTaskStatus.REJECTED, callBack)
                    R.id.draftStateAssignBtn -> {
                        if (data.assignedTo.isNotEmpty()) {
                            viewModel.updateSubtaskStatus(
                                data,
                                SubTaskStatus.ASSIGNED,
                                callBack
                            )
                        } else {
                            shortToastNow("There are no assign to members in subtask")
                        }
                    }
                    R.id.assignedStateAcceptBtn -> viewModel.updateSubtaskStatus(
                        data,
                        SubTaskStatus.ACCEPTED,
                        callBack
                    )
                }
            }
    }


    private fun navigateToSubTaskDetail(data: AllSubtask) {
        val bundle = Bundle()
        bundle.putParcelable("subtask", data)
        navigate(R.id.subTaskDetailFragment, bundle)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSubTaskCreatedEvent(event: LocalEvents.SubTaskCreatedEvent?) {
        viewModel.getSubTasks()
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