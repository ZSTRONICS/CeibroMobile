package com.zstronics.ceibro.ui.tasks.v2.newtask.taskproject

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTaskProjectBinding
import com.zstronics.ceibro.databinding.FragmentTopicBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskProjectFragment :
    BaseNavViewModelFragment<FragmentTaskProjectBinding, ITaskProject.State, TaskProjectVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskProjectVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_project
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }
}