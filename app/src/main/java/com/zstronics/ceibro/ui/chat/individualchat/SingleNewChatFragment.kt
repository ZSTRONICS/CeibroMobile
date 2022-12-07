package com.zstronics.ceibro.ui.chat.individualchat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.databinding.FragmentSingleNewChatBinding
import com.zstronics.ceibro.ui.connections.adapter.AllConnectionsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SingleNewChatFragment :
    BaseNavViewModelFragment<FragmentSingleNewChatBinding, ISingleNewChat.State, SingleNewChatVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: SingleNewChatVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_single_new_chat
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> navigateBack()
        }
    }

    @Inject
    lateinit var adapter: AllConnectionsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView(adapter)

        viewModel.allConnections.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }
        adapter.itemClickListener = { _: View, position: Int, data: MyConnection ->
        }
    }

    private fun initRecyclerView(adapter: AllConnectionsAdapter) {
        mViewDataBinding.connectionRV.adapter = adapter
    }
}