package com.zstronics.ceibro.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.data.repos.chat.messages.SocketReceiveMessageResponse
import com.zstronics.ceibro.data.repos.chat.messages.socket.AllMessageSeenSocketResponse
import com.zstronics.ceibro.databinding.FragmentDashboardBinding
import com.zstronics.ceibro.ui.chat.ChatFragment
import com.zstronics.ceibro.ui.enums.EventType
import com.zstronics.ceibro.ui.home.HomeFragment
import com.zstronics.ceibro.ui.projects.ProjectsFragment
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.ui.tasks.TasksFragment
import com.zstronics.ceibro.ui.works.WorksFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DashboardFragment :
    BaseNavViewModelFragment<FragmentDashboardBinding, IDashboard.State, DashboardVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: DashboardVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_dashboard
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
//            R.id.profileIcon -> navigate(R.id.profileFragment)
            R.id.profileImg -> navigateToProfile()
            R.id.friendsReqBtn -> navigateToConnections()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*Set socket and establish connection*/
        SocketHandler.setSocket()
        SocketHandler.establishConnection()

//        setBadgeOnChat(R.id.nav_chat, 4)

        mViewDataBinding.bottomNavigation.setOnNavigationItemSelectedListener(navListener)
//        mViewDataBinding.bottomNavigation.selectedItemId = R.id.nav_home
//        childFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment())
//            .commit()

        viewState.selectedItem.observe(viewLifecycleOwner) {
            mViewDataBinding.bottomNavigation.selectedItemId = selectedItem
        }


        SocketHandler.getSocket().on(SocketHandler.CHAT_EVENT_REP_OVER_SOCKET) { args ->
            val navHostFragment =
                activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment)
            val fragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
            when {
                args[0].toString().contains(EventType.RECEIVE_MESSAGE.name) -> {
                    println("RECEIVE_MESSAGE")
                }
            }

            if (fragment is DashboardFragment) {
//                val gson = Gson()
//                val messageType = object : TypeToken<SocketReceiveMessageResponse>() {}.type
//                val message: SocketReceiveMessageResponse = gson.fromJson(args[0].toString(), messageType)
            }
        }
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        selectedItem = item.itemId
        val selectedFragment: Fragment = when (item.itemId) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_chat -> ChatFragment()
            R.id.nav_tasks -> TasksFragment()
            R.id.nav_projects -> ProjectsFragment()
            else -> WorksFragment()
        }

        childFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment)
            .commit()
        true
    }

    private fun setBadgeOnChat(menuItemId: Int, number: Int) {
        val badge = mViewDataBinding.bottomNavigation.getOrCreateBadge(menuItemId)
        badge.isVisible = true
        badge.number = number
        badge.backgroundColor = resources.getColor(R.color.appRed)
    }

    private fun navigateToProfile() {
        launchActivity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = false
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.profileFragment
            )
        }
    }

    private fun navigateToConnections() {
        launchActivity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = false
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.connectionsFragment
            )
        }
    }

    companion object {
        var selectedItem: Int = R.id.nav_home
    }
}