package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentOngoingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OngoingFragment :
    BaseNavViewModelFragment<FragmentOngoingBinding, IOngoing.State, OngoingVM>() {
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: OngoingVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_ongoing
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {


        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


}