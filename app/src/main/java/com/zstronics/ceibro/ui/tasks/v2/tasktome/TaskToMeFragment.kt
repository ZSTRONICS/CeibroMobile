package com.zstronics.ceibro.ui.tasks.v2.tasktome

import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTaskToMeBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskToMeFragment :
    BaseNavViewModelFragment<FragmentTaskToMeBinding, ITaskToMe.State, TaskToMeVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskToMeVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_to_me
    override fun toolBarVisibility(): Boolean = false
    var buttonOnSide = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.createNewTaskBtn -> {
                navigate(R.id.newTaskV2Fragment)
            }
        }
    }
}