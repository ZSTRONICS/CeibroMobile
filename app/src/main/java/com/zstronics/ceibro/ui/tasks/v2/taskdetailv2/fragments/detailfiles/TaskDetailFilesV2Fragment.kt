package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailfiles

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTaskDetailFilesV2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskDetailFilesV2Fragment :
    BaseNavViewModelFragment<FragmentTaskDetailFilesV2Binding, ITaskDetailFilesV2.State, TaskDetailFilesV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskDetailFilesV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_detail_files_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }
}