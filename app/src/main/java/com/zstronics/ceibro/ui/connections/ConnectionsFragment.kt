package com.zstronics.ceibro.ui.connections

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.finish
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.databinding.FragmentConnectionsBinding
import com.zstronics.ceibro.ui.connections.adapter.AllConnectionsAdapter
import com.zstronics.ceibro.ui.projects.adapter.AllProjectsAdapter
import dagger.hilt.android.AndroidEntryPoint
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

        adapter.itemLongClickListener =
            { _: View, _: Int, data: MyConnection ->
                //showChatActionSheet(data)
            }
    }

}