package com.zstronics.ceibro

import android.app.NotificationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.zstronics.ceibro.base.TYPE_EXTRA
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.extensions.launchActivityWithFinishAffinity
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.repos.NotificationTaskData
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    lateinit var centerLogoC: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        centerLogoC = findViewById(R.id.centerLogoCeibro)

        navigateToCeibroDataLoading()
        startSplashAnimation()
    }

    private fun navigateToCeibroDataLoading() {

        val intent = intent
        val taskData: NotificationTaskData? = intent.getParcelableExtra("notificationTaskData")
        val extrasType = intent.getIntExtra(TYPE_EXTRA, 0)
        val navigationGraphId: Int = intent.getIntExtra(NAVIGATION_Graph_ID, 0)
        val startDestinationId: Int = intent.getIntExtra(NAVIGATION_Graph_START_DESTINATION_ID, 0)
        val notificationId: Int = intent.getIntExtra("notificationId", 0)


        val bundle = Bundle()
        bundle.putParcelable("notificationTaskData", taskData)
        bundle.putInt(NAVIGATION_Graph_ID, navigationGraphId)
        bundle.putInt(NAVIGATION_Graph_START_DESTINATION_ID, startDestinationId)

        val notificationManager = NotificationManagerCompat.from(this)
        val notificationManager1 = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val activeNotifications = notificationManager1.activeNotifications

        val notificationCount = activeNotifications.size
        notificationManager.cancel(notificationId)

        println("NotificationSize: $notificationCount")
        //condition is equal to 2 because after calculating count, we are canceling 1 notification, so remaining 1 is of summary notification left, so removed all
        if (notificationCount <= 2) {
            notificationManager.cancelAll()
        }

        if (sessionManager.isLoggedIn()) {
            if (CookiesManager.jwtToken.isNullOrEmpty()) {
                sessionManager.setUser()
                sessionManager.setToken()

                launchActivityWithFinishAffinity<NavHostPresenterActivity>(
                    options = bundle, clearPrevious = true
                ) {
                    putExtra(NAVIGATION_Graph_ID, navigationGraphId)
                    putExtra(
                        NAVIGATION_Graph_START_DESTINATION_ID, R.id.ceibroDataLoadingFragment
                    )
                }
            } else {
                launchActivity<NavHostPresenterActivity>(
                    options = bundle, clearPrevious = true
                ) {
                    putExtra(NAVIGATION_Graph_ID, navigationGraphId)
                    putExtra(
                        NAVIGATION_Graph_START_DESTINATION_ID, startDestinationId
                    )
                }
                Handler().postDelayed({
                    finish()
                }, 50)
            }
        } else {
            launchActivityWithFinishAffinity<NavHostPresenterActivity>(
                options = Bundle(), clearPrevious = true
            ) {
                putExtra(NAVIGATION_Graph_ID, R.navigation.onboarding_nav_graph)
                putExtra(
                    NAVIGATION_Graph_START_DESTINATION_ID, R.id.loginFragment
                )
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
        centerLogoC.animate()
            .scaleX(-1F)
            .setDuration(370)
            .withEndAction {
                // After the first animation is finished, run the second scaleX animation
                centerLogoC.animate()
                    .scaleX(1F).duration = 370
            }
    }
}