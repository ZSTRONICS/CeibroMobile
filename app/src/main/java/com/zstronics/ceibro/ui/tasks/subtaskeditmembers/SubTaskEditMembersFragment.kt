package com.zstronics.ceibro.ui.tasks.subtaskeditmembers

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentSubTaskEditMembersBinding
import com.zstronics.ceibro.databinding.FragmentSubTaskRejectionBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubTaskEditMembersFragment :
    BaseNavViewModelFragment<FragmentSubTaskEditMembersBinding, ISubTaskEditMembers.State, SubTaskEditMembersVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: SubTaskEditMembersVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_sub_task_edit_members
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
        }
    }
}