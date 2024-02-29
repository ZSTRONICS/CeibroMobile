package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing

import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentOngoingBinding
import com.zstronics.ceibro.ui.inbox.adapter.InboxAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OngoingFragment :
    BaseNavViewModelFragment<FragmentOngoingBinding, IOngoing.State, OngoingVM>() {

    @Inject
    lateinit var adapter: InboxAdapter
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: OngoingVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_ongoing
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {

            R.id.clTaskType -> {

            }

            R.id.imgSearch -> {
                mViewDataBinding.tasksSearchCard.visibility = View.VISIBLE
            }

            R.id.cancelSearch -> {
                mViewDataBinding.taskSearchBar.setQuery(null, false)
                mViewDataBinding.tasksSearchCard.visibility = View.GONE
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewDataBinding.taskRV.adapter = adapter

    }
}