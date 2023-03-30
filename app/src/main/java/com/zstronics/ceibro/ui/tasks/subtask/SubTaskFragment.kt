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
import com.zstronics.ceibro.data.database.models.subtask.SubTaskStateItem
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
            R.id.allSubTaskFilter -> viewModel.applyStatusFilter(SubTaskStatus.ALL.name)
            R.id.ongoingSubTaskFilter -> viewModel.applyStatusFilter(SubTaskStatus.ONGOING.name)
            R.id.assignedSubTaskFilter -> viewModel.applyStatusFilter(SubTaskStatus.ASSIGNED.name)
            R.id.acceptedSubTaskFilter -> viewModel.applyStatusFilter(SubTaskStatus.ACCEPTED.name)
            R.id.rejectedSubTaskFilter -> viewModel.applyStatusFilter(SubTaskStatus.REJECTED.name)
            R.id.doneSubTaskFilter -> viewModel.applyStatusFilter(SubTaskStatus.DONE.name)
            R.id.draftSubTaskFilter -> viewModel.applyStatusFilter(SubTaskStatus.DRAFT.name)
        }
    }


    @Inject
    lateinit var adapter: SubTaskAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.subTasks.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }

        viewModel.subTasksForStatus.observe(viewLifecycleOwner) {
            var allCount = 0
            var ongoingCount = 0
            var assignedCount = 0
            var acceptedCount = 0
            var rejectedCount = 0
            var doneCount = 0
            var draftCount = 0

            allCount = it.size

            for (subtask in it) {
                if (viewModel.getState(subtask.state).equals(SubTaskStatus.ONGOING.name, true)) {
                    ongoingCount++
                }
                else if (viewModel.getState(subtask.state).equals(SubTaskStatus.ASSIGNED.name, true)) {
                    assignedCount++
                }
                else if (viewModel.getState(subtask.state).equals(SubTaskStatus.ACCEPTED.name, true)) {
                    acceptedCount++
                }
                else if (viewModel.getState(subtask.state).equals(SubTaskStatus.REJECTED.name, true)) {
                    rejectedCount++
                }
                else if (viewModel.getState(subtask.state).equals(SubTaskStatus.DONE.name, true)) {
                    doneCount++
                }
                else if (viewModel.getState(subtask.state).equals(SubTaskStatus.DRAFT.name, true)) {
                    draftCount++
                }
            }
            mViewDataBinding.allSubTaskCount.text =
                if (allCount > 99)
                    "99+"
                else
                    allCount.toString()

            mViewDataBinding.ongoingSubTaskCount.text =
                if (ongoingCount > 99)
                    "99+"
                else
                    ongoingCount.toString()

            mViewDataBinding.assignedSubTaskCount.text =
                if (assignedCount > 99)
                    "99+"
                else
                    assignedCount.toString()

            mViewDataBinding.acceptedSubTaskCount.text =
                if (acceptedCount > 99)
                    "99+"
                else
                    acceptedCount.toString()

            mViewDataBinding.rejectedSubTaskCount.text =
                if (rejectedCount > 99)
                    "99+"
                else
                    rejectedCount.toString()

            mViewDataBinding.doneSubTaskCount.text =
                if (doneCount > 99)
                    "99+"
                else
                    doneCount.toString()

            mViewDataBinding.draftSubTaskCount.text =
                if (draftCount > 99)
                    "99+"
                else
                    draftCount.toString()
        }


        mViewDataBinding.subTaskRV.adapter = adapter

        adapter.itemClickListener = { _: View, position: Int, data: AllSubtask ->
            navigateToSubTaskDetail(data)
        }
        adapter.simpleChildItemClickListener = { childView: View, position: Int, data: AllSubtask ->
            showSubtaskCardMenuPopup(childView, data)
        }
        adapter.deleteChildItemClickListener = { childView: View, position: Int, data: AllSubtask ->
            showDeleteDialog(
                childView,
                requireContext().getString(R.string.are_you_sure_you_want_to_delete_this_subtask_heading),
                data
            )
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
    }

    private fun popUpMenu(v: View, subtaskData: AllSubtask): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_subtask_card_menu, null)

        //following code is to make popup at top if the view is at bottom
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view

        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        //ShowAsDropDown statement at bottom, according to the view visibilities
        //////////////////////


        val editSubTask = view.findViewById<View>(R.id.editSubTask)
        val editDetails = view.findViewById<View>(R.id.editDetails)
        val deleteSubtask = view.findViewById<View>(R.id.deleteSubtask)

        val isSubTaskCreator = isSubTaskCreator(viewModel.user?.id, subtaskData.creator)
        val isTAdmin = isTaskAdmin(viewModel.user?.id, subtaskData.taskData?.admins)

        val userState =
            subtaskData.state?.find { it.userId == viewModel.user?.id }?.userState?.uppercase()
                ?: SubTaskStatus.DRAFT.name

        val allUsersOnAssignState = areAllOnAssignedState(subtaskData.state)


        if (isSubTaskCreator || isTAdmin) {
            if (userState.uppercase() == SubTaskStatus.DRAFT.name) {
                if (isSubTaskCreator) {
                    deleteSubtask.visibility = View.VISIBLE
                } else {
                    deleteSubtask.visibility = View.GONE
                }
                editSubTask.visibility = View.VISIBLE
                editDetails.visibility = View.GONE
            } else if (allUsersOnAssignState) {
                if (isSubTaskCreator) {
                    deleteSubtask.visibility = View.VISIBLE
                } else {
                    deleteSubtask.visibility = View.GONE
                }
                editSubTask.visibility = View.VISIBLE
                editDetails.visibility = View.VISIBLE
            } else {
                deleteSubtask.visibility = View.GONE
                editSubTask.visibility = View.GONE
                editDetails.visibility = View.VISIBLE
            }
        } else {
            if (userState.uppercase() == SubTaskStatus.DRAFT.name || userState.uppercase() == SubTaskStatus.ASSIGNED.name || userState.uppercase() == SubTaskStatus.REJECTED.name) {
                deleteSubtask.visibility = View.GONE
                editSubTask.visibility = View.GONE
                editDetails.visibility = View.GONE
            }
            else {
                deleteSubtask.visibility = View.GONE
                editSubTask.visibility = View.GONE
                editDetails.visibility = View.VISIBLE
            }
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
            showDeleteDialog(
                v,
                context.getString(R.string.are_you_sure_you_want_to_delete_this_subtask_heading),
                subtaskData
            )
            popupWindow.dismiss()
        }


        if (positionOfIcon > height) {
            if (editSubTask.visibility == View.VISIBLE && editDetails.visibility == View.VISIBLE && deleteSubtask.visibility == View.VISIBLE) {
                popupWindow.showAsDropDown(v, 0, -530)
            }
            else if (deleteSubtask.visibility == View.GONE) {
                popupWindow.showAsDropDown(v, 0, -240)
            }
            else {
                popupWindow.showAsDropDown(v, 0, -405)
            }
        } else {
            popupWindow.showAsDropDown(v, 0, 5)
        }
        return popupWindow
    }

    private fun showDeleteDialog(v: View, title: String, subtaskData: AllSubtask) {
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

    private fun areAllOnAssignedState(state: List<SubTaskStateItem>?): Boolean {
        var allOnAssignState = false
        var size = 0

        if (state != null) {
            for (userState in state) {
                if (userState.userState.uppercase() == SubTaskStatus.ASSIGNED.name) {
                    size++
                }
            }
            if (size == state.size) {
                allOnAssignState = true
            }
        }

        return allOnAssignState
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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}