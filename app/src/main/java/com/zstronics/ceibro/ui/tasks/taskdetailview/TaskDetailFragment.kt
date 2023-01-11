package com.zstronics.ceibro.ui.tasks.taskdetailview

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTaskDetailBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskDetailFragment :
    BaseNavViewModelFragment<FragmentTaskDetailBinding, ITaskDetail.State, TaskDetailVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskDetailVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_detail
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> navigateBack()
            R.id.createSubTaskBtn -> navigateToNewSubTaskCreation()
        }
    }

    private fun navigateToNewSubTaskCreation() {
        navigate(R.id.newSubTaskFragment)
    }
}