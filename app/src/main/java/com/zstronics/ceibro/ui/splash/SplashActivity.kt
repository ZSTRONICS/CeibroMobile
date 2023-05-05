package com.zstronics.ceibro.ui.splash

import android.Manifest
import android.os.Bundle
import android.os.Handler
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.ceibro.permissionx.PermissionX
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.activity.BaseBindingViewModelActivity
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.ActivitySplashBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.immutableListOf
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

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

    @Inject
    lateinit var sessionManager: SessionManager

    override fun postExecutePendingBindings(savedInstanceState: Bundle?) {
        if (sessionManager.isFirstTimeLaunch()) {
            checkPermission(
                immutableListOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_CONTACTS,
                )
            ) { deniedList ->
                sessionManager.setNotFirstTimeLaunch()
                startSplashAnimation()
            }
        } else {
            startSplashAnimation()
        }
    }

    private fun startSplashAnimation() {
        handler = Handler()
        runnable = object : Runnable {
            override fun run() {
                if (count == 1) {
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutUserEvent(event: LocalEvents.LogoutUserEvent) {
        handler.removeCallbacks(runnable)
        viewModel.endUserSession()
        shortToastNow("Session expired, please login")
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
        Thread { this.let { Glide.get(it).clearDiskCache() } }.start()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun checkPermission(
        permissionsList: List<String>,
        function: (deniedList: List<String>) -> Unit
    ) {
        PermissionX.init(this).permissions(
            permissionsList
        ).request { allGranted, grantedList, deniedList ->
            if (allGranted) {
                function.invoke(deniedList)
            } else {
                function.invoke(deniedList)
            }
        }
    }
}