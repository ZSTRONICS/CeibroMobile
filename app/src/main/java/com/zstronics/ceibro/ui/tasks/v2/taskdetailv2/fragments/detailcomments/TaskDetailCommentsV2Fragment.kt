package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailcomments

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTaskDetailCommentsV2Binding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskDetailCommentsV2Fragment :
    BaseNavViewModelFragment<FragmentTaskDetailCommentsV2Binding, ITaskDetailCommentsV2.State, TaskDetailCommentsV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskDetailCommentsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_detail_comments_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }
}