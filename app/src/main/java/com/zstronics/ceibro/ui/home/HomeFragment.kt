package com.zstronics.ceibro.ui.home

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
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.databinding.FragmentHomeBinding
import com.zstronics.ceibro.ui.projects.adapter.AllProjectsAdapter
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskAdapter
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment :
    BaseNavViewModelFragment<FragmentHomeBinding, IHome.State, HomeVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: HomeVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_home
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.viewAllProjectsBtn -> {  }
        }
    }


    @Inject
    lateinit var projectAdapter: AllProjectsAdapter
    @Inject
    lateinit var taskAdapter: TaskAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView(projectAdapter, taskAdapter)

        viewModel.homeProjects.observe(viewLifecycleOwner) {
            if (it != null) {
                projectAdapter.setList(it)
            }
        }
        projectAdapter.itemClickListener =
            { _: View, position: Int, data: AllProjectsResponse.Projects ->
                navigate(
                    R.id.createProjectMainFragment,
                    bundleOf(AllProjectsResponse.Projects::class.java.name to data)
                )
            }


        viewModel.homeTasks.observe(viewLifecycleOwner) {
            taskAdapter.setList(it)
        }
        taskAdapter.itemClickListener = { _: View, position: Int, data: CeibroTask ->
            navigateToTaskDetail(data)
        }
        taskAdapter.menuChildItemClickListener = { childView: View, position: Int, data: CeibroTask ->
            showTaskCardMenuPopup(childView, data)
        }
    }

    private fun initRecyclerView(projectAdapter: AllProjectsAdapter, taskAdapter: TaskAdapter) {
        mViewDataBinding.homeProjectRV.setLayoutManager(object : LinearLayoutManager(mViewDataBinding.homeProjectRV.context, HORIZONTAL, false) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                // force width of recyclerview
                lp.width = (width / 1.12).toInt()
                return true
            }
        })

        mViewDataBinding.homeProjectRV.adapter = projectAdapter

        mViewDataBinding.homeTasksRV.setLayoutManager(object : LinearLayoutManager(mViewDataBinding.homeTasksRV.context, HORIZONTAL, false) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                // force width of recyclerview
                lp.width = (width / 1.12).toInt()
                return true
            }
        })

        mViewDataBinding.homeTasksRV.adapter = taskAdapter

    }




    private fun navigateToEditTask(data: CeibroTask) {
        val bundle = Bundle()
        bundle.putBoolean("newTask", false)
        bundle.putParcelable("task", data)
        navigate(R.id.newTaskFragment, bundle)
    }
    private fun navigateToTaskDetail(data: CeibroTask) {
        val bundle = Bundle()
        bundle.putParcelable("task", data)
        navigate(R.id.taskDetailFragment, bundle)
    }

    private fun showTaskCardMenuPopup(v: View, taskData: CeibroTask) {
        val popUpWindowObj = popUpMenu(v, taskData)
    }

    private fun popUpMenu(v: View, taskData: CeibroTask): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_task_card_menu, null)

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

        if (positionOfIcon > height) {
            if (deleteTask.visibility == View.GONE) {
                popupWindow.showAsDropDown(v, -135, -245)
            }
            else {
                popupWindow.showAsDropDown(v, -170, -405)
            }
        } else {
            popupWindow.showAsDropDown(v, 0, 5)
        }
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

}