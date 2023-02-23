package com.zstronics.ceibro.ui.tasks

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.TaskMember
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.databinding.FragmentMainTasksBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskFragment
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus
import com.zstronics.ceibro.ui.tasks.task.FragmentTaskFilterSheet
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.task.TasksFragment
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class MainTasksFragment :
    BaseNavViewModelFragment<FragmentMainTasksBinding, IMainTasks.State, MainTasksVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: MainTasksVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_main_tasks
    var selectedFragment = "TasksFragment"
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.taskHeading -> {
                if (selectedFragment == "SubTaskFragment") {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.task_fragment_container, TasksFragment())
                        .commit()
                    selectedFragment = "TasksFragment"
                    mViewDataBinding.subTaskHeading.setBackgroundResource(0)
                    mViewDataBinding.taskHeading.setBackgroundResource(R.drawable.taskselectedback)
                }
            }
            R.id.subTaskHeading -> {
                if (selectedFragment == "TasksFragment") {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.task_fragment_container, SubTaskFragment())
                        .commit()
                    selectedFragment = "SubTaskFragment"
                    mViewDataBinding.taskHeading.setBackgroundResource(0)
                    mViewDataBinding.subTaskHeading.setBackgroundResource(R.drawable.taskselectedback)
                }
            }
            R.id.taskFilterBtn -> {
                if (selectedFragment == "TasksFragment") {
                    val statusList: ArrayList<String> = arrayListOf()
                    statusList.add(TaskStatus.ALL.name.toCamelCase())
                    statusList.add(TaskStatus.NEW.name.toCamelCase())
                    statusList.add(TaskStatus.ACTIVE.name.toCamelCase())
                    statusList.add(TaskStatus.DONE.name.toCamelCase())
                    statusList.add(TaskStatus.DRAFT.name.toCamelCase())
                    showTaskFilterSheet(viewModel.projects, statusList)
                } else if (selectedFragment == "SubTaskFragment") {
                    val statusList: ArrayList<String> = arrayListOf()
                    statusList.add(SubTaskStatus.ALL.name.toCamelCase())
                    statusList.add(SubTaskStatus.ASSIGNED.name.toCamelCase())
                    statusList.add(SubTaskStatus.ACCEPTED.name.toCamelCase())
                    statusList.add(SubTaskStatus.ONGOING.name.toCamelCase())
                    statusList.add(SubTaskStatus.DONE.name.toCamelCase())
                    statusList.add(SubTaskStatus.REJECTED.name.toCamelCase())
                    statusList.add(SubTaskStatus.DRAFT.name.toCamelCase())
                    showTaskFilterSheet(viewModel.projects, statusList)
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.beginTransaction()
            .replace(R.id.task_fragment_container, TasksFragment())
            .commit()
        selectedFragment = "TasksFragment"
        mViewDataBinding.taskHeading.setBackgroundResource(R.drawable.taskselectedback)
    }

    private fun showTaskFilterSheet(
        projects: MutableList<ProjectsWithMembersResponse.ProjectDetail>?,
        statusList: ArrayList<String>
    ) {

        val fragment = FragmentTaskFilterSheet(projects, statusList)

        fragment.onConfirmClickListener =
            { view: View, projectId: String, selectedStatus: String, selectedDueDate: String, assigneeToMembers: List<Member>? ->

                val newMembers = assigneeToMembers?.map {
                    TaskMember(
                        firstName = it.firstName,
                        surName = it.surName,
                        profilePic = it.profilePic,
                        id = it.id,
                        TaskMemberId = 0
                    )
                }
                if (selectedFragment == "TasksFragment") {
                    EventBus.getDefault().post(
                        LocalEvents.ApplyFilterOnTask(
                            projectId, selectedStatus, selectedDueDate, newMembers
                        )
                    )
                } else {
                    EventBus.getDefault().post(
                        LocalEvents.ApplyFilterOnSubTask(
                            projectId, selectedStatus, selectedDueDate, newMembers
                        )
                    )
                }
            }

        fragment.onClearAllClickListener =
            {
                if (selectedFragment == "TasksFragment")
                    EventBus.getDefault().post(
                        LocalEvents.ClearTaskFilters
                    )
                else
                    EventBus.getDefault().post(
                        LocalEvents.ClearSubtaskFilters
                    )
            }
        fragment.show(childFragmentManager, "FragmentTaskFilterSheet")
    }
}