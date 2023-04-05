package com.zstronics.ceibro.ui.connections

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.databinding.FragmentConnectionsBinding
import com.zstronics.ceibro.ui.connections.adapter.AllConnectionsAdapter
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionsFragment :
    BaseNavViewModelFragment<FragmentConnectionsBinding, IConnections.State, ConnectionsVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ConnectionsVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_connections
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> navigateBack()
            R.id.inviteMainBtn -> navigateToInvitations()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadConnections(mViewDataBinding.connectionRV)
    }

    private fun navigateToInvitations() {
        navigate(R.id.invitationsFragment)
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
            //navigateToMsgView(data)
        }
        adapter.childItemClickListener = { view: View, position: Int, data: MyConnection ->
            //if (view.id == R.id.chatFavIcon)
            //viewModel.addChatToFav(data.id)
        }
    }

    private fun initRecyclerView(adapter: AllConnectionsAdapter) {
        mViewDataBinding.connectionRV.adapter = adapter

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onConnectionRefreshEvent(event: LocalEvents.ConnectionRefreshEvent?) {
        viewModel.loadConnections(mViewDataBinding.connectionRV)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

}