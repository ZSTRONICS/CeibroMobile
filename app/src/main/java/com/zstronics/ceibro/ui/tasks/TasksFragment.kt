package com.zstronics.ceibro.ui.tasks

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTasksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksFragment :
    BaseNavViewModelFragment<FragmentTasksBinding, ITasks.State, TasksVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TasksVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_tasks
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }
}