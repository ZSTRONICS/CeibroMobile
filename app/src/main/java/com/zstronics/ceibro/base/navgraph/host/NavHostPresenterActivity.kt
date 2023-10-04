package com.zstronics.ceibro.base.navgraph.host

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.ActivityNavhostPresenterBinding
import com.zstronics.ceibro.ui.networkobserver.NetworkConnectivityObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

const val NAVIGATION_Graph_ID = "navigationGraphId"
const val NAVIGATION_Graph_START_DESTINATION_ID = "navigationGraphStartDestination"

@AndroidEntryPoint
class NavHostPresenterActivity :
    BaseNavViewModelActivity<ActivityNavhostPresenterBinding, INavHostPresenter.State, NavHostPresenterVM>() {
    override val navigationGraphId: Int
        get() = intent?.getIntExtra(NAVIGATION_Graph_ID, 0) ?: 0
    override val navigationGraphStartDestination: Int
        get() = intent?.getIntExtra(NAVIGATION_Graph_START_DESTINATION_ID, 0) ?: 0
    override val viewModel: NavHostPresenterVM by viewModels()
    override val layoutResId: Int = R.layout.activity_navhost_presenter

    @Inject
    lateinit var networkConnectivityObserver: NetworkConnectivityObserver
    override fun onDestinationChanged(
        controller: NavController?,
        destination: NavDestination?,
        arguments: Bundle?
    ) {
    }

    companion object {
        var activityInstance: NavHostPresenterActivity? = null
        const val BANNER_HIDE_TIME: Long = 3000
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        activityInstance = this
    }

    override fun postExecutePendingBindings(savedInstanceState: Bundle?) {
        super.postExecutePendingBindings(savedInstanceState)
        if (navigationGraphStartDestination != R.id.ceibroDataLoadingFragment) {
            viewModel.viewModelScope.launch {


                networkConnectivityObserver.observe().collect { connectionStatus ->
                    when (connectionStatus) {
                        NetworkConnectivityObserver.Status.Losing -> {
                            // Do not remove this losing state from here
                        }

                        NetworkConnectivityObserver.Status.Available -> {
                            mViewDataBinding.llInternetConnected.visibility = View.VISIBLE
                            mViewDataBinding.llInternetDisconnected.visibility = View.GONE

                            delay(BANNER_HIDE_TIME)
                            // After the delay, hide the views
                            mViewDataBinding.llInternetConnected.visibility = View.GONE
                        }

                        else -> {
                            mViewDataBinding.llInternetConnected.visibility = View.GONE
                            mViewDataBinding.llInternetDisconnected.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
        mViewDataBinding.closeBanner.setOnClickListener {
            mViewDataBinding.llInternetDisconnected.visibility = View.GONE
        }
    }

    override fun onClick(id: Int) {
    }

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
}