package com.zstronics.ceibro.ui.tasks.subtask

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentSubTaskBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubTaskFragment :
    BaseNavViewModelFragment<FragmentSubTaskBinding, ISubTask.State, SubTaskVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: SubTaskVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_sub_task
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }
}