package com.zstronics.ceibro.ui.splash

import android.os.Bundle
import android.os.Handler
import androidx.activity.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.activity.BaseBindingViewModelActivity
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity :
    BaseBindingViewModelActivity<ActivitySplashBinding, ISplash.State, SplashViewModel>() {

    var count = 0
    lateinit var handler: Handler
    lateinit var runnable: Runnable

    override val viewModel: SplashViewModel by viewModels()
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val layoutResId: Int = R.layout.activity_splash

    override fun postExecutePendingBindings(savedInstanceState: Bundle?) {

        handler = Handler()
        runnable = object : Runnable {
            override fun run() {
                if (count == 3) {
                    handler.removeCallbacks(this)

                    if (viewModel.sessionManager.isUserLoggedIn())
                        navigateToDashboard()
                    else
                        navigateToLoginScreen()

                } else {
                    count++
                    animateLogo()
                    handler.postDelayed(this, 950)
                }
            }
        }
        handler.postDelayed(runnable, 250)
    }

    override fun fetchExtras(extras: Bundle?) {
    }

    override fun onClick(id: Int) {
    }

    fun animateLogo() {
        mViewDataBinding.centerLogoC.animate().scaleX(-1F).setDuration(370).withEndAction {
            mViewDataBinding.centerLogoC.animate().scaleX(1F).duration = 370
        }
    }

    private fun navigateToLoginScreen() {
        launchActivity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = true
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.onboarding_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.loginFragment
            )
        }
    }

    private fun navigateToDashboard() {
        launchActivity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = true
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.homeFragment
            )
        }
    }
}