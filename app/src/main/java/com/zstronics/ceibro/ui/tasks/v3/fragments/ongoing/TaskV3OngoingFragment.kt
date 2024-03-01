package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTaskV3OngoingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskV3OngoingFragment :
    BaseNavViewModelFragment<FragmentTaskV3OngoingBinding, ITaskV3Ongoing.State, TaskV3OngoingVM>() {
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskV3OngoingVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_v3_ongoing
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {


        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


}