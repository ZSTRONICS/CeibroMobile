package com.zstronics.ceibro.ui.tasks.task

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
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.databinding.FragmentTasksBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject


@AndroidEntryPoint
class TasksFragment :
    BaseNavViewModelFragment<FragmentTasksBinding, ITasks.State, TasksVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TasksVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_tasks
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.createTaskBtn -> navigateToNewTaskCreation()
            119 -> shortToastNow("Edit Task")
            120 -> shortToastNow("Delete Task")
            R.id.allTaskFilter -> viewModel.applyStatusFilter(TaskStatus.ALL.name)
            R.id.newTaskFilter -> viewModel.applyStatusFilter(TaskStatus.NEW.name)
            R.id.activeTaskFilter -> viewModel.applyStatusFilter(TaskStatus.ACTIVE.name)
            R.id.doneTaskFilter -> viewModel.applyStatusFilter(TaskStatus.DONE.name)
            R.id.draftTaskFilter -> viewModel.applyStatusFilter(TaskStatus.DRAFT.name)
        }
    }

    private fun navigateToNewTaskCreation() {
        val bundle = Bundle()
        bundle.putBoolean("newTask", true)
        navigate(R.id.newTaskFragment, bundle)
    }

    private fun navigateToEditTask(data: CeibroTask) {
        val bundle = Bundle()
        bundle.putBoolean("newTask", false)
        bundle.putParcelable("task", data)
        navigate(R.id.newTaskFragment, bundle)
    }

    @Inject
    lateinit var adapter: TaskAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.tasks.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }

        viewModel.tasksForStatus.observe(viewLifecycleOwner) {
            var allCount = 0
            var newCount = 0
            var activeCount = 0
            var doneCount = 0
            var draftCount = 0

            allCount = it.size

            for (task in it) {
                if (task.state.equals(TaskStatus.NEW.name, true)) {
                    newCount++
                }
                else if (task.state.equals(TaskStatus.ACTIVE.name, true)) {
                    activeCount++
                }
                else if (task.state.equals(TaskStatus.DONE.name, true)) {
                    doneCount++
                }
                else if (task.state.equals(TaskStatus.DRAFT.name, true)) {
                    draftCount++
                }
            }
            mViewDataBinding.allTaskCount.text = allCount.toString()
            mViewDataBinding.newTaskCount.text = newCount.toString()
            mViewDataBinding.activeTaskCount.text = activeCount.toString()
            mViewDataBinding.doneTaskCount.text = doneCount.toString()
            mViewDataBinding.draftTaskCount.text = draftCount.toString()

        }

        mViewDataBinding.taskRV.adapter = adapter

        adapter.itemClickListener = { _: View, position: Int, data: CeibroTask ->
            navigateToTaskDetail(data)
        }
        adapter.menuChildItemClickListener = { childView: View, position: Int, data: CeibroTask ->
            showTaskCardMenuPopup(childView, data)
        }

    }

    private fun navigateToTaskDetail(data: CeibroTask) {
        val bundle = Bundle()
        bundle.putParcelable("task", data)
        navigate(R.id.taskDetailFragment, bundle)
    }

    private fun showTaskCardMenuPopup(v: View, taskData: CeibroTask) {
        val popUpWindowObj = popUpMenu(v, taskData)
        popUpWindowObj.showAsDropDown(v.findViewById(R.id.taskMoreMenuBtn), 0, 10)
    }

    private fun popUpMenu(v: View, taskData: CeibroTask): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_task_card_menu, null)

        val editTask = view.findViewById<View>(R.id.editTask)
        val deleteTask = view.findViewById<View>(R.id.deleteTask)

        val isCreator = isTaskCreator(viewModel.user?.id, taskData.creator)

        if (isCreator) {
            if (taskData.state.uppercase() == TaskStatus.DRAFT.name || taskData.state.uppercase() == TaskStatus.NEW.name) {
                deleteTask.visibility = View.VISIBLE
            } else {
                deleteTask.visibility = View.GONE
            }
        } else {
            deleteTask.visibility = View.GONE
        }


        editTask.setOnClickListener {
            navigateToEditTask(taskData)
            popupWindow.dismiss()
        }
        deleteTask.setOnClickListener {
            showDialog(
                v,
                context.getString(R.string.are_you_sure_you_want_to_delete_the_task_heading),
                taskData
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

    private fun showDialog(v: View, title: String, taskData: CeibroTask) {
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
            viewModel.deleteTask(taskData._id)
        }
        cancelTaskBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun isTaskCreator(userId: String?, creator: TaskMember?): Boolean {
        var isCreator = false
        if (creator?.id.equals(userId)) {
            isCreator = true
        }
        return isCreator
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskCreatedEvent(event: LocalEvents.TaskCreatedEvent?) {
//        showToast("New Task Created")
        viewModel.getTasks()
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