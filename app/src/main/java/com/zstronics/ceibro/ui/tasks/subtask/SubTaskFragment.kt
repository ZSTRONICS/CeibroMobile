package com.zstronics.ceibro.ui.tasks.subtask

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.databinding.FragmentSubTaskBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
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
            showSubtaskCardMenuPopup(childView, data)
        }

        adapter.childItemClickListener =
            { childView: View, position: Int, data: AllSubtask, callBack: (result: Triple<Boolean, Boolean, Boolean>) -> Unit ->
                when (childView.id) {
                    R.id.assignedStateRejectBtn, R.id.acceptedStateRejectBtn -> {
                        val dialog = Dialog(childView.context)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setCancelable(false)
                        dialog.setContentView(R.layout.layout_reject_subtask)
                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        val descriptionText =
                            dialog.findViewById(R.id.descriptionText) as AppCompatTextView
                        descriptionText.text =
                            requireContext().getString(R.string.are_you_sure_you_want_to_reject_this_subtask_heading)
                        val rejectDescriptionText =
                            dialog.findViewById(R.id.rejectDescriptionText) as TextInputEditText
                        val rejectSubTaskBtn =
                            dialog.findViewById(R.id.rejectSubTaskBtn) as AppCompatButton
                        val cancelSubTaskBtn =
                            dialog.findViewById(R.id.cancelSubTaskBtn) as AppCompatButton

                        rejectSubTaskBtn.setOnClickListener {
                            if (rejectDescriptionText.text.toString() == "") {
                                shortToastNow(requireContext().getString(R.string.please_enter_a_reason_to_reject_heading))
                            } else {
                                dialog.dismiss()
                                viewModel.rejectSubTask(data, SubTaskStatus.REJECTED, callBack)
                            }
                        }
                        cancelSubTaskBtn.setOnClickListener {
                            dialog.dismiss()
                        }
                        dialog.show()
                    }

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
                    R.id.acceptedStateStartBtn -> viewModel.updateSubtaskStatus(
                        data,
                        SubTaskStatus.START,
                        callBack
                    )
                    R.id.ongoingStateDoneBtn -> viewModel.updateSubtaskStatus(
                        data,
                        SubTaskStatus.DONE,
                        callBack
                    )
                }
            }
    }

    private fun showSubtaskCardMenuPopup(v: View, subtaskData: AllSubtask) {
        val popUpWindowObj = popUpMenu(v, subtaskData)
        popUpWindowObj.showAsDropDown(v.findViewById(R.id.subTaskMoreMenuBtn), 0, 10)
    }

    private fun popUpMenu(v: View, subtaskData: AllSubtask): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_subtask_card_menu, null)

        val editSubTask = view.findViewById<View>(R.id.editSubTask)
        val editDetails = view.findViewById<View>(R.id.editDetails)
        val deleteSubtask = view.findViewById<View>(R.id.deleteSubtask)

        val isSubTaskCreator = isSubTaskCreator(viewModel.user?.id, subtaskData.creator)
        val isTAdmin = isTaskAdmin(viewModel.user?.id, subtaskData.taskData?.admins)

        val userState =
            subtaskData.state?.find { it.userId == viewModel.user?.id }?.userState?.uppercase()
                ?: SubTaskStatus.DRAFT.name

        if (isSubTaskCreator || isTAdmin) {
            if (userState.uppercase() == SubTaskStatus.DRAFT.name) {
                deleteSubtask.visibility = View.VISIBLE
                editSubTask.visibility = View.VISIBLE
                editDetails.visibility = View.GONE
            } else if (userState.uppercase() == SubTaskStatus.ASSIGNED.name) {
                deleteSubtask.visibility = View.VISIBLE
                editSubTask.visibility = View.VISIBLE
                editDetails.visibility = View.VISIBLE
            } else {
                deleteSubtask.visibility = View.GONE
                editSubTask.visibility = View.GONE
                editDetails.visibility = View.VISIBLE
            }
        } else {
            deleteSubtask.visibility = View.GONE
            editSubTask.visibility = View.GONE
            editDetails.visibility = View.VISIBLE
        }


        editSubTask.setOnClickListener {
            navigateToEditSubTask(subtaskData)
            popupWindow.dismiss()
        }
        editDetails.setOnClickListener {
            navigateToEditDetails(subtaskData)
            popupWindow.dismiss()
        }
        deleteSubtask.setOnClickListener {
            showDialog(
                v,
                context.getString(R.string.are_you_sure_you_want_to_delete_this_subtask_heading),
                subtaskData
            )
            popupWindow.dismiss()
        }

        popupWindow.isFocusable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 13f
        return popupWindow
    }

    private fun showDialog(v: View, title: String, subtaskData: AllSubtask) {
        val dialog = Dialog(v.context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_delete_task)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val descriptionText = dialog.findViewById(R.id.descriptionText) as AppCompatTextView
        descriptionText.text = title
        val deleteTaskBtn = dialog.findViewById(R.id.deleteTaskBtn) as AppCompatButton
        val cancelTaskBtn = dialog.findViewById(R.id.cancelTaskBtn) as AppCompatButton
        deleteTaskBtn.setOnClickListener {
            dialog.dismiss()
            viewModel.deleteSubTask(subtaskData.id)
        }
        cancelTaskBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun isTaskAdmin(userId: String?, admins: List<String>?): Boolean {
        var isAdmin = false
        val memberId = admins?.find { it == userId }
        if (memberId.equals(userId)) {
            isAdmin = true
        }
        return isAdmin
    }

    private fun isSubTaskCreator(userId: String?, creator: TaskMember?): Boolean {
        var isCreator = false
        if (creator?.id.equals(userId)) {
            isCreator = true
        }
        return isCreator
    }

    private fun navigateToEditSubTask(subtaskData: AllSubtask) {
        val bundle = Bundle()
        bundle.putBoolean("newSubTask", false)
        bundle.putParcelable("subtask", subtaskData)
        navigate(R.id.newSubTaskFragment, bundle)
    }

    private fun navigateToEditDetails(subtaskData: AllSubtask) {
        val bundle = Bundle()
        bundle.putParcelable("subtask", subtaskData)
        navigate(R.id.editSubTaskDetailsFragment, bundle)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onApplyFilterOnTaskAndSubTask(event: LocalEvents.ApplyFilterOnTaskAndSubTask) {
        viewModel.applyFilter(event)
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