package com.zstronics.ceibro.ui.tasks.subtaskrejections

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentSubTaskRejectionBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubTaskRejectionFragment :
    BaseNavViewModelFragment<FragmentSubTaskRejectionBinding, ISubTaskRejection.State, SubTaskRejectionVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: SubTaskRejectionVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_sub_task_rejection
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
        }
    }


    @Inject
    lateinit var adapter: SubTaskRejectionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.subtask.observe(viewLifecycleOwner) {

        }

        viewModel.subtaskRejections.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }
        mViewDataBinding.subTaskRejectionRV.adapter = adapter

    }
}