package com.zstronics.ceibro.ui.tasks.subtaskcomments

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubTaskCommentsFragment :
    BaseNavViewModelFragment<FragmentWorksBinding, ISubTaskComments.State, SubTaskCommentsVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: SubTaskCommentsVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_sub_task_comments
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
        }
    }
}