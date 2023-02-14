package com.zstronics.ceibro.ui.tasks.taskdetailview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.databinding.FragmentTaskDetailBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import com.zstronics.ceibro.ui.questioner.createquestion.members.FragmentQuestionParticipantsSheet
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskAdapter
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class TaskDetailFragment :
    BaseNavViewModelFragment<FragmentTaskDetailBinding, ITaskDetail.State, TaskDetailVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskDetailVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_detail
    override fun toolBarVisibility(): Boolean = false
    var isTaskAdmin = false
    var isTaskCreator = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> navigateBack()
            R.id.createSubTaskBtn -> navigateToNewSubTaskCreation()
            R.id.taskTitleLayout -> showTaskDetailSheet()
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
        mViewDataBinding.taskDetailSubTasksRV.adapter = adapter

        adapter.itemClickListener = { _: View, position: Int, data: AllSubtask ->
            navigateToSubTaskDetail(data)
        }
        adapter.simpleChildItemClickListener = { childView: View, position: Int, data: AllSubtask ->
            showSubtaskCardMenuPopup(childView, data)
        }
        adapter.childItemClickListener =
            { childView: View, position: Int, data: AllSubtask, callBack: (result: Triple<Boolean, Boolean, Boolean>) -> Unit ->
                when (childView.id) {
                    R.id.assignedStateRejectBtn, R.id.acceptedStateRejectBtn ->
                        viewModel.rejectSubTask(data, SubTaskStatus.REJECTED, callBack) {
                            navigateBack()
                        }
                    R.id.draftStateAssignBtn -> {
                        if (data.assignedTo.isNotEmpty()) {
                            viewModel.updateSubtaskStatus(
                                data,
                                SubTaskStatus.ASSIGNED,
                                callBack
                            ) {
                                navigateBack()
                            }
                        } else {
                            shortToastNow("There are no assign to members in subtask")
                        }
                    }
                    R.id.assignedStateAcceptBtn -> viewModel.updateSubtaskStatus(
                        data,
                        SubTaskStatus.ACCEPTED,
                        callBack
                    ) {
                        navigateBack()
                    }
                }
            }

        viewModel.task.observe(viewLifecycleOwner) { item ->
            val isAdmin = isTaskAdmin(viewModel.user?.id, item.admins)
            val isCreator = isTaskCreator(viewModel.user?.id, item.creator)
            isTaskAdmin = isAdmin
            isTaskCreator = isCreator

            with(mViewDataBinding) {
                val taskStatusNameBg: Pair<Int, String> = when (item.state.uppercase()) {
                    TaskStatus.NEW.name -> Pair(
                        R.drawable.status_assigned_outline,
                        requireContext().getString(R.string.new_heading)
                    )
                    TaskStatus.ACTIVE.name -> Pair(
                        R.drawable.status_ongoing_outline,
                        requireContext().getString(R.string.active_heading)
                    )
                    TaskStatus.DRAFT.name -> Pair(
                        R.drawable.status_draft_outline,
                        requireContext().getString(R.string.draft_heading)
                    )
                    TaskStatus.DONE.name -> Pair(
                        R.drawable.status_done_outline,
                        requireContext().getString(R.string.done_heading)
                    )
                    else -> Pair(
                        R.drawable.status_draft_outline,
                        item.state
                    )
                }

                val (background, stringRes) = taskStatusNameBg
                taskDetailStatusName.setBackgroundResource(background)
                taskDetailStatusName.text = stringRes

                if (isAdmin || isCreator) {
                    createSubTaskBtn.visibility = View.VISIBLE
                }
                else {
                    createSubTaskBtn.visibility = View.GONE
                }


                taskDetailDueDate.text = DateUtils.reformatStringDate(
                    date = item.dueDate,
                    DateUtils.FORMAT_YEAR_MON_DATE,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                )
                if (taskDetailDueDate.text == "") {                              // Checking if date format was not yyyy-MM-dd then it will be empty
                    taskDetailDueDate.text = DateUtils.reformatStringDate(
                        date = item.dueDate,
                        DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                        DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                    )
                    if (taskDetailDueDate.text == "") {                          // Checking if date format was not dd-MM-yyyy then still it is empty
                        taskDetailDueDate.text = requireContext().getString(R.string.invalid_due_date_text)
                    }
                }

                taskTitle.text = item.title

                taskDetailProjectName.text = item.project.title
                taskDetailSubTaskCount.text = "${item.totalSubTaskCount}/${item.totalSubTaskCount}"

            }
        }
    }

    private fun navigateToNewSubTaskCreation() {
        val bundle = Bundle()
        bundle.putBoolean("newSubTask", true)
        bundle.putParcelable("task", viewModel.task.value)
        navigate(R.id.newSubTaskFragment, bundle)
    }
    private fun navigateToEditSubTask(subtaskData: AllSubtask) {
        val bundle = Bundle()
        bundle.putBoolean("newSubTask", false)
        bundle.putParcelable("task", viewModel.task.value)
        bundle.putParcelable("subtask", subtaskData)
        navigate(R.id.newSubTaskFragment, bundle)
    }

    private fun showTaskDetailSheet() {
        val fragment = viewModel.task.value?.let { FragmentTaskDetailSheet(it.title, it.description?: "") }
        fragment?.show(childFragmentManager, "FragmentTaskDetailSheet")
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
        val userState = subtaskData.state?.find { it.userId == viewModel.user?.id }?.userState?.uppercase()
            ?: TaskStatus.DRAFT.name

        if (isSubTaskCreator || isTaskAdmin) {
            if (userState.uppercase() == SubTaskStatus.DRAFT.name) {
                deleteSubtask.visibility = View.VISIBLE
                editSubTask.visibility = View.VISIBLE
                editDetails.visibility = View.GONE
            }
            else if (userState.uppercase() == SubTaskStatus.ASSIGNED.name) {
                deleteSubtask.visibility = View.VISIBLE
                editSubTask.visibility = View.VISIBLE
                editDetails.visibility = View.VISIBLE
            }
            else {
                deleteSubtask.visibility = View.GONE
                editSubTask.visibility = View.GONE
                editDetails.visibility = View.VISIBLE
            }
        }
        else {
            deleteSubtask.visibility = View.GONE
            editSubTask.visibility = View.GONE
            editDetails.visibility = View.GONE
        }


        editSubTask.setOnClickListener {
            navigateToEditSubTask(subtaskData)
            popupWindow.dismiss()
        }
        editDetails.setOnClickListener {
            shortToastNow("Edit Details")
            popupWindow.dismiss()
        }
        deleteSubtask.setOnClickListener {
            shortToastNow("Delete SubTask")
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



    private fun isTaskAdmin(userId: String?, admins: List<TaskMember>): Boolean {
        var isAdmin = false
        val member = admins.find { it.id == userId }
        if (member?.id.equals(userId)) {
            isAdmin = true
        }
        return isAdmin
    }

    private fun isTaskCreator(userId: String?, creator: TaskMember?): Boolean {
        var isCreator = false
        if (creator?.id.equals(userId)) {
            isCreator = true
        }
        return isCreator
    }

    private fun isSubTaskCreator(userId: String?, creator: TaskMember?): Boolean {
        var isCreator = false
        if (creator?.id.equals(userId)) {
            isCreator = true
        }
        return isCreator
    }

    private fun navigateToSubTaskDetail(data: AllSubtask) {
        val bundle = Bundle()
        bundle.putParcelable("subtask", data)
        navigate(R.id.subTaskDetailFragment, bundle)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSubTaskCreatedEvent(event: LocalEvents.SubTaskCreatedEvent?) {
        if (viewModel.isCurrentTaskId(event?.taskId))
            event?.taskId?.let { viewModel.getSubTasks(it) }
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