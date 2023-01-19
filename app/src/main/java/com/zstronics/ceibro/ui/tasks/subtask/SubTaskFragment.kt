package com.zstronics.ceibro.ui.tasks.subtask

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.databinding.FragmentSubTaskBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import com.zstronics.ceibro.ui.tasks.task.TaskAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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


    @Inject
    lateinit var adapter: SubTaskAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.subTasks.observe(viewLifecycleOwner) {
            adapter.setList(it)
            mViewDataBinding.allSubTaskCount.text = it.size.toString()
        }
        mViewDataBinding.subTaskRV.adapter = adapter

//        adapter.itemClickListener = { _: View, position: Int, data: CeibroTask ->
//            navigateToTaskDetail(data)
//        }

    }

}