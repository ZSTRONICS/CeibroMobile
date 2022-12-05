package com.zstronics.ceibro.ui.works

import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorksFragment :
    BaseNavViewModelFragment<FragmentWorksBinding, IWorks.State, WorksVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: WorksVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_works
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }
}