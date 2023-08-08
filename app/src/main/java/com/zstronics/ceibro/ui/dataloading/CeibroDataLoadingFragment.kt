package com.zstronics.ceibro.ui.dataloading

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.databinding.FragmentCeibroDataLoadingBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class CeibroDataLoadingFragment :
    BaseNavViewModelFragment<FragmentCeibroDataLoadingBinding, ICeibroDataLoading.State, CeibroDataLoadingVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: CeibroDataLoadingVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_ceibro_data_loading
    override fun toolBarVisibility(): Boolean = false
    private val API_CALL_COUNT = 6
    override fun onClick(id: Int) {
        navigateToDashboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSplashAnimation()
        viewModel.loadAppData(requireContext()) {
            val progress = viewModel.apiSucceedCount.div(API_CALL_COUNT).times(
                100
            ).toInt()
            mViewDataBinding.syncProgress.setProgress(
                progress, true
            )
            if (viewModel.apiSucceedCount >= API_CALL_COUNT) {
                navigateToDashboard()
            }
        }
    }


    private fun startSplashAnimation() {
        // Set the total duration for the animation (5 seconds)
        val totalDuration = 500000L

        // The duration for each individual animation
        val individualDuration = 640L

        // Calculate the number of times to run the animation
        val repeatCount = totalDuration / (individualDuration * 2)

        object : CountDownTimer(totalDuration, individualDuration * 2) {
            override fun onTick(millisUntilFinished: Long) {
                // Run the animation on each tick
                animateLogo()
            }

            override fun onFinish() {
                // Animation finished
                // You can navigate to the next screen or perform any other action here.
            }
        }.start()
    }

    private fun animateLogo() {
        // Run the scaleX animation
        mViewDataBinding.centerLogoC.animate()
            .scaleX(-1F)
            .setDuration(370)
            .withEndAction {
                // After the first animation is finished, run the second scaleX animation
                mViewDataBinding.centerLogoC.animate()
                    .scaleX(1F).duration = 370
            }
    }

    private fun navigateToDashboard() {
        startPeriodicContactSyncWorker(requireContext())
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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateConnections(event: LocalEvents.UpdateConnections) {
//        viewModel.loadAppData {
//            if (viewModel.apiSucceedCount == API_CALL_COUNT) {
//                navigateToDashboard()
//            }
//        }
    }
}