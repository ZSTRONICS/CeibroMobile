package com.zstronics.ceibro.ui.inbox

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentInboxBinding
import com.zstronics.ceibro.ui.inbox.adapter.InboxAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InboxFragment :
    BaseNavViewModelFragment<FragmentInboxBinding, IInbox.State, InboxVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: InboxVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_inbox
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }

    private val integerList = arrayListOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)

    @Inject
    lateinit var adapter: InboxAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.taskRV.adapter = adapter
        adapter.setList(integerList)

//        viewModel.newTasks.observe(viewLifecycleOwner) {
//            if (it.isNullOrEmpty()) {
//                mViewDataBinding.taskRV.visibility = View.GONE
//                mViewDataBinding.fromMeOngoingInfoLayout.visibility = View.VISIBLE
//            } else {
//                adapter.setList(it)
//                mViewDataBinding.taskRV.visibility = View.VISIBLE
//                mViewDataBinding.fromMeOngoingInfoLayout.visibility = View.GONE
//            }
//        }
    }
}