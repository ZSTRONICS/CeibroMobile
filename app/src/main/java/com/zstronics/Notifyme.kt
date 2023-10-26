package com.zstronics

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.EXTRA_FROM_NOTIFICATION
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity

class Notifyme : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifyme)

        val intent = intent
        val notificationBundle = intent.getBundleExtra(EXTRA_FROM_NOTIFICATION)

        if (notificationBundle != null) {
            val navigationGraphId = notificationBundle.getInt(NAVIGATION_Graph_ID)
            val startDestinationId =
                notificationBundle.getInt(NAVIGATION_Graph_START_DESTINATION_ID)


            launchActivity<NavHostPresenterActivity>(
                options = Bundle(), clearPrevious = true
            ) {
                putExtra(NAVIGATION_Graph_ID, navigationGraphId)
                putExtra(
                    NAVIGATION_Graph_START_DESTINATION_ID, startDestinationId
                )
            }
        } else {
            finish()
        }

    }
}