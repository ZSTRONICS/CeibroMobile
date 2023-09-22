package com.zstronics.ceibro.base.navgraph.host

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.ActivityNavhostPresenterBinding
import dagger.hilt.android.AndroidEntryPoint

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
    override fun onDestinationChanged(
        controller: NavController?,
        destination: NavDestination?,
        arguments: Bundle?
    ) {
    }

    companion object {
        var activityInstance: NavHostPresenterActivity? = null

    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        activityInstance = this
    }

    override fun onClick(id: Int) {
    }

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
}